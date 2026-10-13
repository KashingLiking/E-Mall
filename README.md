# EMall 项目面试指南

> 按四个维度组织：**怎么设计 / 为什么这么设计 / 有哪些功能 / 原理**
> 每道题标明维度、对应代码位置、追问预测。

---

## 一句话开场白（必背）

> "EMall 是基于 Spring Boot + MySQL + Redis + Vue 3 的前后端分离电商平台。后端提供 RESTful API，JWT 做无状态认证，Redis 缓存商品详情并用互斥锁防击穿、随机 TTL 防雪崩。核心亮点是秒杀模块：用 Redis + Lua 脚本做原子化库存扣减和用户去重，从根本上杜绝超卖，再通过 `@Transactional` 保证数据库写入的一致性。"

---

## 一、整体架构

### 【怎么设计】整体架构是怎样的？

```
浏览器 (Vue 3 + Element Plus)
    ↕ Axios (自动带 token header)
Spring Boot 后端（端口 8080）
  ┌──────────────────────────────────────────┐
  │  CorsFilter          HIGHEST_PRECEDENCE  │  OPTIONS 预检直接放行
  │  LoginInterceptor    HandlerInterceptor  │  JWT 验证（排除登录/注册/商品列表）
  ├──────────────────────────────────────────┤
  │  AccountController   /api/accounts       │
  │  ProductController   /api/products       │
  │  CartController      /api/cart           │
  │  OrdersController    /api/orders         │
  │  SeckillController   /api/seckill        │
  │  UploadController    /api/upload         │
  ├──────────────────────────────────────────┤
  │  ServiceImpl 层  （业务逻辑）             │
  │  Repository 层   （Spring Data JPA）      │
  ├──────────────────────────────────────────┤
  │  MySQL 8.0    (JPA ddl-auto: update)     │
  │  Redis        (StringRedisSerializer)    │
  └──────────────────────────────────────────┘
```

**代码位置：** `com.example.emall.EMallApplication`（入口）、`configure/` 包（拦截器和过滤器）

**技术栈版本：** Spring Boot 2.7.6、Java 8、com.auth0:java-jwt 3.10.3、Vue 3 + Vite 6.2.3 + Element Plus 2.9.7

---

### 【怎么设计】包结构是怎么组织的？

| 包 | 职责 |
|---|---|
| `controller/` | REST 接口（6 个 Controller） |
| `service/` + `serviceImpl/` | 接口 + 实现分离 |
| `repository/` | Spring Data JPA 接口 |
| `po/` | JPA `@Entity` 实体类（数据库映射） |
| `vo/` | 请求/响应 DTO（13 个 VO） |
| `configure/` | CORS、拦截器、Redis 配置 |
| `util/` | TokenUtil（JWT 工具）、SecurityUtil（获取当前用户） |
| `exception/` | EMallException + GlobalExceptionHandler |
| `enums/` | 枚举类（BookType、MembershipLevel、RoleEnum 等） |

PO 和 VO 严格分离，`po.toVO()` 和 `vo.toPO()` 手动转换，不用 MapStruct。

---

## 二、认证系统

### 【怎么设计】认证是怎么实现的？

**代码位置：** `configure/LoginInterceptor.java`、`util/TokenUtil.java`

1. 登录：`AccountServiceImpl.login()` 用 BCrypt 验密，成功后 `TokenUtil.getToken(account)` 签发 JWT
2. JWT 签发：`JWT.create().withAudience(username).withExpiresAt(now + 24h).sign(HMAC256(account.getPassword()))`
3. 前端存 `sessionStorage('token')`，每次请求 Axios 拦截器自动加 `token` header
4. `LoginInterceptor.preHandle()` 验证：先 decode 取 username → 查 DB 拿密码 → 用密码重建 verifier 验签名+有效期
5. 验证成功后把 Account 存 session：`request.getSession().setAttribute("currentAccount", account)`

**排除路径：** 登录、注册、商品列表、商品详情不需要 token（通过 `excludePathPatterns` 配置）。

---

### 【为什么这么设计】为什么用自定义拦截器，而不用 Spring Security 的过滤链？

`SecurityConfig.java` 里 `.anyRequest().permitAll()` 和 `.csrf().disable()` 把 Spring Security 的认证链完全关掉。

