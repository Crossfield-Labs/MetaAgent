# Git LFS 提交指南

## 已完成的配置

✅ 已安装 Git LFS  
✅ 已配置追踪规则（`.gitattributes`）  
✅ 已更新 GitHub Actions 支持 LFS

## 接下来的步骤

### 1. 提交 Git LFS 配置和依赖文件

这些文件会被提交（约 433MB）：
- `app/src/main/assets/models/` - AI 模型文件（254MB）
- `app/src/main/assets/subpack/` - 子包文件（47MB）
- `app/src/main/jniLibs/` - 原生库（132MB）

**执行命令：**

```bash
# 1. 添加 .gitattributes 配置
git add .gitattributes

# 2. 添加依赖文件（会自动用 LFS 管理）
git add app/src/main/assets/models app/src/main/assets/subpack app/src/main/jniLibs

# 3. 添加更新的 workflow
git add .github/

# 4. 提交
git commit -m "chore: 配置 Git LFS 并添加依赖库文件"

# 5. 推送（这一步会比较慢，因为要上传 433MB）
git push origin main
```

### 2. 验证 LFS 是否工作

推送完成后，可以检查：

```bash
# 查看哪些文件被 LFS 管理
git lfs ls-files
```

在 GitHub 仓库页面，LFS 文件会显示为"Stored with Git LFS"。

## ⚠️ 注意事项

1. **首次推送很慢**：433MB 文件需要上传，可能需要 5-15 分钟（取决于网络）
2. **GitHub LFS 免费额度**：
   - 免费用户：1GB 存储 + 1GB/月 带宽
   - 你的文件：433MB（还在额度内）
3. **如果推送失败**：可能是网络问题，重试即可

## 完成后

CI/CD 就能正常工作了！每次 push 代码，GitHub Actions 会：
1. 自动检出代码和 LFS 文件
2. 编译完整的 APK
3. 上传构建产物

需要我帮你执行这些命令吗？还是你自己来？
