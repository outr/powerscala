package org.powerscala.datastore

import impl.mongodb.MongoDBDatastore

import org.scalatest.{Matchers, WordSpec}
import org.powerscala.{Priority, Precision}
import query.{Queryable, Field}
import java.util.UUID
import org.powerscala.hierarchy.event.Descendants

/**
 * @author Matt Hicks <mhicks@powerscala.org>
 */
class DatastoreSpec extends WordSpec with Matchers {
  lazy val datastore = new MongoDBDatastore("localhost", 27017, "DatastoreSpec")
  datastore.register(classOf[BaseTrait])

//  Logging.root.configure {
//    case l => l.withLevel(Level.Debug)
//  }

  "Datastore" when {
    "using mongodb" should {
      val created = datastore.createSessionForThread()
      datastore.session.delete()
      test(datastore.session) {
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
    val c9 = session[Test9]
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
    "query just the ids back out" in {
      val results = c1.query.ids.toList
      results.size should equal(5)
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
      session.datastore should equal(datastore)
      var collectionPersistEvents = 0
      var collectionDeleteEvents = 0
      var sessionPersistEvents = 0
      var sessionDeleteEvents = 0
      var datastorePersistEvents = 0
      var datastoreDeleteEvents = 0
      val collectionPersistListener = c1.persists.on {
        case event => collectionPersistEvents += 1
      }
      val collectionDeleteListener = c1.deletes.on {
        case event => collectionDeleteEvents += 1
      }
      val sessionPersistListener = session.persists.listen(Priority.Normal, Descendants) {
        case event => sessionPersistEvents += 1
      }
      val sessionDeleteListener = session.deletes.listen(Priority.Normal, Descendants) {
        case event => sessionDeleteEvents += 1
      }
      val datastorePersistListener = datastore.persists.listen(Priority.Normal, Descendants) {
        case event => datastorePersistEvents += 1
      }
      val datastoreDeleteListener = datastore.deletes.listen(Priority.Normal, Descendants) {
        case event => datastoreDeleteEvents += 1
      }
      val t = Test1("Testing listener")
      c1.persist(t)
      collectionPersistEvents should equal(1)
      collectionDeleteEvents should equal(0)
      sessionPersistEvents should equal(1)
      sessionDeleteEvents should equal(0)
      datastorePersistEvents should equal(1)
      datastoreDeleteEvents should equal(0)
      c1.delete(t)
      collectionPersistEvents should equal(1)
      collectionDeleteEvents should equal(1)
      sessionPersistEvents should equal(1)
      sessionDeleteEvents should equal(1)
      datastorePersistEvents should equal(1)
      datastoreDeleteEvents should equal(1)
      c1.listeners -= collectionPersistListener
      c1.listeners -= collectionDeleteListener
      session.listeners -= sessionPersistListener
      session.listeners -= sessionDeleteListener
      datastore.listeners -= datastorePersistListener
      datastore.listeners -= datastoreDeleteListener
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
    val t1UUID = UUID.randomUUID()
    "insert Test4 with lazy value" in {
      val t1 = Test1("lazy one", t1UUID)
      val t4 = Test4("fourth", t1)
      c4.persist(t4)
      c1.query.filter(Test1.name equal "lazy one").size should equal(1)
      val t4Again = c4.head
      t4Again.name should equal("fourth")
      t4Again.t1.name should equal("lazy one")
    }
    "query for a lazy value by id" in {
      val results = c4.query.filter(Test4.t1.sub(Lazy.id[Test1] equal t1UUID)).toList
      results.length should equal(1)
      results.head.name should equal("fourth")
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
    val oneUUID = UUID.randomUUID()
    "properly persist a LazyList" in {
      val t9 = Test9("TestLazyList", LazyList(Test1("One", oneUUID), Test1("Two"), Test1("Three")))
      c9.persist(t9)
    }
    "properly load a LazyList" in {
      val t9 = c9.head
      t9.name should equal("TestLazyList")
      t9.list.loaded should equal(false)
      t9.list().length should equal(3)
      t9.list()(0).name should equal("One")
      t9.list()(1).name should equal("Two")
      t9.list()(2).name should equal("Three")
    }
    "properly query a LazyList with a type-safe query" in {
      val results = c9.query.filter(Test9.list.sub(LazyList.id[Test1] equal oneUUID)).toList
      results.length should equal(1)
      results.head.name should equal("TestLazyList")
    }
    "properly load specific fields from the datastore" in {
      val t9 = c9.query.fields(Test9.name).head
      t9.name should equal("TestLazyList")
      t9.list should equal(null)
      t9.id should not equal(null)
    }
    "alias properly when referencing subclasses" in {
      datastore.collectionNameForClass(classOf[BaseTrait]) should equal("BaseTrait")
      datastore.collectionNameForClass(classOf[FirstCase]) should equal("BaseTrait")
      datastore.collectionNameForClass(classOf[SecondCase]) should equal("BaseTrait")
    }
    "insert multiple classes for aliases" in {
      datastore {
        case session => session[FirstCase].persist(FirstCase("One"), FirstCase("Two"), FirstCase("Three"))
      }
      datastore {
        case session => session[SecondCase].persist(SecondCase(1), SecondCase(2), SecondCase(3))
      }
    }
    "verify that BaseTrait has six entries" in {
      datastore {
        case session => session[BaseTrait].size should equal(6)
      }
    }
    "verify that querying of specific class type only returns those types" in {
      datastore {
        case session => session[FirstCase].size should equal(3)
      }
      datastore {
        case session => session[SecondCase].size should equal(3)
      }
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

case class Test1(name: String, id: UUID = UUID.randomUUID()) extends Identifiable with Test

object Test1 extends Queryable[Test1] {
  val name = Field.string[Test1]("name")
  val id = Field.id[Test1]
}

case class Test2(name: String, id: UUID = UUID.randomUUID()) extends Identifiable

object Test2 extends Queryable[Test2] {
  val name = Field.string[Test2]("name")
  val id = Field.id[Test2]
}

case class Test3(name: String, precision: Precision, id: UUID = UUID.randomUUID()) extends Identifiable

object Test3 extends Queryable[Test3] {
  val name = Field.string[Test3]("name")
  val precision = Field.basic[Test3, Precision]("precision")
  val id = Field.id[Test3]
}

case class Test4(name: String, t1: Lazy[Test1], id: UUID = UUID.randomUUID()) extends Identifiable

object Test4 extends Queryable[Test3] {
  val name = Field.string[Test4]("name")
  val t1 = Field.lzy[Test4, Test1]("t1")
  val id = Field.id[Test4]
}

trait TestBase extends Identifiable {
  def name: String
}

case class Test5(name: String, age: Int, id: UUID = UUID.randomUUID()) extends TestBase

object Test5 extends Queryable[Test5] {
  val name = Field.string[Test5]("name")
  val age = Field.int[Test5]("age")
  val id = Field.id[Test5]
}

case class Test6(name: String, ref: String, id: UUID = UUID.randomUUID()) extends TestBase

case class Test7(name: String, test: Test8, id: UUID = UUID.randomUUID()) extends Identifiable

object Test7 extends Queryable[Test7] {
  val name = Field.string[Test7]("name")
  val test = Field.embedded[Test7, Test8]("test")
  val id = Field.id[Test7]
}

case class Test8(name: String, bytes: Array[Byte], id: UUID = UUID.randomUUID()) extends Identifiable

object Test8 extends Queryable[Test8] {
  val name = Field.string[Test8]("name")
//  val bytes = Field[Test8, Array[Byte]]("bytes")
  val id = Field.id[Test8]
}

case class Test9(name: String, list: LazyList[Test1], id: UUID = UUID.randomUUID()) extends Identifiable

object Test9 extends Queryable[Test9] {
  val name = Field.string[Test9]("name")
  val list = Field.lazyList[Test9, Test1]("list")
  val id = Field.id[Test9]
}

trait BaseTrait extends Identifiable

case class FirstCase(name: String, id: UUID = UUID.randomUUID()) extends BaseTrait

case class SecondCase(value: Int, id: UUID = UUID.randomUUID()) extends BaseTrait