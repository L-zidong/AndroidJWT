# 在已连接设备/模拟器且 App 运行到对应页面时执行

# 手机主界面
adb exec-out screencap -p > screenshots/phone_main.png

# 进入某天后：详情页
adb exec-out screencap -p > screenshots/phone_detail.png

# 平板模拟器（sw >= 720dp）双栏界面
adb exec-out screencap -p > screenshots/tablet_master_detail.png

# 提交前请确认 screenshots/ 下为真实运行截图，替换 README 中的展示图
