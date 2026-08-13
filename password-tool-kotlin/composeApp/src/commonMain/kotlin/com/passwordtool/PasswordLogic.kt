package com.passwordtool

import kotlin.math.log2
import kotlin.math.pow

// ========================== 字符集与常量 ==========================

const val LOWERCASE = "abcdefghijklmnopqrstuvwxyz"
const val UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
const val DIGITS = "0123456789"
const val SPECIALS = "!@#\$%^&*()-_=+[]{}|;:,.<>?"
const val AMBIGUOUS = "0O1lI"
const val CJK_POOL = 3000.0
const val SYMBOL_POOL = 20.0

// 常见弱密码黑名单（rockyou 高频）
val COMMON_PASSWORDS = setOf(
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
)

// ========================== 密码生成 ==========================

fun generateRandomPassword(
    length: Int,
    useUpper: Boolean, useLower: Boolean, useDigits: Boolean,
    useSpecial: Boolean, avoidAmbiguous: Boolean
): String {
    var chars = ""
    if (useLower) chars += LOWERCASE
    if (useUpper) chars += UPPERCASE
    if (useDigits) chars += DIGITS
    if (useSpecial) chars += SPECIALS
    if (chars.isEmpty()) chars = LOWERCASE + UPPERCASE + DIGITS
    if (avoidAmbiguous) {
        chars = chars.filter { it !in AMBIGUOUS }
        if (chars.isEmpty()) chars = LOWERCASE.filter { it != 'l' } + DIGITS.filter { it != '0' && it != '1' }
    }
    return buildString { repeat(length) { append(secureChoice(chars)) } }
}

fun generatePassphrase(
    wordCount: Int, separator: String, titleCase: Boolean, includeNumber: Boolean
): String {
    val words = List(wordCount) { WORD_LIST[secureRandomInt(WORD_LIST.size)] }
    val finalWords = if (titleCase) words.map { it.replaceFirstChar { c -> c.uppercaseChar() } } else words
    val result = finalWords.joinToString(separator)
    return if (includeNumber) {
        val pos = secureRandomInt(finalWords.size)
        finalWords.toMutableList().apply { this[pos] += secureRandomInt(100).toString() }.joinToString(separator)
    } else result
}

// ========================== 熵计算 ==========================

private val CJK_RE = Regex("[\\u4e00-\\u9fff]+")
private val ALPHA_RE = Regex("[A-Za-z]+")
private val DIGIT_RE = Regex("\\d+")
private val WORD_SET: Set<String> = WORD_LIST.toSet()

private fun levenshtein1(a: String, b: String): Int {
    if (a == b) return 0
    if (kotlin.math.abs(a.length - b.length) > 1) return 2
    var prev = (0..b.length).toList().toIntArray()
    for (i in 1..a.length) {
        val cur = IntArray(b.length + 1) { j -> if (j == 0) i else 0 }
        for (j in 1..b.length) {
            cur[j] = minOf(prev[j] + 1, cur[j - 1] + 1, prev[j - 1] + if (a[i - 1] == b[j - 1]) 0 else 1)
        }
        prev = cur
    }
    return prev[b.length]
}

/** leet 反变换（Tr0ub4dor → troubadour 类） */
private fun unleet(s: String): String {
    val map = mapOf('0' to 'o', '1' to 'i', '3' to 'e', '4' to 'a', '5' to 's', '7' to 't',
        '8' to 'b', '9' to 'g', '2' to 'z', '$' to 's', '@' to 'a', '!' to 'i', '|' to 'l')
    return s.lowercase().map { map[it] ?: it }.joinToString("")
}

private fun isWordLike(block: String): Boolean {
    val low = block.lowercase()
    if (low.length < 3) return false
    if (low in WORD_SET) return true
    val unl = unleet(low)
    if (unl != low && unl in WORD_SET) return true
    return WORD_LIST.any { levenshtein1(low, it) <= 1 }
}

private fun wordBits(block: String): Double {
    return if (isWordLike(block)) log2(WORD_LIST.size.toDouble()) else block.length * log2(26.0)
}

/** 结构分块熵：汉字/英文词/数字/符号 分别按真实攻击场景估算 */
fun calculateEntropy(password: String): Double {
    if (password.isEmpty()) return 0.0

    val cjkBlocks = CJK_RE.findAll(password).map { it.value }.toList()
    val alphaBlocks = ALPHA_RE.findAll(password).map { it.value }.toList()
    val digitBlocks = DIGIT_RE.findAll(password).map { it.value }.toList()

    var alphaWordCount = 0
    var alphaBits = 0.0
    for (b in alphaBlocks) {
        if (isWordLike(b)) {
            alphaWordCount++
            alphaBits += log2(WORD_LIST.size.toDouble())
        } else {
            alphaBits += b.length * log2(26.0)
        }
    }
    val cjkBits = cjkBlocks.sumOf { it.length * log2(CJK_POOL) }
    val digitBits = digitBlocks.sumOf { it.length * log2(10.0) }
    val symbolCount = password.length - cjkBlocks.sumOf { it.length } -
        alphaBlocks.sumOf { it.length } - digitBlocks.sumOf { it.length }
    val symbolBits = if (symbolCount > 0) symbolCount * log2(SYMBOL_POOL) else 0.0

    if (cjkBlocks.isNotEmpty() || alphaWordCount >= 2) {
        return cjkBits + alphaBits + digitBits + symbolBits
    }

    // 纯随机风格：字符集池
    var pool = 0
    if (password.any { it in LOWERCASE }) pool += 26
    if (password.any { it in UPPERCASE }) pool += 26
    if (password.any { it in DIGITS }) pool += 10
    if (password.any { it in SPECIALS }) pool += 22
    if (password.any { it.code > 127 }) pool += 128
    if (pool == 0) pool = 1
    return password.length * log2(pool.toDouble())
}

