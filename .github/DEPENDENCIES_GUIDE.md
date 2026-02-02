# 依赖管理方案：GitHub Release

## 方案说明

**不使用 Git LFS**（会让仓库变大），改用 **GitHub Release** 管理依赖文件。

## 优点

✅ **仓库轻量**：别人 clone 项目很快（不包含 433MB 依赖）  
✅ **CI/CD 正常**：构建时自动下载依赖  
✅ **无带宽限制**：不占用 LFS 配额  
✅ **专业管理**：依赖与代码分离

## 实施步骤

### 1. 打包依赖文件（已完成）

```bash
tar -czf dependencies.tar.gz app/src/main/assets/models app/src/main/assets/subpack app/src/main/jniLibs
```

生成的 `dependencies.tar.gz` 大约 **200-300MB**（压缩后）

### 2. 创建 GitHub Release 并上传

**在 GitHub 网页操作：**

1. 访问：`https://github.com/Crossfield-Labs/MetaAgent/releases/new`
2. 填写：
   - **Tag version**: `dependencies`
   - **Release title**: `依赖库文件`
   - **Description**: 
     ```
     项目构建所需的依赖库文件，包含：
     - AI 模型文件（语音识别、文本嵌入）
     - 原生库（视频处理、GIF、音频等）
     - 子包文件
     
     CI/CD 构建时会自动下载此文件。
     ```
3. 上传文件：拖动 `dependencies.tar.gz` 到附件区
4. 点击 **Publish release**

### 3. 提交修改的配置

```bash
git add .github/workflows/android-build.yml
git commit -m "ci: 改用 GitHub Release 管理依赖文件"
git push origin main
```

## CI/CD 工作流程

每次构建时：
1. 检出代码
2. **自动下载** `dependencies.tar.gz` 从 Release
3. 解压到对应目录
4. 正常编译构建

## 注意事项

- Release tag 必须是 `dependencies`（workflow 中硬编码）
- 如果依赖文件更新，需要重新上传到同一个 Release
- 初次上传后，CI/CD 就能正常工作了
