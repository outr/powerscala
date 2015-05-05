package org.powerscala

import org.powerscala.transactional._
import org.scalatest.{Matchers, WordSpec}

/**
 * @author Matt Hicks <matt@outr.com>
 */
class TransactionSpec extends WordSpec with Matchers {
  private var local = 0
  private var global = 0

  "Simple transaction" should {
    "not apply until transaction closed" in {
      transaction {
        transaction.onCommit(localized = true) {
          local += 1
        }
        transaction.onCommit(localized = false) {
          global += 1
        }
        local should equal(0)
        global should equal(0)
      }
      local should equal(1)
      global should equal(1)
    }
    "not apply until root transaction closed" in {
      reset()
      transaction {
        transaction {
          transaction.onCommit(localized = true) {
            local += 1
          }
          transaction.onCommit(localized = false) {
            global += 1
          }
          local should equal(0)
          global should equal(0)
        }
        local should equal(1)
        global should equal(0)
      }
      local should equal(1)
      global should equal(1)
    }
    "not apply if exception is thrown" in {
      reset()
      an [RuntimeException] should be thrownBy {
        transaction {
          transaction {
            transaction.onCommit(localized = true) {
              local += 1
            }
            transaction.onCommit(localized = false) {
              global += 1
            }
            local should equal(0)
            global should equal(0)
            throw new RuntimeException
          }
        }
      }
      local should equal(0)
      global should equal(0)
    }
    "rollback if exception is thrown" in {
      reset()
      an[RuntimeException] should be thrownBy {
        transaction {
          transaction {
            transaction.onRollback(localized = true) {
              local += 1
            }
            transaction.onRollback(localized = false) {
              global += 1
            }
            local should equal(0)
            global should equal(0)
            throw new RuntimeException
          }
        }
      }
      local should equal(1)
      global should equal(1)
    }
  }

  def reset() = {
    local = 0
    global = 0
  }
}