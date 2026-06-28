package com.aiwrite.data.local

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * AES/GCM encryption backed by Android Keystore for protecting sensitive values.
 */
object KeyEncryption {
    private const val KEYSTORE_ALIAS = "aiwrite_api_key"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val GCM_TAG_LENGTH = 128

    private fun getKey(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        if (keyStore.containsAlias(KEYSTORE_ALIAS)) {
            return (keyStore.getEntry(KEYSTORE_ALIAS, null) as KeyStore.SecretKeyEntry).secretKey
        }
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").apply {
            init(
                KeyGenParameterSpec.Builder(
                    KEYSTORE_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .build()
            )
        }.generateKey()
    }

    fun encrypt(plaintext: String): String? {
        return try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, getKey())
            val iv = cipher.iv
            val encrypted = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
            // Store IV + encrypted bytes as hex
            (iv + encrypted).joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            null
        }
    }

    fun decrypt(encoded: String): String? {
        return try {
            val bytes = encoded.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
            val iv = bytes.copyOfRange(0, 12)
            val encrypted = bytes.copyOfRange(12, bytes.size)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, getKey(), GCMParameterSpec(GCM_TAG_LENGTH, iv))
            String(cipher.doFinal(encrypted), Charsets.UTF_8)
        } catch (e: Exception) {
            null
        }
    }
}
