package com.example.emall.serviceImpl;

import com.example.emall.enums.RoleEnum;
import com.example.emall.enums.SeckillActivityStatus;
import com.example.emall.exception.EMallException;
import com.example.emall.po.Account;
import com.example.emall.po.Orders;
import com.example.emall.po.Product;
import com.example.emall.po.SeckillActivity;
import com.example.emall.po.SeckillOrder;
import com.example.emall.po.Stockpile;
import com.example.emall.repository.OrdersRepository;
import com.example.emall.repository.ProductRepository;
import com.example.emall.repository.SeckillActivityRepository;
import com.example.emall.repository.SeckillOrderRepository;
import com.example.emall.service.SeckillService;
import com.example.emall.util.SecurityUtil;
import com.example.emall.vo.SeckillActivityVO;
import com.example.emall.vo.SeckillResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class SeckillServiceImpl implements SeckillService {

    private static final String STOCK_KEY = "seckill:stock:%d";
    private static final String USER_KEY  = "seckill:user:%d:%d";

    private static final String SECKILL_LUA =
            "local stock = tonumber(redis.call('GET', KEYS[1]))\n" +
            "if stock == nil or stock <= 0 then\n" +
            "  return -1\n" +
            "end\n" +
            "local isNew = redis.call('SET', KEYS[2], '1', 'NX', 'EX', ARGV[1])\n" +
            "if isNew == false then\n" +
            "  return -2\n" +
            "end\n" +
            "redis.call('DECR', KEYS[1])\n" +
            "return 1\n";

    @Autowired
    private SeckillActivityRepository seckillActivityRepository;

    @Autowired
    private SeckillOrderRepository seckillOrderRepository;

    @Autowired
    private OrdersRepository ordersRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SecurityUtil securityUtil;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Override
    public List<SeckillActivityVO> getActivities() {
        return seckillActivityRepository.findAllWithProduct()
                .stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SeckillActivityVO createActivity(SeckillActivityVO vo) {
        checkAdmin();
        Product product = productRepository.findById(vo.getProductId())
                .orElseThrow(EMallException::productsNotExits);

        SeckillActivity activity = new SeckillActivity();
        activity.setProduct(product);
        activity.setSeckillPrice(vo.getSeckillPrice());
        activity.setSeckillStock(vo.getSeckillStock());
        activity.setStartTime(vo.getStartTime());
        activity.setEndTime(vo.getEndTime());
        activity.setStatus(SeckillActivityStatus.PENDING);
        SeckillActivity saved = seckillActivityRepository.save(activity);
        preloadStock(saved.getId());
        return toVO(saved);
    }

    @Override
    @Transactional
    public SeckillActivityVO updateActivity(Integer id, SeckillActivityVO vo) {
        checkAdmin();
        SeckillActivity activity = seckillActivityRepository.findById(id)
                .orElseThrow(EMallException::seckillActivityNotExits);

        if (vo.getProductId() != null) {
            Product product = productRepository.findById(vo.getProductId())
                    .orElseThrow(EMallException::productsNotExits);
            activity.setProduct(product);
        }
        if (vo.getSeckillPrice() != null) activity.setSeckillPrice(vo.getSeckillPrice());
        if (vo.getSeckillStock() != null) activity.setSeckillStock(vo.getSeckillStock());
        if (vo.getStartTime() != null)    activity.setStartTime(vo.getStartTime());
        if (vo.getEndTime() != null)      activity.setEndTime(vo.getEndTime());
        if (vo.getStatus() != null)       activity.setStatus(SeckillActivityStatus.valueOf(vo.getStatus()));
        SeckillActivity saved = seckillActivityRepository.save(activity);
        preloadStock(saved.getId());
        return toVO(saved);
    }

    @Override
    @Transactional
    public void deleteActivity(Integer id) {
        checkAdmin();
        seckillActivityRepository.deleteById(id);
        redisTemplate.delete(String.format(STOCK_KEY, id));
    }

    @Override
    @Transactional
    public SeckillResultVO doSeckill(Integer activityId) {
        Account currentAccount = securityUtil.getCurrentAccount();
        if (currentAccount == null) throw EMallException.notLogin();

        SeckillActivity activity = seckillActivityRepository.findById(activityId)
                .orElseThrow(EMallException::seckillActivityNotExits);

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(activity.getStartTime())) throw EMallException.seckillNotStarted();
        if (now.isAfter(activity.getEndTime()))     throw EMallException.seckillEnded();

        long ttlSeconds = Math.max(1L, ChronoUnit.SECONDS.between(now, activity.getEndTime()));
        DefaultRedisScript<Long> script = new DefaultRedisScript<>(SECKILL_LUA, Long.class);
        List<String> keys = Arrays.asList(
                String.format(STOCK_KEY, activityId),
                String.format(USER_KEY, activityId, currentAccount.getUserid())
        );
        Long result = redisTemplate.execute(script, keys, String.valueOf(ttlSeconds));

        if (result == null || result == -1L) throw EMallException.seckillSoldOut();
        if (result == -2L)                   throw EMallException.seckillAlreadyPurchased();

        Orders order = new Orders();
        order.setTotalAmount(activity.getSeckillPrice());
        order.setPaymentMethod("SECKILL");
        order.setStatus("PAID");
        order.setCreateTime(LocalDateTime.now());
        order.setAccount(currentAccount);
        Orders savedOrder = ordersRepository.save(order);

        SeckillOrder seckillOrder = new SeckillOrder();
        seckillOrder.setActivity(activity);
        seckillOrder.setAccount(currentAccount);
        seckillOrder.setOrder(savedOrder);
        seckillOrder.setCreateTime(LocalDateTime.now());
        seckillOrderRepository.save(seckillOrder);

        Product product = activity.getProduct();
        Stockpile stockpile = product.getStockpile();
        if (stockpile != null && stockpile.getAmount() > 0) {
            stockpile.setAmount(stockpile.getAmount() - 1);
            product.setSoldQuantity(product.getSoldQuantity() + 1);
            productRepository.save(product);
            redisTemplate.delete("product:detail:" + product.getId());
        }

        return new SeckillResultVO(true, "秒杀成功！", savedOrder.getId());
    }

    @Override
    public void preloadStock(Integer activityId) {
        SeckillActivity activity = seckillActivityRepository.findById(activityId)
                .orElseThrow(EMallException::seckillActivityNotExits);
        String key = String.format(STOCK_KEY, activityId);
        long ttlSeconds = ChronoUnit.SECONDS.between(LocalDateTime.now(), activity.getEndTime());
        if (ttlSeconds > 0) {
            redisTemplate.opsForValue().set(key, String.valueOf(activity.getSeckillStock()),
                    ttlSeconds, TimeUnit.SECONDS);
        }
    }

    private void checkAdmin() {
        Account account = securityUtil.getCurrentAccount();
        if (account == null) throw EMallException.notLogin();
        if (account.getRole() != RoleEnum.admin) throw EMallException.noPermission();
    }

    private SeckillActivityVO toVO(SeckillActivity a) {
        SeckillActivityVO vo = new SeckillActivityVO();
        vo.setId(a.getId());
        vo.setProductId(a.getProduct().getId());
        vo.setProductTitle(a.getProduct().getTitle());
        vo.setProductCover(a.getProduct().getCover());
        vo.setOriginalPrice(a.getProduct().getPrice());
        vo.setSeckillPrice(a.getSeckillPrice());
        vo.setSeckillStock(a.getSeckillStock());
        vo.setStartTime(a.getStartTime());
        vo.setEndTime(a.getEndTime());

        // 根据当前时间自动计算真实状态
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(a.getStartTime())) {
            vo.setStatus("PENDING");
        } else if (now.isAfter(a.getEndTime())) {
            vo.setStatus("ENDED");
        } else {
            vo.setStatus("ACTIVE");
        }

        String redisStock = redisTemplate.opsForValue().get(String.format(STOCK_KEY, a.getId()));
        vo.setRemainingStock(redisStock != null ? Integer.parseInt(redisStock) : a.getSeckillStock());
        return vo;
    }
}
