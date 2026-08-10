# EMall 项目面试指南

---

## 一、项目整体介绍（开场必问）

**Q：介绍一下你的 EMall 项目？**

> EMall 是一个基于 SpringBoot + MySQL + Redis + Vue3 的前后端分离电商平台。后端提供 RESTful API，前端通过 Axios 调用接口，JWT Token 做无状态认证。
>
> 核心功能包括：用户注册登录、商品管理、购物车、订单支付闭环，以及一个支持高并发的秒杀模块。
>
> 在性能优化上，我用 Redis 缓存商品详情，引入随机过期时间防雪崩、互斥锁防击穿；秒杀模块用 Redis + Lua 脚本实现原子化库存扣减和用户去重，保证在高并发下不超卖。

---

## 二、第一点：数据库优化（Redis 缓存 + 防雪崩/击穿）

### 背景知识

| 概念 | 含义 | 解决方案 |
|---|---|---|
| 缓存击穿 | 一个热点 key 过期，大量请求同时打到数据库 | 互斥锁（SETNX） |
| 缓存雪崩 | 大量 key 同时过期，数据库被打垮 | 随机过期时间 |
| 缓存穿透 | 查询不存在的数据，缓存永远不命中 | 布隆过滤器 / 缓存空值 |

### 代码对应位置

`backend/src/main/java/com/example/emall/serviceImpl/ProductServiceImpl.java` → `getInformation()` 方法

### 完整流程

```
请求进来
  ↓
查 Redis（key: product:detail:{id}）
  ↓ 命中 → 直接返回，结束
  ↓ 未命中
尝试 SETNX 加互斥锁（key: product:lock:{id}，TTL=10s）
  ↓ 抢到锁
    → 双重检查 Redis（防止已有其他线程写入）
    → 查数据库
    → 写入 Redis，TTL = 30min + random(0~10min)  ← 防雪崩
    → 释放锁（finally 块保证执行）
  ↓ 没抢到锁
    → sleep 50ms 后重试读缓存
    → 还没有则降级查数据库
```

### 高频面试问题

**Q：为什么用随机过期时间？固定 30 分钟不行吗？**

> 如果所有商品缓存都在同一时刻过期（比如都是 30 分钟），那 30 分钟后所有请求同时打到数据库，造成缓存雪崩。加上随机的 0~10 分钟，让过期时间错开，数据库压力平摊到一段时间内。

**Q：互斥锁是怎么实现的？为什么不用 synchronized？**

> 用 Redis 的 `SETNX`（即 `setIfAbsent`）实现分布式互斥锁。`synchronized` 只能锁单个 JVM 进程内的线程，如果后端部署了多个实例，`synchronized` 无法跨实例互斥，而 Redis 是所有实例共享的，所以能做到分布式级别的互斥。

**Q：锁里面为什么要双重检查？**

> 线程 A 和线程 B 同时发现缓存未命中，A 抢到锁去查数据库，B 等待。A 写完缓存释放锁后，B 拿到锁，如果不做双重检查，B 也会再查一次数据库，造成重复查询。双重检查让 B 拿到锁后先再读一次缓存，发现 A 已经写好了就直接返回。

**Q：锁用 finally 释放，有没有问题？**

> 这里用的是本地 finally 块，Redis 锁的 TTL 设置了 10 秒兜底，即使程序崩溃没执行 finally，锁也会在 10 秒后自动释放，不会死锁。

**Q：你有没有处理缓存穿透？**

> 当前代码里没有专门处理缓存穿透（查不存在的商品 id）。完整方案可以在查数据库返回 null 时，往 Redis 里存一个空值（TTL 设短一点，比如 2 分钟），让后续相同请求命中缓存直接返回 null，不再打数据库。或者用布隆过滤器在请求进来时就过滤掉不存在的 id。

**Q：更新商品时缓存怎么处理？**

> `updateInformation` 和 `deleteProduct` 方法里，数据库写入成功后会立即调用 `redisTemplate.delete(CACHE_KEY_PREFIX + id)` 删除对应缓存，下次请求进来时重新从数据库加载并写入缓存，保证数据一致性。

---

## 三、第二点：接口优化（RESTful + 统一异常处理）

### RESTful API 设计

项目严格遵循 REST 风格：

| 操作 | HTTP 方法 | 路径示例 |
|---|---|---|
| 查询商品列表 | GET | `/api/products` |
| 查询商品详情 | GET | `/api/products/{id}` |
| 新增商品 | POST | `/api/products` |
| 更新商品 | PUT | `/api/products` |
| 删除商品 | DELETE | `/api/products/{id}` |
| 修改库存 | PATCH | `/api/products/stockpile/{id}` |

### 统一异常处理

