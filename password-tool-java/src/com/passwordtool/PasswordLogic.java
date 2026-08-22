package com.passwordtool;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 密码工坊核心逻辑（Java 版）
 * <p>
 * 与 Kotlin 版 PasswordLogic.kt 完全对齐：
 * 随机密码 / 短语密码 / 结构熵计算 / 字典熵 / 破解时间估算 / 常见弱密码黑名单
 * <p>
 * 零第三方依赖，仅使用 JDK 标准库。
 */
public final class PasswordLogic {

    private PasswordLogic() {}

    // ========================== 字符集与常量 ==========================

    public static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    public static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    public static final String DIGITS = "0123456789";
    public static final String SPECIALS = "!@#$%^&*()-_=+[]{}|;:,.<>?";
    public static final String AMBIGUOUS = "0O1lI";
    public static final double CJK_POOL = 3000.0;   // 常用汉字库大小（攻击者常用字典）
    public static final double SYMBOL_POOL = 20.0;  // 常见符号池

    private static final SecureRandom RANDOM = new SecureRandom();

    // 常见弱密码黑名单（rockyou 高频密码，命中即视为秒破）
    private static final Set<String> COMMON_PASSWORDS = new HashSet<>(Arrays.asList(
        "123456", "password", "12345678", "qwerty", "123456789", "12345", "1234", "111111",
        "1234567", "dragon", "123123", "baseball", "abc123", "football", "monkey", "letmein",
        "696969", "shadow", "master", "666666", "qwertyuiop", "123321", "mustang", "1234567890",
        "michael", "654321", "superman", "1qaz2wsx", "7777777", "121212", "000000", "qazwsx",
        "123qwe", "killer", "trustno1", "jordan", "jennifer", "zxcvbnm", "asdfgh", "hunter",
        "buster", "soccer", "harley", "batman", "andrew", "tigger", "sunshine", "iloveyou",
        "charlie", "robert", "thomas", "hockey", "ranger", "daniel", "starwars", "112233",
        "george", "computer", "michelle", "jessica", "pepper", "1111", "zxcvbn", "555555",
        "11111111", "131313", "freedom", "777777", "pass", "aaaaaa", "ginger", "princess",
        "joshua", "cheese", "amanda", "summer", "love", "ashley", "6969", "nicole", "chelsea",
        "biteme", "matthew", "access", "yankees", "987654321", "dallas", "austin", "thunder",
        "taylor", "matrix", "william", "corvette", "hello", "martin", "heather", "secret",
        "merlin", "diamond", "1234qwer", "gfhjkm", "hammer", "silver", "222222", "88888888",
        "anthony", "justin", "test", "bailey", "q1w2e3r4t5", "patrick", "internet", "scooter",
        "orange", "11111", "golfer", "cookie", "richard", "samantha", "bigdog", "guitar",
        "jackson", "whatever", "mickey", "chicken", "sparky", "snoopy", "maverick", "phoenix",
        "camaro", "sexy", "peanut", "morgan", "welcome", "falcon", "cowboy", "ferrari",
        "samsung", "andrea", "smokey", "steelers", "gandalf", "hardcore", "james", "carlos",
        "soccer1", "rangers", "password1", "admin", "passw0rd", "root", "toor", "test123",
        "qwerty123", "pass123", "welcome1", "monkey123", "dragon123", "p@ssw0rd", "p@ssword"
    ));

    // ========================== 安全随机 ==========================

    /** 在 [0, bound) 中取安全随机整数 */
    public static int secureRandomInt(int bound) {
        return RANDOM.nextInt(bound);
    }

    /** 从字符集中安全随机取一个字符 */
    public static char secureChoice(String chars) {
        return chars.charAt(RANDOM.nextInt(chars.length()));
    }

    // ========================== 密码生成 ==========================

