# Platform 使用指南

## 目录
- [1. 快速开始](#1-快速开始)
- [2. 开发环境配置](#2-开发环境配置)
- [3. IDE配置导入](#3-ide配置导入)
- [4. 项目构建与运行](#4-项目构建与运行)
- [5. 服务部署](#5-服务部署)
- [6. 开发工作流](#6-开发工作流)
- [7. 常见问题](#7-常见问题)
- [8. 监控与运维](#8-监控与运维)

## 1. 快速开始

### 1.1 环境要求
- **JDK**: 21+
- **Maven**: 3.8+
- **Docker**: 20.10+
- **Docker Compose**: 2.0+
- **Node.js**: 18+ (可选，用于前端开发)

### 1.2 克隆项目
```bash
git clone <repository-url>
cd platform
```

### 1.3 一键启动
```bash
# 启动基础设施服务
docker-compose up -d mysql redis rabbitmq

# 等待服务启动完成
sleep 30

# 启动平台服务
./start-all.sh
```

### 1.4 验证部署
访问以下地址验证服务状态：
- **API网关**: http://localhost:9000/actuator/health
- **配置中心**: http://localhost:8888/actuator/health
- **用户服务**: http://localhost:8001/actuator/health
- **外部API**: http://localhost:8002/actuator/health
- **HTTP工具**: http://localhost:8003/actuator/health

## 2. 开发环境配置

### 2.1 IntelliJ IDEA配置

#### 导入代码规范
1. 打开 IDEA -> File -> Settings -> Editor -> Code Style
2. 点击设置图标 -> Import Scheme -> IntelliJ IDEA code style XML
3. 选择项目根目录下的 `.idea/codeStyles/Project.xml`

#### 导入检查规则
1. 打开 IDEA -> File -> Settings -> Editor -> Inspections
2. 点击设置图标 -> Import Profile
3. 选择项目根目录下的 `.idea/inspectionProfiles/Platform_Code_Style.xml`

#### 配置Checkstyle插件
1. 安装 CheckStyle-IDEA 插件
2. File -> Settings -> Tools -> Checkstyle
3. 添加配置文件：点击 "+" -> 选择项目根目录下的 `checkstyle.xml`
4. 设置为 Active

#### 配置SonarLint插件
1. 安装 SonarLint 插件
2. 连接到 SonarQube 服务器（如果有）
3. 绑定项目进行实时代码质量检查

### 2.2 VS Code配置

#### 安装扩展
```bash
# Java相关
code --install-extension vscjava.vscode-java-pack
code --install-extension redhat.java
code --install-extension vscjava.vscode-spring-boot

# 代码质量
code --install-extension sonarsource.sonarlint-vscode
code --install-extension shengchen.vscode-checkstyle
```

#### 配置文件
创建 `.vscode/settings.json`:
```json
{
  "java.configuration.updateBuildConfiguration": "automatic",
  "java.compile.nullAnalysis.mode": "automatic",
  "java.format.settings.url": "./checkstyle.xml",
  "sonarlint.connectedMode.project": {
    "connectionId": "platform-sonar",
    "projectKey": "platform"
  }
}
```

### 2.3 Maven配置

#### settings.xml配置
```xml
<settings>
  <profiles>
    <profile>
      <id>platform-dev</id>
      <properties>
        <spring.profiles.active>dev</spring.profiles.active>
        <checkstyle.skip>false</checkstyle.skip>
        <spotbugs.skip>false</spotbugs.skip>
      </properties>
    </profile>
  </profiles>

  <activeProfiles>
    <activeProfile>platform-dev</activeProfile>
  </activeProfiles>
</settings>
```

## 3. IDE配置导入

### 3.1 IntelliJ IDEA完整配置

#### 自动导入配置脚本
创建 `setup-idea.sh`:
```bash
#!/bin/bash

# 创建IDEA配置目录
mkdir -p .idea/inspectionProfiles
mkdir -p .idea/codeStyles

# 设置项目配置
echo "正在配置IntelliJ IDEA..."

# 导入代码风格
cp .idea/codeStyles/Project.xml ~/.IntelliJIdea2023.3/config/codestyles/

# 导入检查规则
cp .idea/inspectionProfiles/Platform_Code_Style.xml ~/.IntelliJIdea2023.3/config/inspection/

echo "IDEA配置完成！请重启IDEA并选择Platform_Code_Style配置。"
```

#### 项目特定配置
`.idea/workspace.xml`:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project version="4">
  <component name="PropertiesComponent">
    <property name="settings.editor.selected.configurable" value="preferences.lookFeel" />
    <property name="project.structure.last.edited" value="Modules" />
    <property name="project.structure.proportion" value="0.15" />
    <property name="project.structure.side.proportion" value="0.2" />
  </component>

  <component name="RunManager">
    <configuration name="All Services" type="CompoundRunConfigurationType">
      <toRun name="ConfigCenterApplication" type="SpringBootApplicationConfigurationType" />
      <toRun name="GatewayApplication" type="SpringBootApplicationConfigurationType" />
      <toRun name="UserServiceApplication" type="SpringBootApplicationConfigurationType" />
      <toRun name="ExternalApiApplication" type="SpringBootApplicationConfigurationType" />
      <toRun name="HttpUtilApplication" type="SpringBootApplicationConfigurationType" />
    </configuration>
  </component>
</project>
```

### 3.2 代码质量检查配置

#### PMD配置
`pmd.xml`:
```xml
<?xml version="1.0"?>
<ruleset name="Platform PMD Rules">
    <description>Platform项目PMD规则</description>

    <rule ref="category/java/bestpractices.xml"/>
    <rule ref="category/java/codestyle.xml"/>
    <rule ref="category/java/design.xml"/>
    <rule ref="category/java/errorprone.xml"/>
    <rule ref="category/java/performance.xml"/>
    <rule ref="category/java/security.xml"/>

    <!-- 排除规则 -->
    <rule ref="category/java/codestyle.xml/LongVariable">
        <properties>
            <property name="minimum" value="25"/>
        </properties>
    </rule>
</ruleset>
```

#### SpotBugs配置
`spotbugs-exclude.xml`:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<FindBugsFilter>
    <!-- 排除测试代码 -->
    <Match>
        <Class name="~.*Test$"/>
    </Match>

    <!-- 排除生成的代码 -->
    <Match>
        <Package name="~.*\.generated\..*"/>
    </Match>

    <!-- 排除Lombok生成的代码 -->
    <Match>
        <Bug pattern="EI_EXPOSE_REP,EI_EXPOSE_REP2"/>
        <Class name="~.*\.(Entity|DTO|VO)$"/>
    </Match>
</FindBugsFilter>
```

## 4. 项目构建与运行

### 4.1 构建脚本

#### 完整构建脚本
`build.sh`:
```bash
#!/bin/bash

set -e

echo "=== Platform项目构建开始 ==="

# 清理旧的构建产物
echo "1. 清理项目..."
mvn clean

# 代码质量检查
echo "2. 执行代码质量检查..."
mvn checkstyle:check
mvn pmd:check
mvn spotbugs:check

# 运行测试
echo "3. 运行测试..."
mvn test

# 构建项目
echo "4. 构建项目..."
mvn compile

# 打包
echo "5. 打包应用..."
mvn package -DskipTests

# 构建Docker镜像
echo "6. 构建Docker镜像..."
docker-compose build

echo "=== 构建完成 ==="
```

#### 快速构建脚本
`quick-build.sh`:
```bash
#!/bin/bash

echo "=== 快速构建开始 ==="

# 跳过测试的快速构建
mvn clean package -DskipTests -Dcheckstyle.skip=true

echo "=== 快速构建完成 ==="
```

### 4.2 启动脚本

#### 完整启动脚本
`start-all.sh`:
```bash
#!/bin/bash

set -e

# 检查Docker服务
if ! docker info > /dev/null 2>&1; then
    echo "错误: Docker服务未启动"
    exit 1
fi

echo "=== 启动Platform服务 ==="

# 启动基础设施
echo "1. 启动基础设施服务..."
docker-compose up -d mysql redis rabbitmq nginx

# 等待基础设施就绪
echo "2. 等待基础设施服务启动..."
sleep 30

# 启动配置中心
echo "3. 启动配置中心..."
cd platform-config-center
nohup mvn spring-boot:run > ../logs/config-center.log 2>&1 &
cd ..

# 等待配置中心启动
echo "4. 等待配置中心启动..."
sleep 20

# 启动网关
echo "5. 启动API网关..."
cd platform-gateway
nohup mvn spring-boot:run > ../logs/gateway.log 2>&1 &
cd ..

# 启动业务服务
echo "6. 启动业务服务..."
cd platform-user-service
nohup mvn spring-boot:run > ../logs/user-service.log 2>&1 &
cd ..

cd platform-external-api
nohup mvn spring-boot:run > ../logs/external-api.log 2>&1 &
cd ..

cd platform-http-util
nohup mvn spring-boot:run > ../logs/http-util.log 2>&1 &
cd ..

echo "7. 等待所有服务启动..."
sleep 30

echo "=== 服务启动完成 ==="
echo "请访问 http://localhost 查看服务状态"
```

#### 停止脚本
`stop-all.sh`:
```bash
#!/bin/bash

echo "=== 停止Platform服务 ==="

# 停止Java应用
pkill -f "spring-boot:run"

# 停止Docker服务
docker-compose down

echo "=== 所有服务已停止 ==="
```

### 4.3 开发模式启动

#### 开发模式脚本
`start-dev.sh`:
```bash
#!/bin/bash

# 启动开发环境
echo "=== 启动开发环境 ==="

# 仅启动基础设施
docker-compose up -d mysql redis rabbitmq

# 设置开发环境变量
export SPRING_PROFILES_ACTIVE=dev
export LOG_LEVEL=DEBUG

echo "基础设施已启动，请在IDE中启动各个服务"
echo "启动顺序："
echo "1. ConfigCenterApplication"
echo "2. GatewayApplication"
echo "3. UserServiceApplication"
echo "4. ExternalApiApplication"
echo "5. HttpUtilApplication"
```

## 5. 服务部署

### 5.1 Docker部署

#### 生产环境Docker Compose
`docker-compose.prod.yml`:
```yaml
version: '3.8'

services:
  # 配置中心
  config-center:
    image: platform/config-center:latest
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/platform
      - SPRING_REDIS_HOST=redis
    deploy:
      replicas: 2
      resources:
        limits:
          memory: 512M
        reservations:
          memory: 256M

  # API网关
  gateway:
    image: platform/gateway:latest
    ports:
      - "9000:9000"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - SPRING_CLOUD_CONFIG_URI=http://config-center:8888
    deploy:
      replicas: 3
      resources:
        limits:
          memory: 512M
        reservations:
          memory: 256M
    depends_on:
      - config-center
```

#### 健康检查配置
```yaml
healthcheck:
  test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
  interval: 30s
  timeout: 10s
  retries: 3
  start_period: 40s
```

### 5.2 Kubernetes部署

#### 配置中心部署
`k8s/config-center-deployment.yaml`:
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: config-center
  namespace: platform
spec:
  replicas: 2
  selector:
    matchLabels:
      app: config-center
  template:
    metadata:
      labels:
        app: config-center
    spec:
      containers:
      - name: config-center
        image: platform/config-center:latest
        ports:
        - containerPort: 8888
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "k8s"
        resources:
          requests:
            memory: "256Mi"
            cpu: "250m"
          limits:
            memory: "512Mi"
            cpu: "500m"
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8888
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8888
          initialDelaySeconds: 20
          periodSeconds: 5
---
apiVersion: v1
kind: Service
metadata:
  name: config-center-service
  namespace: platform
spec:
  selector:
    app: config-center
  ports:
  - protocol: TCP
    port: 8888
    targetPort: 8888
  type: ClusterIP
```

## 6. 开发工作流

### 6.1 Git工作流

#### 分支策略
```
main          ── 生产分支
├── develop   ── 开发分支
├── feature/* ── 功能分支
├── bugfix/*  ── 缺陷修复分支
└── release/* ── 发布分支
```

#### 提交规范
```bash
# 功能开发
git commit -m "feat(user): 添加用户注册功能"

# 缺陷修复
git commit -m "fix(auth): 修复token过期问题"

# 文档更新
git commit -m "docs(api): 更新API文档"

# 代码重构
git commit -m "refactor(service): 重构用户服务"

# 性能优化
git commit -m "perf(query): 优化用户查询性能"

# 测试相关
git commit -m "test(user): 添加用户服务单元测试"
```

### 6.2 代码审查流程

#### Pre-commit Hook
`.git/hooks/pre-commit`:
```bash
#!/bin/bash

echo "执行代码质量检查..."

# 运行Checkstyle
mvn checkstyle:check
if [ $? -ne 0 ]; then
    echo "Checkstyle检查失败，请修复代码风格问题"
    exit 1
fi

# 运行测试
mvn test
if [ $? -ne 0 ]; then
    echo "测试失败，请修复测试问题"
    exit 1
fi

echo "代码质量检查通过"
```

#### Pull Request模板
`.github/pull_request_template.md`:
```markdown
## 变更类型
- [ ] 新功能 (feature)
- [ ] 缺陷修复 (bugfix)
- [ ] 代码重构 (refactor)
- [ ] 文档更新 (docs)
- [ ] 性能优化 (perf)

## 变更描述
<!-- 详细描述本次变更的内容 -->

## 测试说明
<!-- 描述如何测试本次变更 -->

## 影响范围
<!-- 描述本次变更可能影响的模块或功能 -->

## 检查清单
- [ ] 代码已通过所有测试
- [ ] 代码已通过代码质量检查
- [ ] 已添加必要的测试用例
- [ ] 已更新相关文档
- [ ] 已验证向后兼容性
```

## 7. 常见问题

### 7.1 构建问题

#### 问题1: Maven依赖下载失败
```bash
# 解决方案：清理本地仓库并重新下载
rm -rf ~/.m2/repository
mvn clean install
```

#### 问题2: Docker构建失败
```bash
# 检查Docker服务状态
systemctl status docker

# 重启Docker服务
sudo systemctl restart docker

# 清理Docker缓存
docker system prune -a
```

### 7.2 运行时问题

#### 问题1: 端口冲突
```bash
# 查看端口占用
netstat -tlnp | grep :8080

# 杀死占用进程
kill -9 <PID>
```

#### 问题2: 数据库连接失败
```bash
# 检查数据库服务状态
docker logs platform-mysql

# 重启数据库服务
docker-compose restart mysql
```

### 7.3 性能问题

#### 问题1: 内存不足
```bash
# 增加JVM内存
export JAVA_OPTS="-Xms512m -Xmx1g"

# 或在application.yml中配置
spring:
  jpa:
    properties:
      hibernate:
        jdbc:
          batch_size: 20
```

#### 问题2: 响应时间慢
```bash
# 启用JVM性能分析
-XX:+FlightRecorder
-XX:StartFlightRecording=duration=60s,filename=app.jfr

# 分析GC日志
-XX:+PrintGC -XX:+PrintGCDetails
```

## 8. 监控与运维

### 8.1 应用监控

#### Micrometer + Prometheus配置
```yaml
management:
  endpoints:
    web:
      exposure:
        include: "*"
  endpoint:
    health:
      show-details: always
  metrics:
    export:
      prometheus:
        enabled: true
```

#### 自定义监控指标
```java
@Component
public class UserMetrics {

    private final Counter userRegistrations;
    private final Timer userLoginTime;

    public UserMetrics(MeterRegistry meterRegistry) {
        this.userRegistrations = Counter.builder("user.registrations")
            .description("用户注册总数")
            .register(meterRegistry);

        this.userLoginTime = Timer.builder("user.login.time")
            .description("用户登录耗时")
            .register(meterRegistry);
    }
}
```

### 8.2 日志管理

#### Logback配置
`logback-spring.xml`:
```xml
<configuration>
    <springProfile name="prod">
        <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
            <file>logs/platform.log</file>
            <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
                <fileNamePattern>logs/platform.%d{yyyy-MM-dd}.%i.log</fileNamePattern>
                <maxFileSize>100MB</maxFileSize>
                <maxHistory>30</maxHistory>
                <totalSizeCap>3GB</totalSizeCap>
            </rollingPolicy>
            <encoder>
                <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
            </encoder>
        </appender>
    </springProfile>
</configuration>
```

### 8.3 备份策略

#### 数据库备份脚本
`backup-db.sh`:
```bash
#!/bin/bash

DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_DIR="/backup/mysql"
DB_NAME="platform"

# 创建备份目录
mkdir -p $BACKUP_DIR

# 备份数据库
docker exec platform-mysql mysqldump -u root -proot123 $DB_NAME > $BACKUP_DIR/platform_$DATE.sql

# 压缩备份文件
gzip $BACKUP_DIR/platform_$DATE.sql

# 删除7天前的备份
find $BACKUP_DIR -name "*.sql.gz" -mtime +7 -delete

echo "数据库备份完成: platform_$DATE.sql.gz"
```

---

这份使用指南涵盖了从开发环境配置到生产部署的完整流程。开发者可以根据具体需求选择相应的部分进行实施。