package org.powerscala.search

import org.scalatest.{Matchers, WordSpec}
import org.apache.lucene.document.{TextField, StringField, Field, IntField}

import scala.language.implicitConversions
import org.apache.lucene.facet.taxonomy.CategoryPath
import org.apache.lucene.facet.search.FacetsCollector
import org.apache.lucene.search.MultiCollector


/**
 * @author Matt Hicks <matt@outr.com>
 */
class SearchSpec extends WordSpec with Matchers {
  implicit def image2DocumentUpdate(i: Image) = i.toDocumentUpdate

  val search = new Search("description")
  "Search" when {
    "testing simple search" should {
      "insert some simple records without any tags" in {
        search.update(Image(1, "butterfly", "this is a test image of a butterfly."))
        search.update(Image(2, "dragonfly", "this is a test image of a dragonfly."))
        search.update(Image(3, "unicorn", "this is a test image of a unicorn."))
        search.update(Image(4, "rainbow", "this is a test image of a rainbow (not a fly)."))
        search.update(Image(5, "fly", "this is a test image of a fly."))
        search.commit()
      }
      "query all records back getting the proper number" in {
        val results = search.query.run()
        results.total should equal(5)
      }
      "query everything with 'fly' in the description" in {
        val results = search.query("*fly").run()
        results.total should equal(4)
      }
      "query everything with 'fly' in the name" in {
        val results = search.query("name:*fly").run()
        results.total should equal(3)
      }
    }
    "testing modifying existing records" should {
      "update some simple records with tags" in {
        search.update(Image(1, "butterfly", "this is a test image of a butterfly.", List("flying", "butter", "insect", "image")))
        search.update(Image(2, "dragonfly", "this is a test image of a dragonfly.", List("flying", "dragon", "insect", "image")))
        search.update(Image(3, "unicorn", "this is a test image of a unicorn.", List("animal", "mythical", "horse", "image")))
        search.update(Image(4, "rainbow", "this is a test image of a rainbow (not a fly).", List("light", "colorful", "pretty", "image")))
        search.update(Image(5, "fly", "this is a test image of a fly.", List("flying", "insect", "image")))
        search.commit()
      }
      "query all records back getting the proper number" in {
        val results = search.query.run()
        results.total should equal(5)
      }
      "query everything with 'fly' in the name along with tags" in {
        val results = search.query("name:*fly").facet("tag").run()
        results.total should equal(3)
        results.facetResults.size should equal(1)
        val facetsOption = results.facets("tag")
        facetsOption shouldNot equal(None)
        val facets = facetsOption.get.toVector
        facets.size should equal(5)
        facets(0).name should equal("image")
        facets(0).value should equal(3.0)
        facets(1).name should equal("insect")
        facets(1).value should equal(3.0)
        facets(2).name should equal("flying")
        facets(2).value should equal(3.0)
        facets(3).name should equal("dragon")
        facets(3).value should equal(1.0)
        facets(4).name should equal("butter")
        facets(4).value should equal(1.0)
      }
    }
    "testing pagination" should {
      var results: SearchResults = null

      "query back multiple pages" in {
        results = search.query.limit(2).facet("tag", 100).run()
        results.total should equal(5)
        results.pageSize should equal(2)
        results.scoreDocs.length should equal(2)
        results.pageStart should equal(0)
        results.page should equal(0)
        results.pages should equal(3)
        results.facets("tag").get.size should equal(11)
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
  }
}

case class Image(id: Int, name: String, description: String, tags: List[String] = Nil) {
  def toDocumentUpdate = {
    DocumentUpdate(
      List(
        new StringField("id", id.toString, Field.Store.YES),
        new StringField("name", name, Field.Store.YES),
        new TextField("description", description, Field.Store.YES),
        new TextField("tags", tags.mkString(", "), Field.Store.YES)
      ),
      tags.map(t => new CategoryPath("tag", t))
    )
  }
}