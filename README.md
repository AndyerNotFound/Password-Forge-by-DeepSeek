# Password Forge

Made by DeepSeek **Password Generate and Checker**. 同一套功能逻辑，用三种技术栈实现，覆盖不同使用场景：

| 版本 | 语言 / 技术栈 | 形态 | 目录 | 运行环境 |
|------|--------------|------|------|---------|
| Web 版 | Python + Flask | Web 应用 | [`password-tool/`](password-tool/) | 任何能跑 Python 的设备 |
| 多平台版 | Kotlin + Compose Multiplatform | Android APK / 桌面 EXE·DEB | [`password-tool-kotlin/`](password-tool-kotlin/) | JDK 17+（桌面）/ Android SDK |
| Java 版 | Java（纯 JDK，零第三方依赖） | 命令行 / Web 服务 | [`password-tool-java/`](password-tool-java/) | JDK 8+（含 Termux） |

## 功能（三版一致）

- **随机密码生成**：8~128 位，可选小写 / 大写 / 数字 / 特殊符号，可排除易混字符 (0O1lI)
- **短语密码生成**：4~48 词，2459 词库（BIP39 风格），可选分隔符 / 首字母大写 / 随机数字
- **哈希计算**：MD5 / SHA-1 / SHA-256 / SHA-512 / SHA3-256
- **强度分析**：
  - 结构分块熵：自动识别汉字 / 英文单词（含 leet 变形、编辑距离 ≤1 的近似词）/ 数字 / 符号
  - 6 级强度评级（极弱 → 极强）
  - 8 种攻击场景破解时间估算（在线限速 / CPU / GPU / ASIC / 字典规则攻击）
  - 常见弱密码黑名单（rockyou 高频密码，含 leet 变形与尾部数字检测）

> 唯一差异：哈希算法中 Python 版额外支持 **Blake2b**，Kotlin / Java 版因运行时不便提供而省略，其余完全一致。

## 各版本快速开始

### 🐍 Web 版（Python + Flask）

```bash
cd password-tool
pip install flask
python app.py            # 默认 8080 端口
python app.py 5000       # 指定端口
```

浏览器打开 `http://127.0.0.1:8080`。

### 📱 多平台版（Kotlin + Compose）

需要一台电脑构建，详见 [`password-tool-kotlin/README.md`](password-tool-kotlin/README.md)。

```bash
cd password-tool-kotlin
gradle wrapper --gradle-version 8.9            # 首次生成 wrapper
./gradlew :composeApp:assembleDebug            # → Android APK
./gradlew :composeApp:packageDistributionForCurrentOS   # → 桌面安装包
./gradlew :composeApp:run                      # 桌面开发调试
```

### ☕ Java 版（纯 JDK，零依赖）

```bash
cd password-tool-java
./run.sh            # 命令行交互版
./run.sh web        # Web 版，浏览器打开 http://127.0.0.1:8080
./run.sh web 9090   # 指定端口
```

详见 [`password-tool-java/README.md`](password-tool-java/README.md)。

## 项目结构

```
Password-Forge-by-DeepSeek/
├── password-tool/             # Python + Flask Web 版
│   ├── app.py                 # Flask 后端（生成/分析/哈希 API）
│   ├── templates/index.html
│   └── static/ (script.js, style.css)
├── password-tool-kotlin/      # Kotlin Compose 多平台版
│   └── composeApp/src/
│       ├── commonMain/        # 共用逻辑 + Material 3 UI
│       ├── androidMain/       # Android 入口
│       └── desktopMain/       # 桌面入口
├── password-tool-java/        # 纯 JDK 零依赖版
│   ├── src/com/passwordtool/  # PasswordLogic / WordList / Json / Main / WebServer
│   ├── web/                   # 前端页面（复用 M3 界面）
│   └── run.sh                 # 一键编译运行
├── LICENSE                    # Apache-2.0
└── README.md                  # 本文件
```

## 许可证

[Apache-2.0](LICENSE)
