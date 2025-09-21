# Platform 错误检查脚本和工具

## 1. 自动化检查脚本

### Maven 构建检查脚本
创建 `check-build.sh`:
```bash
#!/bin/bash

echo "=== Platform项目构建检查 ==="

# 检查Java环境
if ! command -v java &> /dev/null; then
    echo "❌ Java未安装或未配置到PATH"
    exit 1
fi

if ! command -v mvn &> /dev/null; then
    echo "❌ Maven未安装或未配置到PATH"
    exit 1
fi

echo "✅ Java和Maven环境检查通过"

# 清理并编译
echo "🔄 清理项目..."
mvn clean -q

echo "🔄 编译项目..."
if mvn compile -q; then
    echo "✅ 编译成功"
else
    echo "❌ 编译失败，请检查代码错误"
    exit 1
fi

# 运行测试
echo "🔄 运行测试..."
if mvn test -q; then
    echo "✅ 测试通过"
else
    echo "⚠️ 测试失败，请检查测试用例"
fi

# 代码质量检查
echo "🔄 代码质量检查..."
if mvn checkstyle:check -q; then
    echo "✅ Checkstyle检查通过"
else
    echo "⚠️ Checkstyle检查发现问题"
fi

echo "=== 构建检查完成 ==="
```

### 依赖检查脚本
创建 `check-dependencies.sh`:
```bash
#!/bin/bash

echo "=== 依赖检查 ==="

# 检查Maven依赖
echo "🔄 检查Maven依赖..."
mvn dependency:tree > dependency-tree.txt

echo "🔄 检查依赖冲突..."
if mvn dependency:analyze -q | grep -q "WARNING"; then
    echo "⚠️ 发现依赖问题，请查看详细报告"
    mvn dependency:analyze
else
    echo "✅ 依赖检查通过"
fi

# 检查安全漏洞
echo "🔄 检查安全漏洞..."
if command -v mvn &> /dev/null; then
    mvn org.owasp:dependency-check-maven:check -q
    if [ -f target/dependency-check-report.html ]; then
        echo "📄 安全报告已生成: target/dependency-check-report.html"
    fi
fi

echo "=== 依赖检查完成 ==="
```

### 代码风格检查脚本
创建 `check-style.sh`:
```bash
#!/bin/bash

echo "=== 代码风格检查 ==="

# Checkstyle检查
echo "🔄 运行Checkstyle..."
mvn checkstyle:check
checkstyle_exit_code=$?

# SpotBugs检查
echo "🔄 运行SpotBugs..."
mvn spotbugs:check
spotbugs_exit_code=$?

# PMD检查
echo "🔄 运行PMD..."
mvn pmd:check
pmd_exit_code=$?

# 汇总结果
echo "=== 检查结果汇总 ==="
if [ $checkstyle_exit_code -eq 0 ]; then
    echo "✅ Checkstyle: 通过"
else
    echo "❌ Checkstyle: 失败"
fi

if [ $spotbugs_exit_code -eq 0 ]; then
    echo "✅ SpotBugs: 通过"
else
    echo "❌ SpotBugs: 失败"
fi

if [ $pmd_exit_code -eq 0 ]; then
    echo "✅ PMD: 通过"
else
    echo "❌ PMD: 失败"
fi
```

## 2. IDE 配置导入指南

### IntelliJ IDEA 配置导入
1. **代码风格导入**:
   - File → Settings → Editor → Code Style
   - 点击设置图标 → Import Scheme → IntelliJ IDEA code style XML
   - 选择 `.idea/codeStyles/Project.xml`

2. **检查规则导入**:
   - File → Settings → Editor → Inspections
   - 点击设置图标 → Import Profile
   - 选择 `.idea/inspectionProfiles/Platform_Code_Style.xml`

3. **Checkstyle 插件配置**:
   ```
   1. 安装 CheckStyle-IDEA 插件
   2. File → Settings → Tools → Checkstyle
   3. 添加配置文件: checkstyle.xml
   4. 设置为 Active
   ```

### VS Code 配置导入
创建 `.vscode/settings.json`:
```json
{
  "java.configuration.updateBuildConfiguration": "automatic",
  "java.compile.nullAnalysis.mode": "automatic",
  "java.format.settings.url": "./checkstyle.xml",
  "sonarlint.connectedMode.project": {
    "connectionId": "platform-sonar",
    "projectKey": "platform"
  },
  "editor.formatOnSave": true,
  "editor.codeActionsOnSave": {
    "source.organizeImports": true
  }
}
```

创建 `.vscode/extensions.json`:
```json
{
  "recommendations": [
    "vscjava.vscode-java-pack",
    "redhat.java",
    "vscjava.vscode-spring-boot",
    "sonarsource.sonarlint-vscode",
    "shengchen.vscode-checkstyle"
  ]
}
```

## 3. Maven 插件配置