原因：Spring Security 的 JWT 过滤链配置相对复杂，项目规模不需要方法级 `@PreAuthorize` 注解。自定义 `HandlerInterceptor` 50 行就够，逻辑更直观。

代价：权限检查只能在 Service 层手动 `checkAdmin()` 调用，没有声明式注解，容易漏。

---

### 【原理】JWT 为什么用密码做签名密钥？有什么妙用？

`HMAC256(account.getPassword())`：密钥是该用户的 BCrypt 哈希值。

**关键推论：用户改密码 → BCrypt 哈希变 → HMAC 密钥变 → 旧 token 签名验证失败 → 自动作废。**

不需要维护 token 黑名单，"修改密码自动踢下线"功能零成本实现。

代价：每次请求验证 token 都要查一次数据库（SELECT by username），没有 token 缓存。

**代码位置：** `util/TokenUtil.java` → `getToken()`、`verifyToken()`

---

## 三、Redis 缓存

### 【怎么设计】商品详情缓存是怎么实现的？

**代码位置：** `serviceImpl/ProductServiceImpl.java` → `getInformation()`

```
缓存 key：product:detail:{id}
锁   key：product:lock:{id}

1. Redis GET(cacheKey) → 命中 → 反序列化返回
2. 未命中 → SETNX(lockKey, "1", 10s)
3. 拿到锁 → 二次检查缓存 → 仍未命中 → 查 DB
            → 写缓存，TTL = 30 + random(0~10) 分钟
            → finally: DEL(lockKey)
4. 未拿到锁 → sleep(50ms) → 再读一次缓存
            → 仍未命中 → 直接查 DB（降级）
5. Redis 异常 → 日志警告 + 降级查 DB
```

**序列化方式：** `RedisConfig.java` 用 `StringRedisSerializer`，手动 `objectMapper.writeValueAsString(productVO)` 存，`objectMapper.readValue(cached, ProductVO.class)` 取。`@Primary ObjectMapper` 注册了 `JavaTimeModule` 处理 `LocalDateTime`。

---

### 【为什么这么设计】这个方案解决了哪三个缓存经典问题？

| 问题 | 定义 | 本项目解法 |
|---|---|---|
| **缓存雪崩** | 大量 key 同时过期，DB 被瞬间打垮 | `30 + random(0~10)` 分钟随机 TTL，错开过期时间 |
| **缓存击穿** | 热点 key 过期，大量并发同时穿透到 DB | `SETNX` 互斥锁 + 双重检查（拿到锁才查 DB） |
| **缓存穿透** | 查不存在的 key，每次都打到 DB | 当前未处理（可改进：缓存空值或布隆过滤器） |

**追问：为什么要二次检查缓存（double-check）？**
> 第一个线程拿到锁在查 DB 时，其他线程在等。等锁的线程拿到锁后，缓存已经被写好了，直接用就行，不需要再查 DB。

---

### 【原理】缓存什么时候失效？

四个地方主动删缓存（`redisTemplate.delete("product:detail:" + id)`）：
1. `updateInformation()` — 商品信息更新
2. `deleteProduct()` — 商品删除
3. 秒杀成功后 `doSeckill()` 里扣减 MySQL 库存后
4. 普通支付 `putAfford()` 扣减库存后

写操作都是"先写 DB，再删缓存"（Cache-Aside 模式）。

---

## 四、秒杀模块（核心亮点）

### 【怎么设计】秒杀整体流程是怎样的？

**代码位置：** `serviceImpl/SeckillServiceImpl.java` → `doSeckill()`

```
POST /api/seckill/{activityId}/buy
  1. 查活动（状态、时间窗口校验）
  2. 执行 Lua 脚本（Redis 原子操作）
     → -1：库存为 0 → 抛 seckillSoldOut
     → -2：已购买  → 抛 seckillAlreadyPurchased
     → 1：成功
  3. @Transactional DB 写入（三步）：
     a. 创建 Orders（status=PAID, paymentMethod=SECKILL）
     b. 创建 SeckillOrder（activity_id + user_id，有 UNIQUE 约束兜底）
     c. 扣减 MySQL 库存 + 递增 soldQuantity + 删 Redis 产品缓存
```