/** 字典+规则攻击面对的熵 */
fun estimateDictEntropy(password: String): Double {
    var bits = 0.0
    CJK_RE.findAll(password).forEach { bits += it.value.length * log2(CJK_POOL) }
    ALPHA_RE.findAll(password).forEach {
        val low = it.value.lowercase()
        if (low.length >= 3 && (low in WORD_SET || unleet(low) in WORD_SET ||
                    WORD_LIST.any { w -> levenshtein1(low, w) <= 1 })) {
            bits += log2(WORD_LIST.size.toDouble())
        }
    }
    return bits
}

// ========================== 强度评级与破解时间 ==========================

fun strengthLabel(entropy: Double): Pair<String, String> = when {
    entropy < 28 -> "极弱" to "#cf3a3a"
    entropy < 36 -> "弱" to "#e67e22"
    entropy < 60 -> "一般" to "#f1c40f"
    entropy < 80 -> "强" to "#2ecc71"
    entropy < 128 -> "很强" to "#27ae60"
    else -> "极强" to "#1abc9c"
}

data class CrackRow(val scene: String, val time: String)

fun estimateCrackTimes(entropy: Double, dictEntropy: Double): List<CrackRow> {
    val rates = listOf(
        "在线限速 (100次/小时)" to 100.0 / 3600,
        "在线限速 (10次/分钟)" to 10.0 / 60,
        "单核 CPU (MD5)" to 5e7,
        "单核 CPU (SHA-256)" to 1e7,
        "GPU 集群 (MD5)" to 2e11,
        "GPU 集群 (SHA-256)" to 1e10,
        "GPU 集群 (bcrypt)" to 1e5,
        "ASIC/专用机 (SHA-256)" to 1e13,
    )
    val total = 2.0.pow(entropy)
    val rows = rates.map { (name, rate) -> CrackRow(name, formatDuration(total / rate / 2)) }
    return rows + if (dictEntropy > 0) {
        listOf(CrackRow("常用字典攻击 (词库+规则)", formatDuration(2.0.pow(dictEntropy) / 1e10 / 2)))
    } else emptyList()
}

fun formatDuration(seconds: Double): String = when {
    seconds < 1 -> "${(seconds * 1000).let { String.format("%.1f", it) }} 毫秒"
    seconds < 60 -> "${String.format("%.1f", seconds)} 秒"
    seconds < 3600 -> "${String.format("%.1f", seconds / 60)} 分钟"
    seconds < 86400 -> "${String.format("%.1f", seconds / 3600)} 小时"
    seconds < 86400 * 365 -> "${String.format("%.1f", seconds / 86400)} 天"
    seconds < 86400 * 365 * 100 -> "${String.format("%.1f", seconds / (86400 * 365))} 年"
    seconds < 86400 * 365 * 1e6 -> "${String.format("%.1f", seconds / (86400 * 365 * 1e3))} 千年"
    seconds < 86400 * 365 * 1e9 -> "${String.format("%.1f", seconds / (86400 * 365 * 1e9))} 十亿年"
    else -> "${String.format("%.2e", seconds / (86400 * 365))} 年"
}

/** 常见弱密码检测（含 leet 变形与尾部数字） */
fun isCommonPassword(password: String): Boolean {
    val raw = password.lowercase().trim()
    val rawBase = raw.replace(Regex("\\d+$"), "")
    val core = raw.filter { it.isLetterOrDigit() }
    val base = core.replace(Regex("\\d+$"), "")
    val unlRawBase = unleet(rawBase).filter { it.isLetterOrDigit() }
    val unlBase = unleet(base)
    return listOf(raw, rawBase, core, base, unlRawBase, unlBase).any { it in COMMON_PASSWORDS }
}

/** 分析结果 */
data class Analysis(
    val length: Int,
    val entropy: Double,
    val strength: Pair<String, String>,
    val commonPassword: Boolean,
    val crackRows: List<CrackRow>
)

fun analyze(password: String): Analysis {
    val entropy = calculateEntropy(password)
    var dict = estimateDictEntropy(password)
    val common = isCommonPassword(password)
    val finalEntropy = if (common) minOf(entropy, 9.0) else entropy
    if (common) dict = minOf(dict, 9.0)
    return Analysis(
        length = password.length,
        entropy = finalEntropy,
        strength = strengthLabel(finalEntropy),
        commonPassword = common,
        crackRows = estimateCrackTimes(finalEntropy, dict)
    )
}
