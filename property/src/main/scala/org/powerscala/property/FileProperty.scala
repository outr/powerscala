package org.powerscala.property

import java.io.{FileWriter, File}
import io.Source

import org.powerscala.json._
import org.powerscala.concurrent.Executor
import java.util.concurrent.atomic.AtomicBoolean

/**
 * FileProperty allows mixing with a StandardProperty to persist and load from a File with the value of this property as
 * it changes.
 *
 * Override the checkForChanges to enable periodic checking the file for modification and auto-loading into the property
 *
 * @author Matt Hicks <mhicks@outr.com>
 */
trait FileProperty[T] {
  this: StandardProperty[T] =>

  /**
   * The bound file. Will be created upon first property change if it doesn't already exist.
   */
  def file: File

  /**
   * Defaults to 0.0 (not enabled), but setting to any positive value will check the timestamp of the file every
   * 'checkForChanges' in seconds and load into the property upon change.
   */
  def checkForChanges: Double = 0.0

  private val changing = new AtomicBoolean(false)
  private var lastModified: Long = 0L

  // Load at init if the file exists
  load()

  // Watch the file for changes if enabled
  if (checkForChanges != 0.0) {
    Executor.schedule(checkForChanges) {
      if (file.exists() && file.lastModified() != lastModified) {
        load()
      }
    }
  }

  // Persist back to file upon change
  onChange {
    if (changing.compareAndSet(false, true)) {
      try {
        val updated = value
        if (updated == null) {
          file.delete()
        } else {
          val content = generate(updated)
          val writer = new FileWriter(file)
          try {
            writer.write(content)
          } finally {
            writer.flush()
            writer.close()
          }
        }
      } finally {
        changing.set(false)
      }
    }
  }

  /**
   * Loads the value of the property from the bound file.
   */
  def load() = if (file.exists()) {
    if (changing.compareAndSet(false, true)) {
      try {
        val source = Source.fromFile(file)
        try {
          val content = source.mkString
          val loaded = parse[T](content)(manifest)
          lastModified = file.lastModified()
          value = loaded
        } finally {
          source.close()
        }
      } finally {
        changing.set(false)
      }
    }
  }
}