### 在父POM中添加插件
```xml
<build>
    <plugins>
        <!-- Checkstyle -->
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-checkstyle-plugin</artifactId>
            <version>3.3.1</version>
            <configuration>
                <configLocation>checkstyle.xml</configLocation>
                <suppressionsLocation>checkstyle-suppressions.xml</suppressionsLocation>
                <encoding>UTF-8</encoding>
                <consoleOutput>true</consoleOutput>
                <failsOnError>true</failsOnError>
            </configuration>
            <executions>
                <execution>
                    <id>validate</id>
                    <phase>validate</phase>
                    <goals>
                        <goal>check</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>

        <!-- SpotBugs -->
        <plugin>
            <groupId>com.github.spotbugs</groupId>
            <artifactId>spotbugs-maven-plugin</artifactId>
            <version>4.8.2.0</version>
            <configuration>
                <excludeFilterFile>spotbugs-exclude.xml</excludeFilterFile>
                <effort>Max</effort>
                <threshold>Low</threshold>
            </configuration>
            <executions>
                <execution>
                    <goals>
                        <goal>check</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>

        <!-- PMD -->
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-pmd-plugin</artifactId>
            <version>3.21.2</version>
            <configuration>
                <rulesets>
                    <ruleset>pmd.xml</ruleset>
                </rulesets>
                <printFailingErrors>true</printFailingErrors>
            </configuration>
            <executions>
                <execution>
                    <goals>
                        <goal>check</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>

        <!-- JaCoCo 代码覆盖率 -->
        <plugin>
            <groupId>org.jacoco</groupId>
            <artifactId>jacoco-maven-plugin</artifactId>
            <version>0.8.11</version>
            <executions>
                <execution>
                    <goals>
                        <goal>prepare-agent</goal>
                    </goals>
                </execution>
                <execution>
                    <id>report</id>
                    <phase>test</phase>
                    <goals>
                        <goal>report</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

## 4. 持续集成配置

### GitHub Actions 配置
创建 `.github/workflows/ci.yml`:
```yaml
name: CI

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  test:
    runs-on: ubuntu-latest

    services:
      mysql:
        image: mysql:8.0
        env:
          MYSQL_ROOT_PASSWORD: root123
          MYSQL_DATABASE: platform
        ports:
          - 3306:3306
        options: >-
          --health-cmd="mysqladmin ping"
          --health-interval=10s
          --health-timeout=5s
          --health-retries=3

      redis:
        image: redis:7.2
        ports:
          - 6379:6379
        options: >-
          --health-cmd="redis-cli ping"
          --health-interval=10s
          --health-timeout=5s
          --health-retries=5

    steps:
    - uses: actions/checkout@v4

    - name: Set up JDK 21
      uses: actions/setup-java@v4
      with:
        java-version: '21'
        distribution: 'temurin'

    - name: Cache Maven packages
      uses: actions/cache@v3
      with:
        path: ~/.m2
        key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}
        restore-keys: ${{ runner.os }}-m2

    - name: Run tests
      run: mvn clean test

    - name: Run Checkstyle
      run: mvn checkstyle:check

    - name: Run SpotBugs
      run: mvn spotbugs:check

    - name: Generate test report
      uses: dorny/test-reporter@v1
      if: success() || failure()
      with:
        name: Maven Tests
        path: '**/target/surefire-reports/*.xml'
        reporter: java-junit

    - name: Upload coverage to Codecov
      uses: codecov/codecov-action@v3
      with:
        file: ./target/site/jacoco/jacoco.xml
```

## 5. 常见错误及解决方案

### 编译错误
```bash
# 1. 包导入错误
错误: package com.platform.shared.domain does not exist
解决: 确保 platform-shared-kernel 模块已正确编译和安装

# 2. 版本冲突
错误: class file has wrong version
解决: 检查Java版本，确保使用JDK 21

# 3. 注解处理器错误
错误: annotation processor 'lombok.launch.AnnotationProcessorHider$AnnotationProcessor' not found
解决: 确保Lombok依赖正确配置
```

### 运行时错误
```bash
# 1. 数据库连接失败
错误: Communications link failure
解决: 检查数据库服务状态和连接配置

# 2. 端口占用
错误: Port 8080 was already in use
解决: netstat -tlnp | grep :8080 查找占用进程并终止

# 3. Bean创建失败
错误: Error creating bean with name 'userRepository'
解决: 检查@ComponentScan和@EnableJpaRepositories配置
```

## 6. 代码质量检查清单

### 提交前检查清单
- [ ] 代码编译通过: `mvn compile`
- [ ] 测试全部通过: `mvn test`
- [ ] Checkstyle检查通过: `mvn checkstyle:check`
- [ ] SpotBugs检查通过: `mvn spotbugs:check`
- [ ] 代码覆盖率满足要求: `mvn jacoco:report`
- [ ] 所有public方法有JavaDoc注释
- [ ] 异常处理完整
- [ ] 日志记录适当
- [ ] 安全漏洞检查通过

### 代码审查检查清单
- [ ] DDD模式正确实现
- [ ] 业务逻辑在正确的层次
- [ ] 数据库事务正确处理
- [ ] 输入验证完整
- [ ] 错误处理恰当
- [ ] 性能考虑充分
- [ ] 可测试性良好
- [ ] 文档完整准确

## 7. 性能和监控

### JVM 监控参数
```bash
JAVA_OPTS="-XX:+UseG1GC \
  -XX:+UseContainerSupport \
  -XX:MaxRAMPercentage=75.0 \
  -XX:+HeapDumpOnOutOfMemoryError \
  -XX:HeapDumpPath=/tmp/heapdump.hprof \
  -XX:+ExitOnOutOfMemoryError \
  -Dcom.sun.management.jmxremote \
  -Dcom.sun.management.jmxremote.port=9999 \
  -Dcom.sun.management.jmxremote.authenticate=false \
  -Dcom.sun.management.jmxremote.ssl=false"
```

### 应用监控配置
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always
  metrics:
    export:
      prometheus:
        enabled: true
```

这些工具和脚本将帮助确保代码质量并及早发现问题。