**代码对应位置：** `GlobalExceptionHandler.java` + `EMallException.java`

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(value = EMallException.class)
    public ResponseVO<String> handleAIExternalException(EMallException e) {
        return ResponseVO.buildFailure(e.getErrorCode(), e.getMessage());
    }
}
```

`EMallException` 是自定义异常，内置了 16 种语义化错误码（如 `notLogin()`、`noPermission()`、`seckillSoldOut()` 等），所有 Controller 不需要自己写 try-catch，抛出异常后统一由 `GlobalExceptionHandler` 捕获，返回标准格式的 `ResponseVO`。

### 高频面试问题

**Q：@RestControllerAdvice 和 @ControllerAdvice 的区别？**

> `@RestControllerAdvice` = `@ControllerAdvice` + `@ResponseBody`。加了 `@ResponseBody` 后，返回值会自动序列化成 JSON 写入响应体，适合 REST API。`@ControllerAdvice` 返回的是视图名，适合传统 MVC。

**Q：统一异常处理的好处是什么？**

> 不用在每个 Controller 方法里写重复的 try-catch；所有异常响应格式统一，前端只需要判断 `code` 字段；新增错误类型只需在 `EMallException` 里加一个工厂方法，不影响其他代码。

**Q：如果有多个 @ExceptionHandler，执行哪个？**

> Spring 会选择最精确匹配的。比如同时有 `@ExceptionHandler(EMallException.class)` 和 `@ExceptionHandler(Exception.class)`，抛出 `EMallException` 时优先走前者；抛出其他未知异常时才走后者（兜底处理）。

**Q：ResponseVO 是什么结构？**

> 统一响应体，包含三个字段：`code`（业务状态码）、`message`（提示信息）、`data`（返回数据）。成功时调用 `buildSuccess(data)`，失败时调用 `buildFailure(code, message)`，前端统一判断 `code` 是否为成功码。

---

## 四、第三点：高并发秒杀模块（核心亮点）

### 整体架构

```
用户请求 doSeckill
  ↓
校验登录状态
  ↓
校验活动时间（未开始/已结束直接拒绝）
  ↓
执行 Lua 脚本（原子操作，Redis 层拦截）
  │  返回 -1 → 库存不足，抛异常
  │  返回 -2 → 用户已购买，抛异常
  │  返回  1 → 秒杀成功
  ↓
@Transactional 写数据库（Orders + SeckillOrder + 扣 MySQL 库存）
  ↓
返回订单 ID
```

### Lua 脚本详解

```lua
local stock = tonumber(redis.call('GET', KEYS[1]))   -- 读库存
if stock == nil or stock <= 0 then
  return -1                                           -- 库存不足
end
local isNew = redis.call('SET', KEYS[2], '1', 'NX', 'EX', ARGV[1])
if isNew == false then
  return -2                                           -- 用户已购买（去重）
