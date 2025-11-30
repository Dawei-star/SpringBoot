# 清理Git历史中的敏感信息

## 🔴 问题原因

虽然我们已经修改了当前代码，但**Git历史记录**中仍然包含敏感信息（commit `c07801f`）。GitHub会扫描所有要推送的commit，包括历史记录。

## ✅ 解决方案

### 方案1：完全重置仓库（推荐，如果这是本地仓库）

如果这是本地仓库，还没有成功推送过，可以完全重置：

```powershell
# 1. 中止当前的rebase
git rebase --abort

# 2. 删除.git目录，重新初始化
Remove-Item -Recurse -Force .git

# 3. 重新初始化Git
git init

# 4. 添加所有文件
git add .

# 5. 创建新的初始提交（不包含敏感信息）
git commit -m "feat: 初始提交 - BigEvent博客管理系统

- 完成前后端基础功能
- 实现用户认证和权限管理
- 实现文章、分类、留言等功能
- 添加安全防护措施
- 所有敏感信息使用环境变量配置"

# 6. 添加远程仓库
git remote add origin https://github.com/Dawei-star/SpringBoot.git

# 7. 推送到GitHub
git branch -M main
git push -u origin main --force
```

### 方案2：使用git filter-branch清理历史（如果已推送过）

如果仓库已经推送过，需要清理历史记录：

```powershell
# 1. 中止当前的rebase
git rebase --abort

# 2. 使用filter-branch从历史中移除敏感文件
git filter-branch --force --index-filter \
  "git rm --cached --ignore-unmatch big-event-backend/src/main/java/com/itheima/bigevent/utils/AliOssUtil.java" \
  --prune-empty --tag-name-filter cat -- --all

# 3. 强制垃圾回收
git for-each-ref --format="delete %(refname)" refs/original | git update-ref --stdin
git reflog expire --expire=now --all
git gc --prune=now --aggressive

# 4. 强制推送（会覆盖远程历史）
git push origin main --force
```

### 方案3：使用BFG Repo-Cleaner（最快，推荐）

1. 下载BFG：https://rtyley.github.io/bfg-repo-cleaner/

2. 创建替换文件 `replacements.txt`：
```
LTAI*==>REMOVED
your_access_key==>REMOVED
your_secret_key==>REMOVED
```

3. 运行清理：
```powershell
# 中止rebase
git rebase --abort

# 克隆一个裸仓库
git clone --mirror . ../BigEvent-clean.git

# 运行BFG
java -jar bfg.jar --replace-text replacements.txt ../BigEvent-clean.git

# 清理
cd ../BigEvent-clean.git
git reflog expire --expire=now --all
git gc --prune=now --aggressive

# 推送到远程
git push origin main --force
```

## 🚀 快速修复（最简单的方法）

如果你确定这是本地仓库，最简单的方法是：

```powershell
# 1. 中止rebase
git rebase --abort

# 2. 检查是否有未提交的更改
git status

# 3. 备份当前代码（可选）
# 复制整个项目文件夹到另一个位置

# 4. 删除.git，重新开始
Remove-Item -Recurse -Force .git

# 5. 重新初始化
git init
git add .
git commit -m "feat: 初始提交 - BigEvent博客管理系统"

# 6. 添加远程并推送
git remote add origin https://github.com/Dawei-star/SpringBoot.git
git branch -M main
git push -u origin main --force
```

## ⚠️ 重要提示

1. **使用 `--force` 推送会覆盖远程历史**，确保：
   - 这是你的个人仓库，或者
   - 团队其他成员知道你要重写历史

2. **如果已经推送过**，其他开发者需要：
   ```powershell
   git fetch origin
   git reset --hard origin/main
   ```

3. **备份重要数据**，以防万一

## ✅ 验证修复

推送成功后，检查：

1. ✅ GitHub不再显示敏感信息警告
2. ✅ 代码中所有敏感信息都使用环境变量
3. ✅ `.gitignore` 正确配置
4. ✅ 配置文件示例文件已更新

---

**推荐方案：** 如果这是本地仓库，使用方案1（完全重置）最简单快捷。

