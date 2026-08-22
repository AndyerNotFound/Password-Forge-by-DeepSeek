package com.passwordtool;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * 密码工坊 · 命令行交互版
 * <p>
 * 用法: java -cp out com.passwordtool.Main
 */
public final class Main {

    private static final Scanner SCANNER = new Scanner(System.in);

    private Main() {}

    public static void main(String[] args) throws Exception {
        if (args.length > 0 && "web".equals(args[0])) {
            WebServer.main(args); // 委托给 Web 服务模式
            return;
        }
        System.out.println();
        System.out.println("==================================================");
        System.out.println("  🔐 密码工坊 · Java 版 v4.3");
        System.out.println("  随机密码 / 短语密码 / 哈希计算 / 强度分析");
        System.out.println("==================================================");
        while (true) {
            System.out.println();
            System.out.println("请选择功能:");
            System.out.println("  1. 生成随机密码");
            System.out.println("  2. 生成短语密码");
            System.out.println("  3. 计算哈希值");
            System.out.println("  4. 密码强度分析");
            System.out.println("  0. 退出");
            System.out.print("> ");
            String line = SCANNER.nextLine().trim();
            switch (line) {
                case "1": menuRandomPassword(); break;
                case "2": menuPassphrase(); break;
                case "3": menuHash(); break;
                case "4": menuAnalyze(); break;
                case "0":
                    System.out.println("再见 👋");
                    return;
                default:
                    System.out.println("无效选项，请重新输入。");
            }
        }
    }

    // ========================== 随机密码 ==========================

    private static void menuRandomPassword() {
        int length = askInt("密码长度 (8~128, 默认 16)", 16, 8, 128);
        boolean lower = askBool("包含小写字母 (a-z)? [Y/n]", true);
        boolean upper = askBool("包含大写字母 (A-Z)? [Y/n]", true);
        boolean digits = askBool("包含数字 (0-9)? [Y/n]", true);
        boolean special = askBool("包含特殊符号 (!@#...)? [Y/n]", true);
        boolean avoid = askBool("排除易混字符 (0O1lI)? [y/N]", false);

        String pw = PasswordLogic.generateRandomPassword(
            length, upper, lower, digits, special, avoid);
        double entropy = PasswordLogic.calculateEntropy(pw);
        PasswordLogic.Strength st = PasswordLogic.strengthLabel(entropy);

        System.out.println();
        System.out.println("  ── 生成的密码 ──");
        System.out.println("  " + pw);
        System.out.println("  长度: " + pw.length() + " 位 | 熵值: "
            + String.format("%.1f", entropy) + " bits | 评级: " + st.label);
        System.out.println();
        if (askBool("复制到剪贴板? [y/N]", false)) {
            copyToClipboard(pw);
            System.out.println("  已复制 ✅");
        }
        if (askBool("查看完整强度分析? [y/N]", false)) {
            printAnalysis(PasswordLogic.analyze(pw));
        }
    }

    // ========================== 短语密码 ==========================

    private static void menuPassphrase() {
        int count = askInt("单词数量 (4~48, 默认 6)", 6, 4, 48);
        System.out.println("分隔符: 1)连字符-  2)下划线_  3)空格  4)句点.  5)无  (默认 1)");
        System.out.print("> ");
        String sepChoice = SCANNER.nextLine().trim();
        String sep = "-";
        switch (sepChoice) {
            case "2": sep = "_"; break;
            case "3": sep = " "; break;
            case "4": sep = "."; break;
            case "5": sep = ""; break;
            default: break;
        }
        boolean title = askBool("首字母大写 (Title Case)? [y/N]", false);
        boolean number = askBool("随机插入数字? [y/N]", false);

        System.out.println();
        System.out.println("  ── 候选短语 (共 5 条) ──");
        String first = null;
        for (int i = 0; i < 5; i++) {
            String p = PasswordLogic.generatePassphrase(count, sep, title, number);
            if (i == 0) first = p;
            System.out.println("  " + (i + 1) + ". " + p);
        }
        double entropy = PasswordLogic.calculateEntropy(first);
        PasswordLogic.Strength st = PasswordLogic.strengthLabel(entropy);
        System.out.println("  参考熵值: " + String.format("%.1f", entropy) + " bits | 评级: " + st.label);
        System.out.println();
        if (askBool("复制第 1 条到剪贴板? [y/N]", false)) {
            copyToClipboard(first);
            System.out.println("  已复制 ✅");
        }
    }

