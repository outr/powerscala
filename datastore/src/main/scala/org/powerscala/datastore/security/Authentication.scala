package org.powerscala.datastore.security

import java.util.UUID
import org.powerscala.datastore.Identifiable
import org.powerscala.encryption.PasswordFactory

/**
 * @author Matt Hicks <mhicks@outr.com>
 */
case class Authentication(encryptedPassword: Array[Byte],
                          salt: Array[Byte],
                          enabled: Boolean = true,
                          id: UUID = UUID.randomUUID()) extends Identifiable {
  def authenticate(password: String) = enabled && PasswordFactory.authenticate(password, encryptedPassword, salt)
}

object Authentication {
  def apply(password: String): Authentication = {
    val salt = PasswordFactory.generateSalt()
    val encrypted = PasswordFactory.encryptPassword(password, salt)
    Authentication(encrypted, salt)
  }
}
