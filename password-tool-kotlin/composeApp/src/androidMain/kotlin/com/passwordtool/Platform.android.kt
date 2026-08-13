package com.passwordtool

import java.security.MessageDigest
import java.security.SecureRandom

private val secureRandom = SecureRandom()

actual fun secureRandomInt(bound: Int): Int = secureRandom.nextInt(bound)

actual fun secureChoice(chars: String): Char = chars[secureRandom.nextInt(chars.length)]

actual fun hashHex(algorithm: String, text: String): String {
    val md = MessageDigest.getInstance(algorithm)
    return md.digest(text.toByteArray(Charsets.UTF_8))
        .joinToString("") { "%02x".format(it) }
}
