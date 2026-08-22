#!/bin/sh
# 🔐 密码工坊 · Java 版 编译 + 运行脚本
# 用法:
#   ./run.sh          命令行交互模式
#   ./run.sh web      启动 Web 服务（浏览器访问 http://127.0.0.1:8080）
#   ./run.sh web 9090 指定端口启动 Web 服务
set -e
cd "$(dirname "$0")"

if ! command -v javac >/dev/null 2>&1; then
    echo "[提示] 未检测到 JDK，请先安装:"
    echo "  Termux:  pkg install openjdk-17"
    echo "  Debian:  apt install openjdk-17-jdk"
    exit 1
fi

echo "[1/2] 编译中 ..."
mkdir -p out
javac -encoding UTF-8 -d out $(find src -name '*.java')

echo "[2/2] 启动 ..."
if [ "$1" = "web" ]; then
    shift
    exec java -cp out com.passwordtool.WebServer "$@"
else
    exec java -cp out com.passwordtool.Main
fi
