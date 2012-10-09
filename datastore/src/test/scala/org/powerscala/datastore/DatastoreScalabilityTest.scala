package org.powerscala.datastore

import impl.mongodb.MongoDBDatastore
import java.util
import query.Field

import scala.math._
import org.powerscala.concurrent.Time
import org.powerscala.Logging

/**
 * @author Matt Hicks <mhicks@powerscala.org>
 */
object DatastoreScalabilityTest extends Logging {
  lazy val datastore = new MongoDBDatastore()
  val items = 1000

  def main(args: Array[String]): Unit = {
    val took = Time.elapsed {
//      persistItems()
//      verifyOutOfMemory()
      iterateItems()
//      itemsAsList()
    }
    info("Completed successfully in %s seconds".format(took))
  }

  def persistItems() = {
    datastore {
      case session => {
        val collection = session[TestObject]
        (0 until items).foreach {
          case i => {
            info("Persisting: %s".format(i))
            collection.persist(TestObject("item%s".format(i)))
          }
        }
      }
    }
  }

  def iterateItems() = {
    datastore {
      case session => {
        val collection = session[TestObject]
        var count = 0
        collection.foreach {
          case item => count += 1
        }
        info("Found %s items".format(count))
      }
    }
  }

  def itemsAsList() = {
    val items = datastore {
      case session => session[TestObject].toList
    }
    info("Items: %s".format(items.size))
  }

  def verifyOutOfMemory() = {
    (0 until 1000).map(i => TestObject("item%s".format(i)))
  }
}

case class TestObject(name: String, data: String = TestObject.generateData, id: util.UUID = util.UUID.randomUUID()) extends Identifiable

object TestObject {
  val name = Field.string[TestObject]("name")
  val data = Field.string[TestObject]("data")
  val id = Field.id[TestObject]

  def generateData = "".padTo(1024 * 512, random.toString).mkString("-")
}