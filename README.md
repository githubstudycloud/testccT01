# Platform DDD 微服务框架

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.1-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/)
[![Maven](https://img.shields.io/badge/Maven-3.8+-red.svg)](https://maven.apache.org/)
[![Docker](https://img.shields.io/badge/Docker-20.10+-blue.svg)](https://www.docker.com/)

基于Spring Boot 3.4.1和Java 21构建的企业级DDD（领域驱动设计）微服务框架。

## 🏗️ 项目架构

```
platform/
├── platform-shared-kernel/     # 共享内核
├── platform-config-center/     # 配置中心
├── platform-gateway/          # API网关
├── platform-http-util/        # HTTP工具服务
├── platform-external-api/     # 外部API服务
├── platform-user-service/     # 用户服务
├── nginx/                     # Nginx配置
└── docker-compose.yml         # Docker编排
```

## ✨ 核心特性

### 🎯 DDD架构
- **聚合根模式**: 完整的聚合根实现，管理业务不变性
- **值对象**: Email、Phone、Money等业务值对象
- **领域事件**: 事件驱动的业务流程
- **仓储模式**: 领域层与基础设施层解耦
- **规格模式**: 复杂查询条件封装

### 🚀 微服务支持
- **配置中心**: Spring Cloud Config统一配置管理
- **API网关**: Spring Cloud Gateway路由和过滤
- **服务发现**: 支持多种注册中心
- **熔断降级**: Resilience4j容错处理
- **分布式缓存**: Redis支持

### 🛡️ 代码质量
- **统一规范**: 详细的开发规范文档
- **代码检查**: Checkstyle、PMD、SpotBugs
- **IDE配置**: IntelliJ IDEA完整配置
- **异常处理**: 分层异常体系
- **日志管理**: 结构化日志输出

## 🚀 快速开始

### 环境要求
- JDK 21+
- Maven 3.8+
- Docker 20.10+
- Docker Compose 2.0+

### 启动步骤

1. **克隆项目**
   ```bash
   git clone https://github.com/githubstudycloud/testccT01.git
   cd testccT01
   git checkout platform-ddd-framework
   ```

2. **启动基础设施**
   ```bash
   docker-compose up -d mysql redis rabbitmq
   ```

3. **启动服务**
   ```bash
   # 配置中心
   cd platform-config-center
   mvn spring-boot:run &

   # API网关
   cd ../platform-gateway
   mvn spring-boot:run &

   # 用户服务
   cd ../platform-user-service
   mvn spring-boot:run &

   # 外部API服务
   cd ../platform-external-api
   mvn spring-boot:run &

   # HTTP工具服务
   cd ../platform-http-util
   mvn spring-boot:run &
   ```

4. **验证服务**
   - 配置中心: http://localhost:8888/actuator/health
   - API网关: http://localhost:9000/actuator/health
   - 用户服务: http://localhost:8001/actuator/health

## 📋 服务端口

| 服务 | 端口 | 描述 |
|------|------|------|
| 配置中心 | 8888 | Spring Cloud Config Server |
| API网关 | 9000 | Spring Cloud Gateway |
| 用户服务 | 8001 | 用户管理服务 |
| 外部API | 8002 | 第三方API集成服务 |
| HTTP工具 | 8003 | HTTP客户端工具服务 |
| MySQL | 3306 | 关系型数据库 |
| Redis | 6379 | 缓存数据库 |
| RabbitMQ | 5672/15672 | 消息队列 |

## 🛠️ 技术栈

### 核心框架
- **Spring Boot**: 3.4.1
- **Spring Cloud**: 2024.0.1
- **Spring Cloud Alibaba**: 2023.0.3.2
- **Spring Security**: 6.x

### 数据存储
- **MySQL**: 8.0
- **Redis**: 7.2
- **RabbitMQ**: 3.12

### 开发工具
- **Maven**: 构建工具
- **Docker**: 容器化
- **Nginx**: 反向代理
- **Lombok**: 代码简化

### 代码质量
- **Checkstyle**: 代码风格检查
- **PMD**: 代码质量分析
- **SpotBugs**: Bug检测
- **JaCoCo**: 代码覆盖率

## 📖 文档

### 开发文档
- [开发规范](DEVELOPMENT_RULES.md) - 完整的开发规范和最佳实践
- [使用指南](USAGE_GUIDE.md) - 详细的使用说明和配置指南
- [错误检查](ERROR_CHECK_SCRIPTS.md) - 自动化错误检查工具
- [Git配置](GIT_CONFIGURATION.md) - Git仓库配置指南

### API文档
- Swagger UI: http://localhost:8001/swagger-ui.html (用户服务)
- Swagger UI: http://localhost:8002/swagger-ui.html (外部API服务)

## 🏛️ DDD架构层次

### 领域层 (Domain Layer)
- **聚合根**: 业务实体的根，管理业务不变性
- **实体**: 有唯一标识的业务对象
- **值对象**: 无唯一标识的不可变对象
- **领域服务**: 跨聚合的业务逻辑
- **仓储接口**: 数据持久化抽象

### 应用层 (Application Layer)
- **应用服务**: 协调领域对象完成业务用例
- **命令对象**: 封装用户请求
- **查询对象**: 封装查询请求
- **事件处理**: 处理领域事件

### 基础设施层 (Infrastructure Layer)
- **仓储实现**: 数据持久化具体实现
- **外部服务**: 第三方服务集成
- **配置管理**: 系统配置
- **消息发布**: 事件发布机制

### 接口层 (Interface Layer)
- **REST控制器**: HTTP API接口
- **DTO对象**: 数据传输对象
- **异常处理**: 全局异常处理
- **参数验证**: 输入参数验证

## 🔧 开发配置

### IDE配置
1. **IntelliJ IDEA**
   - 导入代码风格: `.idea/codeStyles/Project.xml`
   - 导入检查规则: `.idea/inspectionProfiles/Platform_Code_Style.xml`

2. **VS Code**
   - 安装Java扩展包
   - 配置Checkstyle插件

### 代码检查
```bash
# 编译检查
mvn clean compile

# 代码风格检查
mvn checkstyle:check

# 运行测试
mvn test

# 完整检查
mvn clean install
```

## 🚢 部署

### Docker部署
```bash
# 构建镜像
docker-compose build

# 启动所有服务
docker-compose up -d

# 查看服务状态
docker-compose ps
```

### 生产环境
1. **配置环境变量**
2. **设置数据库连接**
3. **配置Redis集群**
4. **设置消息队列**
5. **启动服务监控**

## 🤝 贡献指南

1. Fork 项目
2. 创建功能分支: `git checkout -b feature/AmazingFeature`
3. 提交更改: `git commit -m 'Add some AmazingFeature'`
4. 推送分支: `git push origin feature/AmazingFeature`
5. 创建 Pull Request

### 提交规范
```
feat: 新功能
fix: 修复Bug
docs: 文档更新
style: 代码格式
refactor: 重构
test: 测试
chore: 构建过程或辅助工具的变动
```

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情

## 👥 团队

- **架构师**: Platform Team
- **开发者**: Claude Code Assistant

## 📞 联系我们

- 项目地址: https://github.com/githubstudycloud/testccT01
- 问题反馈: [Issues](https://github.com/githubstudycloud/testccT01/issues)

---

🤖 **Generated with [Claude Code](https://claude.ai/code)**

**Co-Authored-By: Claude <noreply@anthropic.com>**