end
redis.call('DECR', KEYS[1])                          -- 原子扣减库存
return 1                                             -- 成功
```

- `KEYS[1]` = `seckill:stock:{activityId}`（库存 key）
- `KEYS[2]` = `seckill:user:{activityId}:{userId}`（用户去重 key）
- `ARGV[1]` = 活动剩余秒数（用户 key 的 TTL，活动结束自动清理）

**为什么用 Lua 脚本？** Redis 执行 Lua 脚本是原子的，整个脚本要么全部执行，要么不执行，中间不会插入其他命令。如果用普通命令（GET → 判断 → DECR），两个请求可能同时读到库存为 1，然后都执行 DECR，导致库存变成 -1，出现超卖。

### Redis 库存预热

**代码位置：** `SeckillServiceImpl.java` → `preloadStock()` 方法

管理员创建或更新秒杀活动时，系统自动调用 `preloadStock()`，将数据库中的 `seckillStock` 写入 Redis：

```java
redisTemplate.opsForValue().set(
    "seckill:stock:" + activityId,
    String.valueOf(activity.getSeckillStock()),
    ttlSeconds,          // TTL = 活动结束时间 - 当前时间
    TimeUnit.SECONDS
);
```

TTL 设置为活动剩余时间，活动结束后 Redis key 自动过期，不需要手动清理。

### 高频面试问题

**Q：为什么不直接用数据库做秒杀，非要用 Redis？**

> 秒杀场景下，几千甚至几万请求在同一秒内打过来，如果直接用数据库的行锁来控制库存，数据库连接池会被打满，响应时间急剧上升，甚至宕机。Redis 是纯内存操作，单机 QPS 可以达到十万级，用 Redis 在内存层先拦截掉无效请求（库存不足、重复购买），只有真正秒杀成功的请求才写数据库，大幅减轻数据库压力。

**Q：Lua 脚本为什么能保证原子性？**

> Redis 是单线程处理命令的，Lua 脚本在 Redis 里执行时，整个脚本作为一个不可分割的命令块执行，执行期间不会处理其他客户端的命令。所以"读库存 → 检查用户 → 扣库存"这三步对 Redis 来说是原子的，不存在并发问题。

**Q：SET NX 是什么意思？用来做什么？**

> `SET key value NX` 表示"只有当 key 不存在时才设置"，是原子操作。在秒杀里用来做用户去重：第一次购买时 `seckill:user:{activityId}:{userId}` 不存在，SET NX 成功返回 OK；第二次购买时 key 已存在，SET NX 失败返回 nil，Lua 脚本返回 -2，告知用户已购买过，防止一个用户多次秒杀。

**Q：Redis 扣减成功了，但数据库写入失败了怎么办？**

> 这是秒杀模块目前的一个潜在问题。`doSeckill` 方法加了 `@Transactional`，数据库操作失败会回滚，但 Redis 的扣减不会回滚（Redis 没有事务回滚机制）。完整的解决方案是引入消息队列（如 RocketMQ）做异步落库，或者在异常时手动执行 `INCR` 补回 Redis 库存。面试可以主动提出这个问题并说明解决思路，展示你对细节的思考。

**Q：@Transactional 在秒杀里的作用是什么？**

> 秒杀成功后需要写三张表：`orders`（订单）、`seckill_orders`（秒杀订单关联）、更新 `product` 的 `stockpile`（MySQL 库存）。`@Transactional` 保证这三个操作要么全部成功，要么全部回滚，不会出现订单写成功了但 MySQL 库存没扣减的数据不一致情况。

**Q：管理员权限是怎么校验的？**

> `checkAdmin()` 方法从 Spring Security 的上下文中获取当前登录用户（通过 JWT Token 解析），检查用户的 `role` 字段是否为 `RoleEnum.admin`，不是管理员则抛出 `noPermission` 异常，由 `GlobalExceptionHandler` 统一返回 403。创建、更新、删除秒杀活动的接口都在方法开头调用 `checkAdmin()`。

**Q：秒杀库存预热的 TTL 为什么设置成活动剩余时间？**

> 活动结束后这批库存数据就没用了，TTL 到期自动清理，不需要手动维护，也避免了下次活动创建时读到旧库存的问题。同时如果管理员更新了活动，`preloadStock()` 会重新写入，自动覆盖旧值并更新 TTL。

---

## 五、JPA/Hibernate ORM 相关

**Q：JPA 和 Hibernate 的关系是什么？**

> JPA（Java Persistence API）是 Java 官方的 ORM 规范，只定义接口。Hibernate 是 JPA 规范的一个具体实现。Spring Boot 的 `spring-boot-starter-data-jpa` 默认就使用 Hibernate 作为底层实现。

**Q：项目里 @Transactional 加在 Service 层还是 Repository 层？为什么？**

> 加在 Service 层。因为一个业务操作往往涉及多个 Repository 操作，需要把它们放在同一个事务里保证原子性。如果加在 Repository 层，每个数据库操作是独立的事务，无法跨操作回滚。

**Q：Hibernate.initialize() 是做什么的？**

> JPA 默认使用懒加载（LAZY），关联对象（如商品的 `specifications`、`stockpile`）在查询主对象时不会立即加载，只有真正访问时才发 SQL。但如果 Session 已经关闭（离开事务范围），再访问懒加载字段会抛 `LazyInitializationException`。`Hibernate.initialize()` 在 Session 还开着的时候强制初始化关联集合，避免这个问题。

---

## 六、JWT 认证相关（必问）

**Q：JWT 是什么？项目里怎么用的？**

> JWT（JSON Web Token）是一种无状态的认证方案，由 Header、Payload、Signature 三部分组成，用 Base64 编码后用 `.` 拼接。用户登录成功后服务端生成 JWT 返回给前端，前端每次请求在 Header 里携带 `Authorization: Bearer {token}`，服务端验证签名和有效期，从 Payload 里解析出用户信息，不需要查数据库或查 Session。

**Q：JWT 和 Session 的区别？**

> Session 是有状态的，服务端需要存储 Session 信息，多实例部署时需要 Session 共享（Redis）。JWT 是无状态的，服务端不存储任何信息，天然支持水平扩展；缺点是 Token 发出后无法主动失效（除非维护黑名单）。

---

## 七、前后端分离相关

**Q：跨域问题是怎么解决的？**

> 后端配置了 `CorsFilter`，对所有请求设置了 `Access-Control-Allow-Origin`、`Access-Control-Allow-Methods` 等响应头，允许前端域名的跨域请求。Spring Security 的过滤器链里让 CorsFilter 优先于认证过滤器执行，确保 OPTIONS 预检请求不被拦截。

---

## 八、可以主动加分的点

面试最后可以主动提：

**1. 你发现了哪些可以改进的地方？**
- 购物车添加商品时，应该先校验库存再插入，目前顺序反了
- 缓存穿透还没有处理，可以加布隆过滤器或缓存空值
- 秒杀 Redis 扣减成功但数据库失败时，没有补偿机制，可以引入消息队列

**2. 如果要支持更高并发，你会怎么优化？**
- 秒杀接口加限流（令牌桶/漏桶），在网关层或 Controller 层拦截超量请求
- 数据库写操作改为异步，通过消息队列削峰填谷
- 商品列表接口也加 Redis 缓存
