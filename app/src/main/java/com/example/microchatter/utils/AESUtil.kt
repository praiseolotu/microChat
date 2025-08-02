package com.example.microchatter.utils

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

object AESUtil {

    private val key: SecretKey by lazy {
        val keyGen = KeyGenerator.getInstance("AES")
        keyGen.init(128)
        keyGen.generateKey()
    }

    fun encrypt(input: String): String = encrypt(input.toByteArray(Charsets.UTF_8))

    fun encrypt(input: ByteArray): String {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val iv = ByteArray(16).apply { SecureRandom().nextBytes(this) }
        cipher.init(Cipher.ENCRYPT_MODE, key, IvParameterSpec(iv))
        val encrypted = cipher.doFinal(input)
        return Base64.encodeToString(iv + encrypted, Base64.DEFAULT)
    }

    fun decrypt(encryptedInput: String): String {
        return String(decryptToBytes(encryptedInput), Charsets.UTF_8)
    }

    fun decryptToBytes(encryptedInput: String): ByteArray {
        val combined = Base64.decode(encryptedInput, Base64.DEFAULT)
        val iv = combined.sliceArray(0 until 16)
        val encrypted = combined.sliceArray(16 until combined.size)
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, key, IvParameterSpec(iv))
        return cipher.doFinal(encrypted)
    }
}