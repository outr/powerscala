package org.powerscala.datastore

import impl.mongodb.MongoDBDatastore
import java.util

import org.scalatest.WordSpec
import org.scalatest.matchers.ShouldMatchers
import org.powerscala.Precision
import query.{Queryable, Field}

/**
 * @author Matt Hicks <mhicks@powerscala.org>
 */
class DatastoreSpec extends WordSpec with ShouldMatchers {
  lazy val datastore = new MongoDBDatastore("localhost", 27017, "DatastoreSpec")

  "Datastore" when {
    "using mongodb" should {
      val (session, created) = datastore.createOrGet()
      datastore.session.delete()
      test(session) {
        if (created) {
          datastore.session.delete()
          datastore.disconnect()
        }
      }
    }
  }

  def test(session: DatastoreSession)(finish: => Unit) = {
    val c1 = session[Test1]
    val c2 = session[Test2]
    val c3 = session[Test3]
    val c4 = session[Test4]
    val cb = session[TestBase]
    val c5 = session[Test5]
    val c7 = session[Test7]
    val t1 = Test1("test1")
    "have no objects in the database" in {
      println("Checking db size...")
      c1.size should equal(0)
    }
    "insert an object" in {
      c1.persist(t1)
    }
    "query the object back out" in {
      val results = c1.toList
      results.size should equal(1)
      results.head should equal(t1)
    }
    "delete the object" in {
      c1.delete(t1)
      c1.size should equal(0)
    }
    "insert five Test1 objects" in {
      val o1 = Test1("One")
      val o2 = Test1("Two")
      val o3 = Test1("Three")
      val o4 = Test1("Four")
      val o5 = Test1("Five")
      c1.persist(o1, o2, o3, o4, o5)
    }
    "query 'Three' back out" in {
      val query = c1.query.filter(Test1.name equal "Three")
      val results = query.toList
      results.size should equal(1)
      results.head.name should equal("Three")
    }
    "query 'Four' back out by example" in {
      val example = Test1("Four")
      val results = c1.byExample(example).toList
      results.size should equal(1)
      results.head.name should equal("Four")
    }
    "query items with name sorting ascending" in {
      val results = c1.query.sort(Test1.name.ascending).toList
      results.size should equal(5)
      results.head.name should equal("Five")
      results.last.name should equal("Two")
    }
    "query items with name sorting descending" in {
      val results = c1.query.sort(Test1.name.descending).toList
      results.size should equal(5)
      results.head.name should equal("Two")
      results.last.name should equal("Five")
    }
    "query items limiting the results" in {
      val results = c1.query.limit(2).toList
      results.size should equal(2)
    }
    "query items with name sorting ascending with skipping and limiting" in {
      val results = c1.query.sort(Test1.name.ascending).limit(3).skip(1)
      results.size should equal(3)
      results.head.name should equal("Four")
      results.last.name should equal("Three")
    }
    "insert five Test2 objects" in {
      val o1 = Test2("One")
      val o2 = Test2("Two")
      val o3 = Test2("Three")
      val o4 = Test2("Four")
      val o5 = Test2("Five")
      c2.persist(o1, o2, o3, o4, o5)
    }
    "properly differentiate between class types via all" in {
      c1.size should equal(5)
      c2.size should equal(5)
    }
    "validate events occurring at the Datastore level" in {
      c1.parent should equal(session)
      session.parent should equal(datastore)
      var collectionEvents = 0
      var sessionEvents = 0
      var datastoreEvents = 0
      val collectionListener = c1.listeners.synchronous {
        case event => collectionEvents += 1
      }
      val sessionListener = session.listeners.filter.child.synchronous {
        case event => sessionEvents += 1
      }
      val datastoreListener = datastore.listeners.filter.descendant().synchronous {
        case event => datastoreEvents += 1
      }
      c1.persist(Test1("Testing listener"))
      collectionEvents should equal(1)
      sessionEvents should equal(1)
      datastoreEvents should equal(1)
      c1.listeners -= collectionListener
      session.listeners -= sessionListener
      datastore.listeners -= datastoreListener
    }
    "properly differentiate between class types via query" in {
      val results1 = c1.query.filter(Test1.name equal "Three").toList
      val results2 = c2.query.filter(Test2.name equal "Three").toList
      results1.size should equal(1)
      results2.size should equal(1)
    }
    "properly utilize 'in' to query two items" in {
      val results = c2.query.filter(Test2.name.in("Two", "Three")).sort(Test2.name.ascending).toList
      results.size should equal(2)
      results.head.name should equal("Three")
      results.tail.head.name should equal("Two")
    }
    "properly utilize 'or' to query two items" in {
      val results = c2.query.filter(Test2.or(Test2.name equal("One"), Test2.name equal("Five"))).sort(Test2.name.ascending).toList
      results.size should equal(2)
      results.head.name should equal("Five")
      results.tail.head.name should equal("One")
    }
    "persist a Test3 with an EnumEntry" in {
      c3.persist(Test3("first", Precision.Milliseconds))
    }
    "query back one Test3 with an EnumEntry" in {
      val t = c3.head
      t.name should equal("first")
      t.precision should equal(Precision.Milliseconds)
    }
    "insert Test4 with lazy value" in {
      val t1 = Test1("lazy one")
      val t4 = Test4("fourth", t1)
      c4.persist(t4)
      c1.query.filter(Test1.name equal "lazy one").size should equal(1)
      val t4Again = c4.head
      t4Again.name should equal("fourth")
      t4Again.t1.name should equal("lazy one")
    }
    "insert a few Test5 entries for 'and' query" in {
      c5.persist(Test5("One", 1), Test5("Two", 2), Test5("Three", 3), Test5("Two", 3))
    }
    "properly utilize 'and' to query one item" in {
      val results = c5.query.filter(Test5.and(Test5.name equal("Two"), Test5.age equal(2))).toList
      results.size should equal(1)
      val t = results.head
      t.name should equal("Two")
      t.age should equal(2)
    }
    "properly query with regular expression" in {
      val results = c5.query.filter(Test5.name regex("T.*")).sort(Test5.name.ascending).sort(Test5.age.ascending).toList
      results.size should equal(3)
      val r1 = results.head
      val r2 = results.tail.head
      val r3 = results.tail.tail.head
      r1.name should equal("Three")
      r1.age should equal(3)
      r2.name should equal("Two")
      r2.age should equal(2)
      r3.name should equal("Two")
      r3.age should equal(3)
    }
    "insert Test5 in TestBase collection" in {
      val t5 = Test5("test5", 5)
      cb.persist(t5)
    }
    "insert Test6 in TestBase collection" in {
      val t6 = Test6("test6", "six")
      cb.persist(t6)
    }
    "query items out of TestBase collection" in {
      val results = cb.toList
      results.length should equal(2)
      val t5 = results.collect {
        case t: Test5 => t
      }.head
      val t6 = results.collect {
        case t: Test6 => t
      }.head
      t5.name should equal("test5")
      t5.age should equal(5)
      t6.name should equal("test6")
      t6.ref should equal("six")
    }
    "validate persistence states" in {
      val t = Test1("persistanceStateTest")
      c1.isPersisted(t) should equal(false)
      c1.persist(t)
      c1.isPersisted(t) should equal(true)
      val t1 = c1.query.filter(Test1.name equal "persistanceStateTest").head
      c1.isPersisted(t1) should equal(true)
      val t2 = t1.copy("persistanceStateTest2")
      c1.isPersisted(t2) should equal(true)
      c1.delete(t2)
      c1.isPersisted(t) should equal(false)
    }
    "validate persisting and querying Identifiables within Identifiables" in {
      val t8 = Test8("test8", Array(1.toByte, 2.toByte, 3.toByte))
      val t7 = Test7("t7", t8)
      c7.persist(t7)
      val queried = c7.head
      queried.test.bytes should equal(t7.test.bytes)
      queried.test.id should equal(t7.test.id)
    }
    "properly sub-query" in {
      val results = c7.query.filter(Test7.test(Test8.name equal("test8"))).toList
      results.length should equal(1)
    }
    // TODO: sub-query support: Test4.t1.name (lazy) and Test7.names.contains("Matt")
    "close resources in" in {
      finish
    }
  }
}