---

### 【原理】Lua 脚本的内容是什么？为什么能防超卖？

**代码位置：** `serviceImpl/SeckillServiceImpl.java` → `SECKILL_LUA` 常量

```lua
local stock = tonumber(redis.call('GET', KEYS[1]))
if stock == nil or stock <= 0 then return -1 end   -- 无库存
local isNew = redis.call('SET', KEYS[2], '1', 'NX', 'EX', ARGV[1])
if isNew == false then return -2 end               -- 已购买
redis.call('DECR', KEYS[1])                        -- 扣库存
return 1
```

- `KEYS[1]`：`seckill:stock:{activityId}`（库存计数器）
- `KEYS[2]`：`seckill:user:{activityId}:{userId}`（用户去重标记）
- `ARGV[1]`：`ChronoUnit.SECONDS.between(now, activity.getEndTime())`（去重 key 活动结束时自动过期）

**防超卖原理：** Redis 是单线程处理命令的，Lua 脚本在 Redis 里原子执行，整个脚本期间不处理其他客户端命令。"查库存 + 检查用户 + 扣库存"三步合一，不存在并发间隙，根本不可能出现两个请求同时读到库存为 1 都成功的情况。

---

### 【为什么这么设计】为什么不用普通的 Java 代码做判断？

普通代码：`GET 库存 → 判断 → DECR`，这是三条独立 Redis 命令。高并发下两个请求同时读到库存=1，都判断通过，都执行 DECR，库存变成 -1，出现超卖。

Lua 脚本：三步是一个原子操作，中间不可被打断。

---

### 【为什么这么设计】为什么还要在 DB 建 `SeckillOrder` 的 UNIQUE 约束？

Lua 脚本是第一道防线。`(activity_id, user_id)` 的数据库唯一约束是第二道防线。

万一极端情况下 Redis 集群出现问题（如主从切换、持久化异常），DB 层兜底确保一个用户最多只有一条秒杀订单，不会出现数据异常。

---

### 【原理】库存如何预热到 Redis？

**代码位置：** `serviceImpl/SeckillServiceImpl.java` → `preloadStock()`

```java
long ttlSeconds = ChronoUnit.SECONDS.between(LocalDateTime.now(), activity.getEndTime());
if (ttlSeconds > 0) {
    redisTemplate.opsForValue().set(
        "seckill:stock:" + activityId,
        String.valueOf(activity.getSeckillStock()),
        ttlSeconds, TimeUnit.SECONDS
    );
}
```

- TTL = 活动剩余时长，活动结束后 Redis key 自动过期，零成本清理
- 在创建活动（`createActivity`）和更新活动（`updateActivity`）时自动调用
- 活动结束前库存 key 被查 `doSeckill` 使用；`GET /api/seckill` 的 `toVO()` 也实时从 Redis 读剩余库存展示给前端

---

### 【原理】秒杀存在什么已知问题？怎么改进？

**已知问题：** Lua 脚本扣 Redis 成功后，`@Transactional` DB 写入可能失败（网络抖动、DB 超时）。此时 Redis 已扣库存、用户去重 key 已写入，但没有订单——用户白白消耗了一次购买机会，而且 DB 库存没有减少，两者不一致。

**改进方向：**
1. DB 写失败时执行补偿：`INCR` 恢复库存、`DEL` 删去重 key
2. 引入消息队列（如 RocketMQ）：Lua 成功后发消息，消费者异步写 DB，支持重试直到成功

---

## 五、普通购物流程

### 【有哪些功能】购物车和订单流程是怎样的？

**代码位置：** `serviceImpl/CartsServiceImpl.java`、`serviceImpl/OrdersServiceImpl.java`

**加购：** `POST /api/cart` → 验库存 → 存 cart 行（product_id + user_id + quantity）

**结算：** `POST /api/cart/checkout`（CheckoutRequestVO：cartItemIds[] + 收货地址 + 支付方式）→ 校验购物车归属 → 计算总价 → 创建 Orders + CartsOrdersRelation

