# CI/CD 配置说明

## 已配置功能

### 1. 自动构建 (每次 push 到 main)
- 自动编译 Debug APK
- 保存构建产物 30 天
- 可在 GitHub Actions 页面下载

### 2. 自动发布 (创建版本标签时)
- 创建 Git Tag 如 `v1.0.0` 时触发
- 自动编译 Release APK
- 自动创建 GitHub Release
- 自动上传 APK 到 Release

## ⚠️ 重要：依赖库问题

项目需要的依赖库（从 Google Drive 下载的文件）GitHub Actions 无法自动获取。

### 解决方案 A：使用 Git LFS（推荐）

将依赖库文件提交到仓库，使用 Git LFS 管理大文件：

```bash
# 安装 Git LFS
git lfs install

# 追踪大文件
git lfs track "app/src/main/assets/models/**"
git lfs track "app/src/main/assets/subpack/**"
git lfs track "app/src/main/jniLibs/**"

# 提交
git add .gitattributes
git add app/src/main/assets/models app/src/main/assets/subpack app/src/main/jniLibs
git commit -m "chore: 添加依赖库文件到 Git LFS"
git push
```

### 解决方案 B：使用 GitHub Secrets

将依赖库上传到其他可访问的地方（如 GitHub Release），然后在 workflow 中下载。

## 配置 GitHub Secrets

需要在仓库设置中添加以下 Secrets：

1. `GITHUB_CLIENT_ID` - GitHub OAuth App 客户端 ID
2. `GITHUB_CLIENT_SECRET` - GitHub OAuth App 密钥（可选）

**路径**: GitHub 仓库 → Settings → Secrets and variables → Actions → New repository secret

## 如何使用

### 自动构建
每次 push 代码到 main 分支，会自动触发构建。

查看构建结果：
- 访问仓库的 **Actions** 标签
- 点击最新的 workflow run
- 下载构建的 APK

### 发布新版本
```bash
# 创建并推送版本标签
git tag v1.0.0
git push origin v1.0.0
```

会自动：
1. 构建 Release APK
2. 创建 GitHub Release
3. 上传 APK 到 Release

## 注意事项

- 第一次运行可能会失败（缺少依赖库），需要先解决依赖库问题
- Release APK 需要签名，目前使用 debug 签名
- 如需生产签名，需配置 keystore 相关 Secrets