trait Test {
  def name: String
}

case class Test1(name: String, id: util.UUID = util.UUID.randomUUID()) extends Identifiable with Test

object Test1 extends Queryable[Test1] {
  val name = Field.string[Test1]("name")
  val id = Field.id[Test1]
}

case class Test2(name: String, id: util.UUID = util.UUID.randomUUID()) extends Identifiable

object Test2 extends Queryable[Test2] {
  val name = Field.string[Test2]("name")
  val id = Field.id[Test2]
}

case class Test3(name: String, precision: Precision, id: util.UUID = util.UUID.randomUUID()) extends Identifiable

object Test3 extends Queryable[Test3] {
  val name = Field.string[Test3]("name")
  val precision = Field.basic[Test3, Precision]("precision")
  val id = Field.id[Test3]
}

case class Test4(name: String, t1: Lazy[Test1], id: util.UUID = util.UUID.randomUUID()) extends Identifiable

object Test4 extends Queryable[Test3] {
  val name = Field.string[Test4]("name")
  val t1 = Field.embedded[Test4, Test1]("t1")
  val id = Field.id[Test4]
}

trait TestBase extends Identifiable {
  def name: String
}

case class Test5(name: String, age: Int, id: util.UUID = util.UUID.randomUUID()) extends TestBase

object Test5 extends Queryable[Test5] {
  val name = Field.string[Test5]("name")
  val age = Field.int[Test5]("age")
  val id = Field.id[Test5]
}

case class Test6(name: String, ref: String, id: util.UUID = util.UUID.randomUUID()) extends TestBase

case class Test7(name: String, test: Test8, id: util.UUID = util.UUID.randomUUID()) extends Identifiable

object Test7 extends Queryable[Test7] {
  val name = Field.string[Test7]("name")
  val test = Field.embedded[Test7, Test8]("test")
  val id = Field.id[Test7]
}

case class Test8(name: String, bytes: Array[Byte], id: util.UUID = util.UUID.randomUUID()) extends Identifiable

object Test8 extends Queryable[Test8] {
  val name = Field.string[Test8]("name")
//  val bytes = Field[Test8, Array[Byte]]("bytes")
  val id = Field.id[Test8]
}