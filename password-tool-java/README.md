# 🔐 密码工坊 · Java 版

基于**纯 JDK（零第三方依赖）**的密码生成与强度分析工具。
功能与 Python 版 / Kotlin 版完全一致，一份代码两种运行方式：

| 模式 | 说明 | 命令 |
|------|------|------|
| 命令行 | 终端交互菜单（适合 Termux/服务器） | `./run.sh` |
| Web 服务 | 内置 HTTP 服务器 + 原有 M3 前端页面 | `./run.sh web` |

## 功能

- **随机密码生成**：8~128 位，可选小写/大写/数字/特殊符号，可排除易混字符 (0O1lI)
- **短语密码生成**：4~48 词，2459 词库（BIP39 风格），可选分隔符 / 首字母大写 / 随机数字
- **哈希计算**：MD5 / SHA-1 / SHA-256 / SHA-512 / SHA3-256（UTF-8 编码）
- **强度分析**：
  - 结构分块熵：自动识别汉字 / 英文单词（含 leet 变形、编辑距离≤1 的近似词）/ 数字 / 符号
  - 6 级强度评级（极弱 → 极强）
  - 8 种攻击场景破解时间估算（在线限速 / CPU / GPU / ASIC / 字典规则攻击）
  - 常见弱密码黑名单（rockyou 高频密码，含 leet 变形与尾部数字检测）

> 注：哈希算法比 Python 版少了 Blake2b（JDK 内置不支持），其余完全一致。

## 环境要求

- JDK 8+（推荐 17，Termux: `pkg install openjdk-17`）

## 快速开始

```bash
./run.sh            # 命令行交互版
./run.sh web        # Web 版，浏览器打开 http://127.0.0.1:8080
./run.sh web 9090   # 指定端口
```

环境变量：

- `PORT`：Web 服务端口（默认 8080）
- `WEB_DIR`：前端页面目录（默认自动查找项目下的 `web/`）

## 手动编译运行

```bash
mkdir -p out
javac -encoding UTF-8 -d out $(find src -name '*.java')
java -cp out com.passwordtool.Main        # 命令行版
java -cp out com.passwordtool.WebServer   # Web 版
```

## 项目结构

```
password-tool-java/
├── src/com/passwordtool/
│   ├── PasswordLogic.java   # 核心逻辑（生成/熵/破解时间/黑名单/哈希）零依赖
│   ├── WordList.java        # 2459 词表
│   ├── Json.java            # 极简 JSON 工具（生成 + 解析）
│   ├── Main.java            # 命令行交互版
│   └── WebServer.java       # 内置 Web 服务（JDK 自带 HttpServer）
├── web/                     # 前端页面（复用原 M3 界面）
│   ├── index.html
│   └── static/ (style.css, script.js)
├── run.sh                   # 一键编译运行脚本
└── README.md
```

## API 接口（与 Python Flask 版兼容）

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/health` | GET | 健康检查 `{"status":"ok","version":"4.3"}` |
| `/api/generate-password` | POST | `{length, use_upper, use_lower, use_digits, use_special, avoid_ambiguous}` |
| `/api/generate-passphrase` | POST | `{count, separator, include_number, title_case}` |
| `/api/analyze` | POST | `{password}` → 熵/评级/常见弱密码/`crack_times`（场景→秒数） |
| `/api/hash` | POST | `{password}` → 各算法哈希 |

## 与各版本对照

| 版本 | 语言 | 形态 | 位置 |
|------|------|------|------|
| 多文件版 | Python + Flask | Web | `password-tool/` |
| 多平台版 | Kotlin + Compose | Android / 桌面 | `password-tool-kotlin/` |
| **Java 版** | Java（纯 JDK） | 命令行 / Web | `password-tool-java/`（本目录） |
