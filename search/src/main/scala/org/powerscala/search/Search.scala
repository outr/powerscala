package org.powerscala.search

import java.io.File
import org.apache.lucene.store.{NativeFSLockFactory, FSDirectory}
import org.apache.lucene.index.{Term, IndexWriter, IndexWriterConfig}
import org.apache.lucene.util.Version
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.search.{IndexSearcher, NRTManagerReopenThread, SearcherFactory, NRTManager}
import org.apache.lucene.document.Document

/**
 * @author Matt Hicks <matt@outr.com>
 */
class Search(path: File,
             val analyzer: Analyzer = new StandardAnalyzer(Version.LUCENE_42),
             refreshRate: Double = 5.0,
             maximumDelay: Double = 0.1) {
  private val directory = FSDirectory.open(path, new NativeFSLockFactory())
  private val config = new IndexWriterConfig(Version.LUCENE_42, analyzer)
  private val writer = new IndexWriter(directory, config)

  private val trackingIndexWriter = new NRTManager.TrackingIndexWriter(writer)
  private val applyAllDeletes = true
  private val manager = new NRTManager(trackingIndexWriter, new SearcherFactory, applyAllDeletes)
  private val reopenThread = new NRTManagerReopenThread(manager, refreshRate, maximumDelay) {
    setName("NRTManagerReopenThread")
    setPriority(math.min(Thread.currentThread().getPriority + 2, Thread.MAX_PRIORITY))
    setDaemon(true)
  }

  reopenThread.start()

  /**
   * Acquires an up-to-date IndexSearcher to allow searching with and then releases after the supplied function is
   * finished with it.
   *
   * @param f function to utilize the IndexSearcher
   * @tparam T the return value from the function
   * @return T
   */
  def process[T](f: IndexSearcher => T): T = {
    val searcher = manager.acquire()
    try {
      f(searcher)
    } finally {
      manager.release(searcher)
    }
  }

  protected def write[T](f: IndexWriter => T): T = f(writer)

  def add(document: Document, commit: Boolean = true) = write {
    case w => {
      w.addDocument(document)
      if (commit) {
        w.commit()
      }
    }
  }

  def update(term: Term, document: Document, commit: Boolean = true) = write {
    case w => {
      w.updateDocument(term, document)
      if (commit) {
        w.commit()
      }
    }
  }

  def delete(term: Term, commit: Boolean = true) = write {
    case w => {
      w.deleteDocuments(term)
      if (commit) {
        w.commit()
      }
    }
  }

  def dispose() = {
    reopenThread.close()
    manager.close()
    writer.commit()
    writer.close(true)
  }
}
