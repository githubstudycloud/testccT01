#!/bin/bash

echo "=== Git 配置和推送脚本 ==="

# 配置代理
echo "1. 配置Git代理..."
git config --global http.proxy http://192.168.0.98:8800
git config --global https.proxy http://192.168.0.98:8800

# 配置用户信息
echo "2. 配置Git用户信息..."
git config user.name "Platform Developer"
git config user.email "developer@platform.com"

# 配置远程仓库
echo "3. 配置远程仓库..."
git remote remove origin 2>/dev/null || true
git remote add origin https://github.com/githubstudycloud/testccT01.git

# 检查当前分支
current_branch=$(git branch --show-current)
echo "当前分支: $current_branch"

# 推送代码
echo "4. 推送代码到远程仓库..."
echo "正在推送分支: $current_branch"

# 设置较长的超时时间
git config http.postBuffer 524288000
git config http.lowSpeedLimit 0
git config http.lowSpeedTime 999999

# 尝试推送
if git push -u origin $current_branch; then
    echo "✅ 代码推送成功!"
    echo "🌐 访问: https://github.com/githubstudycloud/testccT01/tree/$current_branch"
else
    echo "❌ 推送失败，请检查网络连接和代理设置"
    echo "💡 你也可以手动执行以下命令:"
    echo "   git push -u origin $current_branch"
fi

echo "=== Git 配置完成 ==="
echo ""
echo "📋 仓库信息:"
echo "- 远程仓库: https://github.com/githubstudycloud/testccT01.git"
echo "- 当前分支: $current_branch"
echo "- 提交数量: $(git rev-list --count HEAD)"
echo "- 文件数量: $(git ls-files | wc -l)"
echo ""
echo "🔧 如果推送失败，可以尝试:"
echo "1. 检查网络连接"
echo "2. 确认代理设置正确"
echo "3. 尝试使用 SSH 密钥认证"
echo "4. 联系管理员检查仓库权限"