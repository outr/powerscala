package org.powerscala.encryption

import javax.crypto.SecretKeyFactory
import java.security.SecureRandom
import javax.crypto.spec.PBEKeySpec
import java.util
import scala.util.Random

/**
 * @author Matt Hicks <mhicks@powerscala.org>
 */
object PasswordFactory {
  var saltType = "SHA1PRNG"

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

  def generateSalt() = if (saltType.equalsIgnoreCase("TEST")) {
    Array[Byte](1, 2, 3, 4, 5, 6, 7, 8)
  } else {
    val random = SecureRandom.getInstance(saltType)
    val salt = new Array[Byte](8)
    random.nextBytes(salt)
    salt
  }

  def generatePassword(chars: String = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ_0123456789",
                       digits: Int = 8) = {
    val r = new Random()
    (0 until digits).map(index => chars.charAt(r.nextInt(chars.length))).mkString
  }
}