**支付（Mock）：** `POST /api/orders/{orderId}/pay`
1. 设 status=PAID，paymentMethod=MOCK
2. `deductInventory(orderId)`：JOIN FETCH 全图 → 逐项扣减 MySQL 库存 + 递增 soldQuantity + 删 Redis 产品缓存
3. `removePurchasedCartItems(orderId)`：先删 `carts_orders_relation`，再删 `carts`（两条 native SQL）
4. `addToTotalSpent(userId, amount)` → `updateMembershipLevel()`（可能触发会员升级）

---

### 【原理】Cart-Order 为什么用 junction 表而不是在订单里直接存商品？

`CartsOrdersRelation` 表把购物车行和订单行关联起来（多对多桥接）。好处：
- 支付前购物车条目保持原状，便于查看历史
- 支付成功后再物理删除购物车行，逻辑清晰
- 一个订单可以包含多个购物车条目，一个购物车条目未来也可以对应多个部分履约（扩展性）

---

## 六、会员系统

### 【有哪些功能】会员等级怎么升级的？

**代码位置：** `enums/MembershipLevel.java`、`serviceImpl/AccountServiceImpl.java` → `updateMembershipLevel()`

| 等级 | 累计消费门槛 |
|---|---|
| BRONZE | 0 |
| SILVER | 1000 |
| GOLD | 5000 |
| PLATINUM | 10000 |
| DIAMOND | 50000 |

每次成功支付调 `addToTotalSpent()` 累加消费额，再调 `MembershipLevel.fromTotalSpent(total)` 重新计算等级。如果等级变了就 save。

---

## 七、商品管理

### 【有哪些功能】商品模块有哪些接口？

**代码位置：** `controller/ProductController.java`、`serviceImpl/ProductServiceImpl.java`

| 接口 | 方法 | 说明 |
|---|---|---|
| `GET /api/products` | 无需认证 | 查全部商品列表 |
| `GET /api/products/{id}` | 无需认证 | 查详情（Redis 缓存） |
| `GET /api/products/search?title=` | 无需认证 | LIKE 模糊搜索 |
| `POST /api/products` | 需 token | 新建商品 |
| `PUT /api/products` | 需 token | 更新商品（重建 specifications 列表） |
| `DELETE /api/products/{id}` | 需 token | 删除商品 + 清缓存 |
| `PATCH /api/products/stockpile/{id}` | 需 token | 单独更新库存 |

商品有灵活的 `specifications`（作者、ISBN、装帧等），存独立 key-value 表，更新时先 delete all 再 insert new（重建策略）。

---

## 八、JPA 查询设计

### 【原理】Hibernate 懒加载怎么处理的？

所有关联关系都是 `FetchType.LAZY`，在 `@Transactional` 方法里调 `Hibernate.initialize()` 手动初始化：

```java
// queryFromDb() 里
Hibernate.initialize(product.getSpecifications());
Hibernate.initialize(product.getStockpile());
```

需要关联查询的场景用 JOIN FETCH：

**代码位置：** `repository/` 各接口

| 查询 | JPQL |
|---|---|
| 购物车详情（防 N+1） | `JOIN FETCH c.product p JOIN FETCH p.stockpile` |
| 订单扣库存（防 N+1） | 深度嵌套：Orders → CartsOrdersRelation → Carts → Product → Stockpile |
| 秒杀列表（防 N+1） | `JOIN FETCH a.product` |
| 模糊搜索 | `WHERE p.title LIKE %:title%` |

---

### 【原理】分页查询怎么实现的？

`GET /api/orders` 用 Spring Data 的 `Page<Orders>`：

```java
Pageable pageable = PageRequest.of(page, size, Sort.by("createTime").descending());
Page<Orders> orders = ordersRepository.findByAccount(account, pageable);
```

返回给前端的 `PageResponseVO` 包含 `content`、`totalElements`、`totalPages`、`pageNumber`。

---

## 九、前端架构

### 【怎么设计】前端请求和路由守卫是怎么做的？

**代码位置：** `frontend/src/utils/request.ts`、`frontend/src/router/index.ts`

**Axios 拦截器：**
- 请求拦截：自动从 `sessionStorage` 读 token 加到 `config.headers['token']`
- 响应拦截：只处理 HTTP 层面的异常，不全局拦截 401 自动跳登录（各组件自己处理业务错误）

