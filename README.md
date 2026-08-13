# 🔐 密码工坊 - Kotlin 版

基于 **Kotlin + Compose Multiplatform + Material 3** 的密码生成与强度分析工具。
一套代码，可构建出：

| 平台 | 产物 | 构建命令 |
|------|------|---------|
| Android | APK | `./gradlew :composeApp:assembleDebug` |
| Windows | EXE 压缩包 (含 .exe) | `./gradlew :composeApp:packageDistributionForCurrentOS` |
| Linux | Deb 安装包 | 同上（在 Linux 上运行） |

## 功能

- 随机密码生成（8~128 位，可选字符集、排除易混字符）
- 短语密码生成（4~48 词，2459 词库，可选分隔符/大写/数字）
- 哈希计算（MD5 / SHA-1 / SHA-256 / SHA-512 / SHA3-256）
- 强度分析：熵值（分块识别汉字/英文词/数字/符号）、6 级评级、
  8 种攻击场景破解时间估算、常见弱密码黑名单（含 leet 变形检测）

## 构建要求

- JDK 17+（[下载](https://adoptium.net/)）
- Android 构建还需 Android SDK（Android Studio 自带，或命令行 `sdkmanager`）

## 快速开始（需要一台电脑）

### 1. 准备

```bash
# 安装 JDK 17 后，在项目目录执行：
# 首次会生成 gradlew（如果本机已装 gradle 8.9+，可直接用 gradle 命令）
gradle wrapper --gradle-version 8.9
```

### 2. 构建 Android APK

```bash
./gradlew :composeApp:assembleDebug
# 产物: composeApp/build/outputs/apk/debug/composeApp-debug.apk
```

### 3. 构建桌面版（Windows EXE / Linux）

```bash
# Windows 上运行 → 生成 Windows 压缩包（内含 .exe）
# Linux 上运行 → 生成 .deb 安装包
./gradlew :composeApp:packageDistributionForCurrentOS
# 产物: composeApp/build/compose/binaries/main/
```

### 4. 本地直接运行桌面版（开发调试）

```bash
./gradlew :composeApp:run
```

## 项目结构

```
composeApp/
├── src/
│   ├── commonMain/kotlin/com/passwordtool/
│   │   ├── App.kt              # Material 3 UI（双平台共用）
│   │   ├── PasswordLogic.kt    # 熵/破解时间/黑名单/生成逻辑
│   │   ├── Platform.kt         # expect 声明（安全随机/哈希）
│   │   └── WordList.kt         # 2459 词表（自动生成）
│   ├── androidMain/            # Android 入口 + actual 实现
│   └── desktopMain/            # 桌面入口 + actual 实现
└── build.gradle.kts
```
