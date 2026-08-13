package com.passwordtool

/**
 * 平台相关能力声明（expect）
 * - 密码学安全随机数（Android/Desktop 都用 JVM SecureRandom）
 * - 哈希计算（JVM MessageDigest）
 */

/** 在 [0, bound) 中取安全随机整数 */
expect fun secureRandomInt(bound: Int): Int

/** 从字符集中安全随机取一个字符 */
expect fun secureChoice(chars: String): Char

/** 计算哈希（algorithm: MD5 / SHA-1 / SHA-256 / SHA-512 / SHA3-256） */
expect fun hashHex(algorithm: String, text: String): String