**路由守卫：** `beforeEach` 只保护 `meta: { requiresAuth: true }` 的路由（目前只有 `/edit-profile` 和 `/orders`），其他路由不拦截。

**Token 存储用 `sessionStorage`（不是 localStorage）：** 标签页关闭自动清除，不会在多标签页间共享状态。

---

### 【原理】秒杀页面倒计时怎么做的？

**代码位置：** `frontend/src/views/seckill/SeckillList.vue`

```javascript
setInterval(updateCountdowns, 1000)  // onMounted 启动
onUnmounted(() => clearInterval(...)) // 组件销毁清理
```

`updateCountdowns` 每秒计算：
- `now < startTime` → "未开始"（显示距开始倒计时）
- `startTime <= now < endTime` → "进行中"（显示 HH:mm:ss 剩余时间）
- `now >= endTime` → "已结束"

`remainingStock` 直接从后端 `toVO()` 里读 Redis 实时值，不前端轮询，列表查询就是最新库存。

`toDate()` 同时兼容 ISO string 和 `[year,month,day,hour,minute,second]` 数组格式（防御性编码，历史原因）。

---

## 十、文件上传

### 【有哪些功能】图片上传是怎么实现的？

**代码位置：** `controller/UploadController.java`

- 校验 MIME type（image/jpeg、image/png、image/webp）+ 文件扩展名双重验证
- UUID 重命名，存到运行目录 `uploads/` 下
- 返回路径 `/uploads/{uuid}.ext`
- `MyWebMvcConfig` 注册静态资源 handler：`/uploads/**` → 文件系统路径
- 前端 `resolveImageUrl()` 拼 `http://localhost:8080` 前缀

---

## 十一、已知设计缺陷（主动提，体现思考深度）

| 缺陷 | 位置 | 说明 |
|---|---|---|
| 秒杀 Redis-DB 不一致 | `SeckillServiceImpl.doSeckill()` | Lua 成功后 DB 写失败无补偿机制 |
| 每次认证 2-4 次 DB 查询 | `LoginInterceptor` + `SecurityUtil` | `verifyToken` + `getAccount` 各查一次，Service 层还会再查 |
| 加购顺序问题 | `CartsServiceImpl.addCartProduct()` | `save()` 在库存检查之前调用（`@Transactional` 会回滚，但逻辑顺序错误） |
| 无缓存穿透保护 | `ProductServiceImpl.getInformation()` | 不存在的商品 ID 每次都打 DB，未缓存空值 |
| 商品 CRUD 无管理员校验 | `ProductController` | 任意登录用户可增删改商品，只有秒杀管理检查了 admin 角色 |
| 密码暴露在 AccountVO | `AccountVO.toVO()` | BCrypt 哈希被映射到 VO 并通过 GET 接口返回给前端 |

---

## 十二、高频追问速答

| 追问 | 答 |
|---|---|
| Redis 和 MySQL 一致性怎么保证？ | Cache-Aside：先写 DB 成功，再删缓存；下次读时重新加载 |
| 为什么 Lua 脚本能保证原子性？ | Redis 单线程处理命令，Lua 脚本是不可分割的命令块，执行期间不处理其他客户端请求 |
| 超卖问题还有哪些解法？ | MySQL 悲观锁（`SELECT FOR UPDATE`）、乐观锁（version 字段 CAS）、消息队列串行化；Lua 方案最高效 |
| token 过期了怎么处理？ | 当前无 refresh token，过期直接 401，前端各组件自己处理跳转登录 |
| 为什么用 BCrypt 不用 MD5？ | BCrypt 内置 salt + work factor（rounds=10），计算慢但可配置；MD5 是定长哈希，彩虹表秒破 |
| 如果要支持高并发怎么扩展？ | 后端无状态（JWT），可水平扩展；Redis 单点改集群；MySQL 读写分离；秒杀限流（令牌桶/Sentinel） |
| 秒杀库存预热时机？ | 创建/更新活动时立即预热，TTL = 活动剩余时长，结束自动过期 |
| `sessionStorage` vs `localStorage`？ | sessionStorage 标签页关闭自动清除，安全性更好；localStorage 跨标签页共享，适合"记住我" |
