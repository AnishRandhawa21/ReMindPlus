package com.anish.remindplus.utils

import android.util.Base64
import java.security.spec.KeySpec
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * Helper class to handle End-to-End Encryption (E2EE) for notes and reminders.
 * This version uses the User ID to derive a stable encryption key, allowing
 * cross-device synchronization (Option A).
 */
object E2EEHelper {
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val ALGORITHM = "AES"
    private const val ITERATION_COUNT = 1000
    private const val KEY_LENGTH = 256
    
    // A fixed salt used for key derivation. Must be identical across all devices.
    private val SALT = "ReMindPlus_Sync_Salt_2024".toByteArray()

    // Cache the derived keys in memory
    private val keyCache = mutableMapOf<String, SecretKey>()

    private fun getSecretKey(userId: String): SecretKey {
        val normalizedId = userId.trim().lowercase()
        return keyCache.getOrPut(normalizedId) {
            deriveKey(normalizedId)
        }
    }

    private fun deriveKey(userId: String): SecretKey {
        return try {
            // Use HmacSHA1 for universal compatibility (API 1+)
            // This ensures the same key is generated across all devices regardless of Android version.
            val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
            
            val spec: KeySpec = PBEKeySpec(userId.toCharArray(), SALT, ITERATION_COUNT, KEY_LENGTH)
            val tmp = factory.generateSecret(spec)
            SecretKeySpec(tmp.encoded, ALGORITHM)
        } catch (e: Exception) {
            // Fallback to a simpler derivation if factory fails
            val hash = userId.toByteArray().take(32).toByteArray()
            val keyBytes = ByteArray(32)
            System.arraycopy(hash, 0, keyBytes, 0, hash.size.coerceAtMost(32))
            SecretKeySpec(keyBytes, ALGORITHM)
        }
    }

    /**
     * Encrypts a string using a key derived from the userId.
     */
    fun encrypt(plainText: String?, userId: String): String {
        if (plainText.isNullOrEmpty()) return ""
        if (userId.isBlank()) return plainText
        
        return try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(userId))
            
            val iv = cipher.iv
            val encryptedBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
            
            // Format: [IV_LENGTH (1 byte)][IV][CipherText]
            val combined = ByteArray(1 + iv.size + encryptedBytes.size)
            combined[0] = iv.size.toByte()
            System.arraycopy(iv, 0, combined, 1, iv.size)
            System.arraycopy(encryptedBytes, 0, combined, 1 + iv.size, encryptedBytes.size)
            
            Base64.encodeToString(combined, Base64.NO_WRAP)
        } catch (e: Exception) {
            e.printStackTrace()
            plainText
        }
    }

    /**
     * Decrypts a string using a key derived from the userId.
     */
    fun decrypt(cipherText: String?, userId: String): String {
        if (cipherText.isNullOrEmpty()) return ""
        if (userId.isBlank()) return cipherText
        
        return try {
            // If it doesn't look like base64 or is too short, return as is
            if (cipherText.length < 20) return cipherText
            
            val combined = try { 
                Base64.decode(cipherText, Base64.NO_WRAP) 
            } catch (e: Exception) { 
                return cipherText 
            }
            
            if (combined.isEmpty()) return cipherText

            val ivLength = combined[0].toInt() and 0xFF
            
            // Basic sanity check: GCM IV is usually 12 bytes
            // If it's not 12, this is likely an old plaintext note that happened to be valid Base64
            if (ivLength != 12 || ivLength > combined.size - 1) return cipherText

            val iv = ByteArray(ivLength)
            System.arraycopy(combined, 1, iv, 0, ivLength)
            
            val encryptedBytes = ByteArray(combined.size - 1 - ivLength)
            System.arraycopy(combined, 1 + ivLength, encryptedBytes, 0, encryptedBytes.size)
            
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val spec = GCMParameterSpec(128, iv)
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey(userId), spec)
            
            val decryptedBytes = cipher.doFinal(encryptedBytes)
            String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            // Returns original text if decryption fails (e.g. wrong key or not encrypted)
            cipherText
        }
    }
}
