package org.powerscala.search

import org.apache.lucene.document.Document
import org.apache.lucene.index.Term
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.util.Version

/**
 * @author Matt Hicks <matt@outr.com>
 */
trait TypedSearch[T] {
  def search: Search

  def term(t: T): Term

  def createDocument(t: T): Document

  def update(t: T, commit: Boolean = true) = {
    val term = this.term(t)
    val document = createDocument(t)
    search.update(term, document, commit = commit)
  }

  def delete(t: T, commit: Boolean = true) = {
    val term = this.term(t)
    search.delete(term, commit = commit)
  }

  def query() = search.process {
    case searcher => {
      val parser = new QueryParser(Version.LUCENE_42, "", search.analyzer)
      val q = parser.parse("")
//      q.
//      searcher.search()
    }
  }
}
