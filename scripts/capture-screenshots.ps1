# 在已连接设备/模拟器且 App 运行到对应页面时执行（PowerShell）

# 手机主界面
adb exec-out screencap -p | Set-Content -Encoding Byte screenshots/phone_main.png

# 详情页（先手动进入详情再执行）
# adb exec-out screencap -p | Set-Content -Encoding Byte screenshots/phone_detail.png

# 平板双栏（需 sw>=720dp 模拟器）
# adb exec-out screencap -p | Set-Content -Encoding Byte screenshots/tablet_master_detail.png

Write-Host "截图已保存到 screenshots/，请替换 README 中的展示图后提交。"