    public static String generateRandomPassword(int length, boolean useUpper, boolean useLower,
                                                boolean useDigits, boolean useSpecial,
                                                boolean avoidAmbiguous) {
        StringBuilder chars = new StringBuilder();
        if (useLower) chars.append(LOWERCASE);
        if (useUpper) chars.append(UPPERCASE);
        if (useDigits) chars.append(DIGITS);
        if (useSpecial) chars.append(SPECIALS);
        if (chars.length() == 0) chars.append(LOWERCASE).append(UPPERCASE).append(DIGITS);
        if (avoidAmbiguous) {
            StringBuilder filtered = new StringBuilder();
            for (int i = 0; i < chars.length(); i++) {
                if (AMBIGUOUS.indexOf(chars.charAt(i)) < 0) filtered.append(chars.charAt(i));
            }
            chars = filtered;
            if (chars.length() == 0) {
                chars = new StringBuilder(
                    LOWERCASE.replace("l", "") + DIGITS.replace("0", "").replace("1", ""));
            }
        }
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) sb.append(secureChoice(chars.toString()));
        return sb.toString();
    }

    public static String generatePassphrase(int wordCount, String separator,
                                            boolean titleCase, boolean includeNumber) {
        String[] words = new String[wordCount];
        for (int i = 0; i < wordCount; i++) words[i] = WordList.WORDS[secureRandomInt(WordList.SIZE)];
        String[] finalWords = words.clone();
        if (titleCase) {
            for (int i = 0; i < finalWords.length; i++) {
                String w = finalWords[i];
                finalWords[i] = Character.toUpperCase(w.charAt(0)) + w.substring(1);
            }
        }
        if (includeNumber) {
            int pos = secureRandomInt(finalWords.length);
            finalWords[pos] = finalWords[pos] + secureRandomInt(100);
        }
        return String.join(separator, finalWords);
    }

    // ========================== 熵计算 ==========================

    private static final Pattern CJK_RE = Pattern.compile("[\\u4e00-\\u9fff]+");
    private static final Pattern ALPHA_RE = Pattern.compile("[A-Za-z]+");
    private static final Pattern DIGIT_RE = Pattern.compile("\\d+");
    private static final Set<String> WORD_SET = new HashSet<>(Arrays.asList(WordList.WORDS));

    private static double log2(double x) {
        return Math.log(x) / Math.log(2.0);
    }

    /** 计算编辑距离（限制小字符串，性能足够） */
    private static int levenshtein1(String a, String b) {
        if (a.equals(b)) return 0;
        if (Math.abs(a.length() - b.length()) > 1) return 2;
        int[] prev = new int[b.length() + 1];
        for (int j = 0; j <= b.length(); j++) prev[j] = j;
        for (int i = 1; i <= a.length(); i++) {
            int[] cur = new int[b.length() + 1];
            cur[0] = i;
            for (int j = 1; j <= b.length(); j++) {
                int cost = (a.charAt(i - 1) == b.charAt(j - 1)) ? 0 : 1;
                cur[j] = Math.min(Math.min(prev[j] + 1, cur[j - 1] + 1), prev[j - 1] + cost);
            }
            prev = cur;
        }
        return prev[b.length()];
    }

    /** leet 反变换（Tr0ub4dor → troubador 类） */
    private static String unleet(String s) {
        StringBuilder sb = new StringBuilder();
        for (char c : s.toLowerCase().toCharArray()) {
            switch (c) {
                case '0': sb.append('o'); break;
                case '1': sb.append('i'); break;
                case '3': sb.append('e'); break;
                case '4': sb.append('a'); break;
                case '5': sb.append('s'); break;
                case '7': sb.append('t'); break;
                case '8': sb.append('b'); break;
                case '9': sb.append('g'); break;
                case '2': sb.append('z'); break;
                case '$': sb.append('s'); break;
                case '@': sb.append('a'); break;
                case '!': sb.append('i'); break;
                case '|': sb.append('l'); break;
                default:  sb.append(c); break;
            }
        }
        return sb.toString();
    }

    private static boolean isWordLike(String block) {
        String low = block.toLowerCase();
        if (low.length() < 3) return false;
        if (WORD_SET.contains(low)) return true;
        String unl = unleet(low);
        if (!unl.equals(low) && WORD_SET.contains(unl)) return true;
        for (String w : WordList.WORDS) {
            if (levenshtein1(low, w) <= 1) return true;
        }
        return false;
    }

    private static double wordBits(String block) {
        return isWordLike(block) ? log2(WordList.SIZE) : block.length() * log2(26.0);
    }

    /** 结构分块熵：汉字/英文词/数字/符号 分别按真实攻击场景估算 */
    public static double calculateEntropy(String password) {
        if (password == null || password.isEmpty()) return 0.0;

        List<String> cjkBlocks = findAll(CJK_RE, password);
        List<String> alphaBlocks = findAll(ALPHA_RE, password);
        List<String> digitBlocks = findAll(DIGIT_RE, password);

        int alphaWordCount = 0;
        double alphaBits = 0.0;
        for (String b : alphaBlocks) {
            if (isWordLike(b)) {
                alphaWordCount++;
                alphaBits += log2(WordList.SIZE);
            } else {
                alphaBits += b.length() * log2(26.0);
            }
        }

        double cjkBits = 0.0;
        for (String b : cjkBlocks) cjkBits += b.length() * log2(CJK_POOL);
        double digitBits = 0.0;
        for (String b : digitBlocks) digitBits += b.length() * log2(10.0);

        int symbolCount = password.length()
            - sumLen(cjkBlocks) - sumLen(alphaBlocks) - sumLen(digitBlocks);
        double symbolBits = symbolCount > 0 ? symbolCount * log2(SYMBOL_POOL) : 0.0;

        // 密码含汉字，或包含 >=2 个可识别英文单词 → 按结构熵
        if (!cjkBlocks.isEmpty() || alphaWordCount >= 2) {
            return cjkBits + alphaBits + digitBits + symbolBits;
        }

        // 纯随机风格：字符集池
        int pool = 0;
        if (containsAny(password, LOWERCASE)) pool += 26;
        if (containsAny(password, UPPERCASE)) pool += 26;
        if (containsAny(password, DIGITS)) pool += 10;
        if (containsAny(password, SPECIALS)) pool += 22;
        if (hasExtendedChar(password)) pool += 128;
        if (pool == 0) pool = 1;
        return password.length() * log2(pool);
    }

    /** 字典+规则攻击面对的熵 */
    public static double estimateDictEntropy(String password) {
        if (password == null || password.isEmpty()) return 0.0;
        double bits = 0.0;
        for (String b : findAll(CJK_RE, password)) bits += b.length() * log2(CJK_POOL);
        for (String b : findAll(ALPHA_RE, password)) {
            String low = b.toLowerCase();
            if (low.length() >= 3 && isWordLike(b)) {
                bits += log2(WordList.SIZE);
            }
        }
        return bits;
    }

    // ========================== 强度评级与破解时间 ==========================

    public static final class Strength {
        public final String label;
        public final String color;
        public Strength(String label, String color) { this.label = label; this.color = color; }
    }

    public static Strength strengthLabel(double entropy) {
        if (entropy < 28)   return new Strength("极弱", "#cf3a3a");
        if (entropy < 36)   return new Strength("弱", "#e67e22");
        if (entropy < 60)   return new Strength("一般", "#f1c40f");
        if (entropy < 80)   return new Strength("强", "#2ecc71");
        if (entropy < 128)  return new Strength("很强", "#27ae60");
        return new Strength("极强", "#1abc9c");
    }

    /** 破解场景行：key 与 Python 版接口一致，seconds 为秒数（前端自行格式化） */
    public static final class CrackRow {
        public final String key;      // 场景标识（接口字段名）
        public final String scene;    // 中文场景名
        public final double seconds;  // 预计耗时（秒）
        public CrackRow(String key, String scene, double seconds) {
            this.key = key; this.scene = scene; this.seconds = seconds;
        }
    }

    public static List<CrackRow> estimateCrackTimes(double entropy, double dictEntropy) {
        double total = Math.pow(2.0, entropy);
        List<CrackRow> rows = new ArrayList<>();
        rows.add(new CrackRow("online_100ph", "在线限速 (100次/小时)", total / (100.0 / 3600) / 2));
        rows.add(new CrackRow("online_10pm",  "在线限速 (10次/分钟)",  total / (10.0 / 60) / 2));
        rows.add(new CrackRow("cpu_md5",      "单核 CPU (MD5)",        total / 5e7 / 2));
        rows.add(new CrackRow("cpu_sha256",   "单核 CPU (SHA-256)",    total / 1e7 / 2));
        rows.add(new CrackRow("gpu_md5",      "GPU 集群 (MD5)",        total / 2e11 / 2));
        rows.add(new CrackRow("gpu_sha256",   "GPU 集群 (SHA-256)",    total / 1e10 / 2));
        rows.add(new CrackRow("gpu_bcrypt",   "GPU 集群 (bcrypt)",     total / 1e5 / 2));
        rows.add(new CrackRow("asic_sha256",  "ASIC/专用机 (SHA-256)", total / 1e13 / 2));
        if (dictEntropy > 0) {
            rows.add(new CrackRow("dict_rule", "常用字典攻击 (词库+规则)",
                Math.pow(2.0, dictEntropy) / 1e10 / 2));
        }
        return rows;
    }

    public static String formatDuration(double seconds) {
        if (seconds < 1)                return String.format("%.1f 毫秒", seconds * 1000);
        if (seconds < 60)               return String.format("%.1f 秒", seconds);
        if (seconds < 3600)             return String.format("%.1f 分钟", seconds / 60);
        if (seconds < 86400)            return String.format("%.1f 小时", seconds / 3600);
        if (seconds < 86400 * 365)      return String.format("%.1f 天", seconds / 86400);
        if (seconds < 86400 * 365 * 100) return String.format("%.1f 年", seconds / (86400 * 365));
        if (seconds < 86400 * 365 * 1e6) return String.format("%.1f 千年", seconds / (86400 * 365 * 1e3));
        if (seconds < 86400 * 365 * 1e9) return String.format("%.1f 十亿年", seconds / (86400 * 365 * 1e9));
        return String.format("%.2e 年", seconds / (86400 * 365));
    }

    // ========================== 常见弱密码检测 ==========================

    /** 常见弱密码检测（含 leet 变形与尾部数字） */
    public static boolean isCommonPassword(String password) {
        String raw = password.toLowerCase().trim();
        String rawBase = raw.replaceAll("\\d+$", "");
        String core = raw.replaceAll("[^a-z0-9]", "");
        String base = core.replaceAll("\\d+$", "");
        String unlRawBase = unleet(rawBase).replaceAll("[^a-z0-9]", "");
        String unlBase = unleet(base);
        String[] candidates = {raw, rawBase, core, base, unlRawBase, unlBase};
        for (String c : candidates) {
            if (!c.isEmpty() && COMMON_PASSWORDS.contains(c)) return true;
        }
        return false;
    }

    // ========================== 分析 ==========================

    public static final class Analysis {
        public final int length;
        public final double entropy;
        public final Strength strength;
        public final boolean commonPassword;
        public final List<CrackRow> crackRows;
        public Analysis(int length, double entropy, Strength strength,
                        boolean commonPassword, List<CrackRow> crackRows) {
            this.length = length;
            this.entropy = entropy;
            this.strength = strength;
            this.commonPassword = commonPassword;
            this.crackRows = crackRows;
        }
    }

    public static Analysis analyze(String password) {
        double entropy = calculateEntropy(password);
        double dict = estimateDictEntropy(password);
        boolean common = isCommonPassword(password);
        if (common) {
            entropy = Math.min(entropy, 9.0);
            dict = Math.min(dict, 9.0);
        }
        return new Analysis(
            password.length(), entropy, strengthLabel(entropy), common,
            estimateCrackTimes(entropy, dict));
    }

    // ========================== 哈希 ==========================

    /** 支持的算法（JDK 内置；Blake2b 需第三方库，故不提供） */
    public static final String[] HASH_ALGORITHMS = {"MD5", "SHA-1", "SHA-256", "SHA-512", "SHA3-256"};

    /** 计算哈希（algorithm: MD5 / SHA-1 / SHA-256 / SHA-512 / SHA3-256） */
    public static String hashHex(String algorithm, String text) {
        try {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            byte[] digest = md.digest(text.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException("不支持的算法: " + algorithm, e);
        }
    }

    /** 计算全部支持算法的哈希，返回 Map（算法名 → 十六进制） */
    public static Map<String, String> calculateHashes(String password) {
        Map<String, String> map = new LinkedHashMap<>();
        for (String algo : HASH_ALGORITHMS) {
            map.put(algo, hashHex(algo, password));
        }
        return map;
    }

    // ========================== 内部工具 ==========================

    private static List<String> findAll(Pattern p, String s) {
        List<String> out = new ArrayList<>();
        Matcher m = p.matcher(s);
        while (m.find()) out.add(m.group());
        return out;
    }

    private static int sumLen(List<String> list) {
        int n = 0;
        for (String s : list) n += s.length();
        return n;
    }

    private static boolean containsAny(String s, String chars) {
        for (int i = 0; i < s.length(); i++) {
            if (chars.indexOf(s.charAt(i)) >= 0) return true;
        }
        return false;
    }

    private static boolean hasExtendedChar(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) > 127) return true;
        }
        return false;
    }
}
