package org.powerscala.encryption

import javax.crypto.SecretKeyFactory
import java.security.SecureRandom
import javax.crypto.spec.PBEKeySpec
import java.util

/**
 * @author Matt Hicks <mhicks@powerscala.org>
 */
object PasswordFactory {
  def authenticate(attemptedPassword: String, encryptedPassword: Array[Byte], salt: Array[Byte]) = {
    val encryptedAttemptedPassword = encryptPassword(attemptedPassword, salt)
    util.Arrays.equals(encryptedPassword, encryptedAttemptedPassword)
  }

  def encryptPassword(password: String, salt: Array[Byte]) = {
    val algorithm = "PBKDF2WithHmacSHA1"
    val derivedKeyLength = 160
    val iterations = 20000
    val spec = new PBEKeySpec(password.toCharArray, salt, iterations, derivedKeyLength)
    val factory = SecretKeyFactory.getInstance(algorithm)
    factory.generateSecret(spec).getEncoded
  }

  def generateSalt() = {
    val random = SecureRandom.getInstance("SHA1PRNG")
    val salt = new Array[Byte](8)
    random.nextBytes(salt)
    salt
  }
}