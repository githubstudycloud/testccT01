# Platform开发规范文档

## 目录
- [1. 项目结构规范](#1-项目结构规范)
- [2. 代码编写规范](#2-代码编写规范)
- [3. DDD设计规范](#3-ddd设计规范)
- [4. 数据库设计规范](#4-数据库设计规范)
- [5. API设计规范](#5-api设计规范)
- [6. 异常处理规范](#6-异常处理规范)
- [7. 日志规范](#7-日志规范)
- [8. 测试规范](#8-测试规范)
- [9. 部署规范](#9-部署规范)
- [10. 性能优化规范](#10-性能优化规范)

## 1. 项目结构规范

### 1.1 模块命名规范
- 所有模块名称以 `platform-` 开头
- 使用小写字母和连字符分隔：`platform-user-service`
- 避免使用缩写，使用完整单词

### 1.2 包结构规范
```
com.platform.{service}
├── application/          # 应用服务层
│   ├── command/         # 命令对象
│   ├── query/           # 查询对象
│   └── service/         # 应用服务
├── domain/              # 领域层
│   ├── entity/          # 实体
│   ├── valueobject/     # 值对象
│   ├── service/         # 领域服务
│   ├── repository/      # 仓储接口
│   └── event/           # 领域事件
├── infrastructure/      # 基础设施层
│   ├── repository/      # 仓储实现
│   ├── config/          # 配置类
│   └── client/          # 外部客户端
├── interfaces/          # 接口层
│   ├── rest/            # REST控制器
│   ├── dto/             # 数据传输对象
│   └── facade/          # 门面服务
└── {ServiceName}Application.java
```

### 1.3 文件命名规范
- Java类名使用帕斯卡命名法：`UserService`
- 配置文件使用小写字母和连字符：`application-dev.yml`
- 数据库表名使用小写字母和下划线：`user_profiles`

## 2. 代码编写规范

### 2.1 Java编码规范
```java
// ✅ 正确示例
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    public Result<User> createUser(CreateUserCommand command) {
        try {
            // 业务逻辑
            User user = User.create(command.getUsername(), command.getPassword());
            User savedUser = userRepository.save(user);

            log.info("用户创建成功: {}", savedUser.getId());
            return Result.success(savedUser);
        } catch (Exception e) {
            log.error("用户创建失败", e);
            return Result.error("USER_CREATE_ERROR", e.getMessage());
        }
    }
}
```

### 2.2 注释规范
```java
/**
 * 用户服务类
 * 负责处理用户相关的业务逻辑
 *
 * @author platform-team
 * @since 1.0.0
 */
public class UserService {

    /**
     * 创建用户
     *
     * @param command 创建用户命令对象
     * @return 创建结果，包含用户信息
     * @throws BusinessException 当用户名已存在时抛出
     */
    public Result<User> createUser(CreateUserCommand command) {
        // 实现逻辑
    }
}
```

### 2.3 变量命名规范
- 使用有意义的变量名
- 布尔变量使用 `is`, `has`, `can` 等前缀
- 集合变量使用复数形式
- 常量使用全大写字母和下划线

```java
// ✅ 正确示例
private boolean isActive;
private boolean hasPermission;
private List<User> activeUsers;
private static final String DEFAULT_PASSWORD = "123456";

// ❌ 错误示例
private boolean flag;
private List<User> user;
private String pwd;
```

## 3. DDD设计规范

### 3.1 聚合设计原则
- 一个聚合根负责一个业务概念
- 聚合内保持强一致性，聚合间保持最终一致性
- 聚合大小适中，避免过大或过小

```java
// ✅ 正确的聚合根设计
@Entity
@Table(name = "users")
public class User extends AggregateRoot {

    // 聚合根标识
    @Id
    private UserId id;

    // 业务属性
    private Username username;
    private Email email;

    // 业务行为
    public void changeEmail(Email newEmail) {
        this.email = newEmail;
        addDomainEvent(new UserEmailChangedEvent(this.id, newEmail));
    }
}
```

### 3.2 值对象设计原则
- 值对象必须是不可变的
- 实现equals和hashCode方法
- 包含验证逻辑

```java
// ✅ 正确的值对象设计
@Value
public class Email {
    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private final String value;

    public Email(String email) {
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Invalid email format");
        }
        this.value = email;
    }
}
```

### 3.3 领域事件规范
- 事件名称使用过去时：`UserCreatedEvent`
- 事件包含足够的信息用于后续处理
- 事件发布采用异步方式

```java
// ✅ 正确的领域事件
public class UserCreatedEvent extends DomainEvent {
    private final UserId userId;
    private final Username username;
    private final Email email;

    public UserCreatedEvent(UserId userId, Username username, Email email) {
        super();
        this.userId = userId;
        this.username = username;
        this.email = email;
    }
}
```

## 4. 数据库设计规范

### 4.1 表命名规范
- 表名使用小写字母和下划线
- 表名使用复数形式：`users`, `orders`
- 中间表命名格式：`table1_table2`

### 4.2 字段命名规范
- 字段名使用小写字母和下划线
- 主键统一命名为 `id`
- 外键命名格式：`{table}_id`
- 时间字段：`created_at`, `updated_at`

### 4.3 索引规范
```sql
-- 主键索引
PRIMARY KEY (`id`)

-- 唯一索引
UNIQUE KEY `uk_username` (`username`)

-- 普通索引
KEY `idx_email` (`email`)

-- 复合索引
KEY `idx_status_created` (`status`, `created_at`)
```

## 5. API设计规范

### 5.1 RESTful API规范
```java
// ✅ 正确的API设计
@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "用户管理", description = "用户相关接口")
public class UserController {

    @GetMapping
    @Operation(summary = "查询用户列表")
    public Result<PageResult<UserDTO>> getUsers(
        @RequestParam(defaultValue = "1") Long current,
        @RequestParam(defaultValue = "10") Long size) {
        // 实现逻辑
    }

    @PostMapping
    @Operation(summary = "创建用户")
    public Result<UserDTO> createUser(@Valid @RequestBody CreateUserRequest request) {
        // 实现逻辑
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询用户")
    public Result<UserDTO> getUser(@PathVariable Long id) {
        // 实现逻辑
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新用户")
    public Result<UserDTO> updateUser(
        @PathVariable Long id,
        @Valid @RequestBody UpdateUserRequest request) {
        // 实现逻辑
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除用户")
    public Result<Void> deleteUser(@PathVariable Long id) {
        // 实现逻辑
    }
}
```

### 5.2 HTTP状态码规范
- 200: 成功
- 201: 创建成功
- 400: 请求参数错误
- 401: 未认证
- 403: 无权限
- 404: 资源不存在
- 500: 服务器内部错误

### 5.3 响应格式规范
```json
{
  "code": "00000",
  "message": "成功",
  "data": {
    "id": 1,
    "username": "admin"
  },
  "timestamp": 1640995200000
}
```

## 6. 异常处理规范

### 6.1 异常分类
- `DomainException`: 领域异常
- `BusinessException`: 业务异常
- `ValidationException`: 验证异常
- `ResourceNotFoundException`: 资源未找到异常

### 6.2 异常处理示例
```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.warn("业务异常: {}", e.getMessage());
        return Result.error(e.getErrorCode(), e.getMessage());
    }

    @ExceptionHandler(ValidationException.class)
    public Result<Void> handleValidationException(ValidationException e) {
        log.warn("验证异常: {}", e.getMessage());
        return Result.error("VALIDATION_ERROR", e.getMessage());
    }
}
```

## 7. 日志规范

### 7.1 日志级别
- ERROR: 系统错误，需要立即处理
- WARN: 警告信息，需要关注
- INFO: 重要的业务信息
- DEBUG: 调试信息，生产环境关闭

### 7.2 日志格式
```java
// ✅ 正确的日志写法
log.info("用户登录成功: userId={}, ip={}", userId, clientIp);
log.warn("用户登录失败: username={}, reason={}", username, "密码错误");
log.error("发送邮件失败: email={}", email, exception);

// ❌ 错误的日志写法
log.info("用户登录成功: " + userId);
log.error("发送邮件失败");
```

## 8. 测试规范

### 8.1 单元测试
```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("创建用户成功")
    void createUser_Success() {
        // Given
        CreateUserCommand command = new CreateUserCommand("test", "password");
        User user = User.create("test", "password");
        when(userRepository.save(any(User.class))).thenReturn(user);

        // When
        Result<User> result = userService.createUser(command);

        // Then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData().getUsername()).isEqualTo("test");
    }
}
```

### 8.2 集成测试
```java
@SpringBootTest
@Testcontainers
class UserControllerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:13");

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void createUser_Integration() {
        CreateUserRequest request = new CreateUserRequest("test", "password");

        ResponseEntity<Result> response = restTemplate.postForEntity(
            "/api/v1/users", request, Result.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
```

## 9. 部署规范

### 9.1 Docker规范
```dockerfile
# 使用多阶段构建
FROM openjdk:21-jdk-slim as builder
WORKDIR /app
COPY . .
RUN ./mvnw clean package -DskipTests

FROM openjdk:21-jre-slim
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 9.2 配置管理
- 生产环境配置通过环境变量或配置中心管理
- 敏感信息使用加密存储
- 不同环境使用不同的配置文件

## 10. 性能优化规范

### 10.1 数据库优化
- 合理使用索引
- 避免N+1查询问题
- 使用连接池
- 分页查询使用LIMIT

### 10.2 缓存策略
```java
@Cacheable(value = "users", key = "#id")
public User findById(Long id) {
    return userRepository.findById(id);
}

@CacheEvict(value = "users", key = "#user.id")
public void updateUser(User user) {
    userRepository.save(user);
}
```

### 10.3 异步处理
```java
@Async("taskExecutor")
public CompletableFuture<Void> sendEmailAsync(String email, String content) {
    // 发送邮件逻辑
    return CompletableFuture.completedFuture(null);
}
```

## 代码检查清单

### 基础检查
- [ ] 所有public方法都有JavaDoc注释
- [ ] 所有异常都有适当的处理
- [ ] 日志记录遵循规范
- [ ] 变量命名符合规范
- [ ] 没有硬编码的魔法数字

### DDD检查
- [ ] 聚合根正确实现
- [ ] 值对象是不可变的
- [ ] 领域事件正确发布
- [ ] 仓储模式正确实现
- [ ] 业务逻辑在领域层

### 性能检查
- [ ] 数据库查询有适当的索引
- [ ] 避免N+1查询问题
- [ ] 使用适当的缓存策略
- [ ] 大数据量操作使用分页

### 安全检查
- [ ] 输入验证完整
- [ ] SQL注入防护
- [ ] 敏感信息加密
- [ ] 访问权限控制
- [ ] 审计日志记录