package org.powerscala.search

import java.util

import com.spatial4j.core.distance.DistanceUtils
import com.spatial4j.core.shape.Point
import org.apache.lucene.facet.{LabelAndValue, FacetField}
import org.apache.lucene.index.Term
import org.apache.lucene.search._
import org.apache.lucene.util.Bits
import org.scalatest.{Matchers, WordSpec}
import org.apache.lucene.document.{IntField, TextField, StringField, Field}

import scala.language.implicitConversions

/**
 * @author Matt Hicks <matt@outr.com>
 */
class SearchSpec extends WordSpec with Matchers {
  implicit def image2DocumentUpdate(i: Image): DocumentUpdate = i.toDocumentUpdate
  implicit def person2DocumentUpdate(p: Person): DocumentUpdate = p.toDocumentUpdate

  val imageSearch = new Search("description", sortedFields = List(SortedField("name")))
  imageSearch.facetsConfig.setMultiValued("tag", true)
  val personSearch = new Search("id")
  personSearch.configureSpatialStrategy()
  "Search" when {
    "testing simple search" should {
      "insert some simple records without any tags" in {
        imageSearch.update(Image(1, "butterfly", "this is a test image of a butterfly."))
        imageSearch.update(Image(2, "dragonfly", "this is a test image of a dragonfly."))
        imageSearch.update(Image(3, "unicorn", "this is a test image of a unicorn."))
        imageSearch.update(Image(4, "rainbow", "this is a test image of a rainbow (not a fly)."))
        imageSearch.update(Image(5, "fly", "this is a test image of a fly."))
        imageSearch.commit()
      }
      "query all records back getting the proper number" in {
        val results = imageSearch.query.run()
        results.total should equal(5)
      }
      "query everything with 'fly' in the description" in {
        val results = imageSearch.query("*fly").run()
        results.total should equal(4)
      }
      "query everything with 'fly' in the name" in {
        val results = imageSearch.query("name:*fly").run()
        results.total should equal(3)
      }
      "query everything starting with 'dragon' in the name" in {
        val results = imageSearch.query("name:dragon*").run()
        results.total should equal(1)
      }
      "query with term" in {
        val results = imageSearch.query.term("fly", "name").run()
        results.total should equal(1)
      }
      "query with regular expression" in {
        val results = imageSearch.query.regexp(".*fly", "name").run()
        results.total should equal(3)
      }
    }
    "testing modifying existing records" should {
      "update some simple records with tags" in {
        imageSearch.update(Image(1, "butterfly", "this is a test image of a butterfly.", List("flying", "butter", "insect", "image")))
        imageSearch.update(Image(2, "dragonfly", "this is a test image of a dragonfly.", List("flying", "dragon", "insect", "image")))
        imageSearch.update(Image(3, "unicorn", "this is a test image of a unicorn.", List("animal", "mythical", "horse", "image")))
        imageSearch.update(Image(4, "rainbow", "this is a test image of a rainbow (not a dragonfly).", List("light", "colorful", "pretty", "image")))
        imageSearch.update(Image(5, "fly", "this is a test image of a fly.", List("flying", "insect", "image")))
        imageSearch.commit()
      }
      "query all records back getting the proper number" in {
        val results = imageSearch.query.run()
        results.total should equal(5)
      }
      "query one record matching 'unicorn' and get the document" in {
        val results = imageSearch.query("unicorn").run()
        results.total should equal(1)
        val doc = results.doc(0)
        doc.get("id") should equal("3")
        doc.get("name") should equal("unicorn")
        doc.get("description") should equal("this is a test image of a unicorn.")
        doc.get("tags") should equal("animal mythical horse image")
      }
      "query all text that matches 'dragonfly'" in {
        val results = imageSearch.query("dragonfly").run()
        results.total should equal(2)
      }
      "query all tags with 'fly' in a tag" in {
        val results = imageSearch.query.facet("tag", filter = Some((lv: LabelAndValue) => lv.label.contains("fly"))).run()
        val entries = results.facetResults("tag").map(lv => s"${lv.label}: ${lv.value}").toSet
        entries should equal(Set("flying: 3"))
      }
    }
    "validating facets in search" should {
      "query everything with 'fly' in the name along with tags" in {
        val results = imageSearch.query("name:*fly").facet("tag").run()
        results.total should equal(3)
        results.facetResults.size should equal(1)
        val facets = results.facets("tag").toVector
        facets.size should equal(5)
        facets(0).label should equal("flying")
        facets(0).value should equal(3.0)
        facets(1).label should equal("insect")
        facets(1).value should equal(3.0)
        facets(2).label should equal("image")
        facets(2).value should equal(3.0)
        facets(3).label should equal("butter")
        facets(3).value should equal(1.0)
        facets(4).label should equal("dragon")
        facets(4).value should equal(1.0)
      }
    }
    "testing sort order" should {
      "query everything with 'fly' in the name sorted by name" in {
        val results = imageSearch.query("name:*fly").sort(new Sort(new SortField("name", SortField.Type.STRING))).run()
        results.total should equal(3)
        results.doc(0).get("name") should equal("butterfly")
        results.doc(1).get("name") should equal("dragonfly")
        results.doc(2).get("name") should equal("fly")
      }
      "query everything with 'fly' in the name sorted by name reversed" in {
        val results = imageSearch.query("name:*fly").sort(new Sort(new SortField("name", SortField.Type.STRING, true))).run()
        results.total should equal(3)
        results.doc(0).get("name") should equal("fly")
        results.doc(1).get("name") should equal("dragonfly")
        results.doc(2).get("name") should equal("butterfly")
      }
    }
    "testing pagination" should {
      var results: SearchResults = null

      "query back multiple pages" in {
        results = imageSearch.query.limit(2).facet("tag", 100).run()
        results.total should equal(5)
        results.pageSize should equal(2)
        results.scoreDocs.length should equal(2)
        results.pageStart should equal(0)
        results.page should equal(0)
        results.pages should equal(3)
        results.facets("tag").length should equal(11)
      }
      "go to the next page" in {
        results = results.page(1)
        results.total should equal(5)
        results.pageSize should equal(2)
        results.scoreDocs.length should equal(2)
        results.pageStart should equal(2)
        results.page should equal(1)
        results.pages should equal(3)
      }
      "go to the last page" in {
        results = results.page(2)
        results.total should equal(5)
        results.pageSize should equal(2)
        results.scoreDocs.length should equal(1)
        results.pageStart should equal(4)
        results.page should equal(2)
        results.pages should equal(3)
      }
      "go back to the first page" in {
        results = results.page(0)
        results.total should equal(5)
        results.pageSize should equal(2)
        results.scoreDocs.length should equal(2)
        results.pageStart should equal(0)
        results.page should equal(0)
        results.pages should equal(3)
      }
      "attempt to go backward a page" in {
        results = results.previousPage
        results.page should equal(0)
      }
      "go forward one page" in {
        results = results.nextPage
        results.page should equal(1)
      }
      "go forward another page" in {
        results = results.nextPage
        results.page should equal(2)
      }
      "attempt to go forward another page" in {
        results = results.nextPage
        results.page should equal(2)
      }
      "go backward one page" in {
        results = results.previousPage
        results.page should equal(1)
      }
    }
    "doing faceted drill-down" should {
      "return one result for 'butter'" in {
        val results = imageSearch.query.facet("tag", 100).drillDown("tag", "butter").run()
        results.total should equal(1)
        results.facets("tag").length should equal(3)
      }
      "return three results for 'flying'" in {
        val results = imageSearch.query.facet("tag", 100).drillDown("tag", "flying").run()
        results.total should equal(3)
        results.facets("tag").length should equal(4)
      }
      "combine tagged drill-down and search arguments" in {
        val results = imageSearch.query("dragonfly").facet("tag", 100).drillDown("tag", "flying").run()
        results.total should equal(1)
        results.facets("tag").length should equal(3)
      }
      "combine two drill-down args" in {
        val results = imageSearch.query.facet("tag", 100).drillDown("tag", "butter").drillDown("tag", "flying").run()
        results.total should equal(1)
      }
    }
    "doing spatial queries" should {
      "create some Person documents" in {
        personSearch.update(Person(10, 20, List(personSearch.point(33.77, -80.93))))
        personSearch.update(Person(11, 12, List(personSearch.point(-50.7693246, 60.9289094))))
        personSearch.update(Person(12, 80, List(personSearch.point(0.1, 0.1), personSearch.point(0, 100))))
        personSearch.commit()
      }
      "search for all Person documents sorted by distance ascending" in {
        val results = personSearch.query.sortFromPoint(60, -50).run()
        results.total should equal(3)
        results.doc(0).get("id") should equal("10")
        results.doc(1).get("id") should equal("12")
        results.doc(2).get("id") should equal("11")
      }
      "search for all Person documents within a circle" in {
        val results = personSearch.query.filterByCircle(33.0, -80.0, DistanceUtils.dist2Degrees(200, DistanceUtils.EARTH_MEAN_RADIUS_KM)).run()
        results.total should equal(1)
        results.doc(0).get("id") should equal("10")
      }
      "search for all Person documents within a circle using secondary point" in {
        val results = personSearch.query.filterByCircle(0.0, 100.0, DistanceUtils.dist2Degrees(200, DistanceUtils.EARTH_MEAN_RADIUS_KM)).run()
        results.total should equal(1)
        results.doc(0).get("id") should equal("12")
      }
      "search for all Person documents with an age between 10 and 30" in {
        val results = personSearch.query.intRange(10, 30, "age").run()
        results.total should equal(2)
        results.docs.map(doc => doc.get("age").toInt).toSet should equal(Set(12, 20))
      }
      "search for all Person documents with an age greater than 30" in {
        val results = personSearch.query.intRange(30, field = "age").run()
        results.total should equal(1)
        results.docs.map(doc => doc.get("age").toInt).toSet should equal(Set(80))
      }
    }
  }
}

case class Image(id: Int, name: String, description: String, tags: List[String] = Nil) {
  def toDocumentUpdate = {
    DocumentUpdate(
      List(
        new StringField("id", id.toString, Field.Store.YES),
        new TextField("name", name, Field.Store.YES),
        new TextField("description", description, Field.Store.YES),
        new TextField("tags", tags.mkString(" "), Field.Store.YES)
      ) ::: tags.map(t => new FacetField("tag", t)),
      Nil
    )
  }
}

case class Person(id: Int, age: Int, points: List[Point]) {
  def toDocumentUpdate = {
    DocumentUpdate(
      List(new StringField("id", id.toString, Field.Store.YES), new IntField("age", age, Field.Store.YES)),
      points
    )
  }
}