    // ========================== 哈希 ==========================

    private static void menuHash() {
        System.out.print("输入要哈希的文本: ");
        String input = SCANNER.nextLine();
        if (input.isEmpty()) {
            System.out.println("  输入不能为空。");
            return;
        }
        System.out.println();
        System.out.println("  ── 哈希结果 (UTF-8) ──");
        Map<String, String> hashes = PasswordLogic.calculateHashes(input);
        for (Map.Entry<String, String> e : hashes.entrySet()) {
            System.out.println("  " + e.getKey() + "  " + e.getValue());
        }
    }

    // ========================== 强度分析 ==========================

    private static void menuAnalyze() {
        System.out.print("输入要分析的密码: ");
        String input = SCANNER.nextLine();
        if (input.isEmpty()) {
            System.out.println("  输入不能为空。");
            return;
        }
        printAnalysis(PasswordLogic.analyze(input));
    }

    private static void printAnalysis(PasswordLogic.Analysis a) {
        System.out.println();
        System.out.println("  ── 强度分析报告 ──");
        if (a.commonPassword) {
            System.out.println("  ⚠️ 此密码在常见弱密码黑名单中，可被瞬间破解！请立即更换！");
        }
        System.out.println("  长度: " + a.length + " 位");
        System.out.println("  熵值: " + String.format("%.1f", a.entropy) + " bits");
        System.out.println("  评级: " + a.strength.label + "  (" + a.strength.color + ")");
        System.out.println();
        System.out.println("  预计破解所需时间:");
        List<PasswordLogic.CrackRow> rows = a.crackRows;
        int nameWidth = 0;
        for (PasswordLogic.CrackRow r : rows) {
            nameWidth = Math.max(nameWidth, r.scene.length());
        }
        for (PasswordLogic.CrackRow r : rows) {
            System.out.printf("    %-" + nameWidth + "s  %s%n", r.scene, PasswordLogic.formatDuration(r.seconds));
        }
        System.out.println();
    }

    // ========================== 工具 ==========================

    private static int askInt(String prompt, int def, int min, int max) {
        while (true) {
            System.out.print(prompt + ": ");
            String line = SCANNER.nextLine().trim();
            if (line.isEmpty()) return def;
            try {
                int v = Integer.parseInt(line);
                if (v < min || v > max) {
                    System.out.println("  请输入 " + min + "~" + max + " 之间的整数。");
                    continue;
                }
                return v;
            } catch (NumberFormatException e) {
                System.out.println("  请输入整数。");
            }
        }
    }

    private static boolean askBool(String prompt, boolean def) {
        while (true) {
            System.out.print(prompt + ": ");
            String line = SCANNER.nextLine().trim().toLowerCase();
            if (line.isEmpty()) return def;
            if (line.equals("y") || line.equals("yes")) return true;
            if (line.equals("n") || line.equals("no")) return false;
            System.out.println("  请输入 y 或 n。");
        }
    }

    /** 尝试复制到剪贴板（需要系统剪贴板工具；失败则忽略） */
    private static void copyToClipboard(String text) {
        try {
            // 优先尝试 Termux 的 termux-clipboard-set
            String[] cmds = {"termux-clipboard-set", "clip", "xclip", "pbcopy"};
            for (String cmd : cmds) {
                Process p = new ProcessBuilder(cmd).start();
                p.getOutputStream().write(text.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                p.getOutputStream().close();
                if (p.waitFor() == 0) return;
            }
        } catch (Exception ignored) {
            // 环境无剪贴板工具时静默失败
        }
        System.out.println("  (未检测到剪贴板工具，请手动复制)");
    }
}
