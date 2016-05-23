package spec

import org.powerscala.collection.HierarchicalIterator
import org.scalatest.{Matchers, WordSpec}

class HierarchicalIteratorSpec extends WordSpec with Matchers {
  "HierarchicalIterator" should {
    "create a simple structure and organize back out properly" in {
      val root = new Test("root", List(
        new Test("Numbers", List(
          new Test("One"),
          new Test("Two"),
          new Test("Three")
        )),
        new Test("Animals", List(
          new Test("Cute", List(
            new Test("Pandas"),
            new Test("Bunnies"),
            new Test("Dogs")
          ))
        )),
        new Test("Nothing")
      ))
      val iterator = new HierarchicalIterator[Test](root, (t: Test) => t.children.iterator)
      val names = iterator.toList.map(_.name)
      names should equal(List("root", "Numbers", "One", "Two", "Three", "Animals", "Cute", "Pandas", "Bunnies", "Dogs", "Nothing"))
    }
  }

  class Test(val name: String, val children: List[Test] = Nil)
}