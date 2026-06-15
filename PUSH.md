# 推送到 Gitee / GitHub

本地仓库已初始化并完成首次提交。按以下步骤创建远程仓库并推送。

## 1. 在 Gitee 或 GitHub 创建空仓库

- **Gitee**：https://gitee.com/projects/new → 仓库名 `WeatherForecast` → **不要**勾选「使用 Readme 初始化」
- **GitHub**：https://github.com/new → 同上

## 2. 关联远程并推送

在 `WeatherForecast` 目录下执行（将 `YOUR_USERNAME` 换成你的账号）：

### Gitee（国内访问快，推荐）

```powershell
cd "D:\WORK\mineru-output\移动应用开发-实验2\WeatherForecast"

git remote add origin https://github.com/L-zidong/AndroidJWT.git
git branch -M main
git push -u origin main
```

### GitHub

```powershell
git remote add origin https://github.com/YOUR_USERNAME/WeatherForecast.git
git branch -M main
git push -u origin main
```

## 3. 推送前检查

- [ ] `local.properties` 未被提交（含 SDK 路径、API 密钥）
- [ ] `qweather_private.pem` 未被提交
- [ ] 截图：建议用真机/模拟器截图替换 `screenshots/` 下当前演示图（见 `scripts/capture-screenshots.ps1`）

## 4. 更新简历

推送成功后，将仓库链接填入 [`简历-刘子东-Android实习.md`](../简历-刘子东-Android实习.md)：

```markdown
GitHub/Gitee：https://gitee.com/YOUR_USERNAME/WeatherForecast
```

## 5. 可选：替换为真实截图

1. Android Studio 运行 App 到主界面 / 详情 / 平板双栏
2. 执行 `scripts/capture-screenshots.ps1`（需配置 adb 到 PATH）
3. 提交并推送：

```powershell
git add screenshots/
git commit -m "docs: replace README screenshots with device captures"
git push
```
