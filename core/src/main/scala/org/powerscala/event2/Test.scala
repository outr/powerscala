package org.powerscala.event2

/**
 * @author Matt Hicks <matt@outr.com>
 */
object Test extends Listenable {
  def strings = StringProcessor

  def main(args: Array[String]): Unit = {
    strings.add(this) {
      case "Hello" => Some("World")
      case _ => None
    }

    val r1 = strings.fire("Hello", this)
    val r2 = strings.fire("Goodbye", this)
    println(s"R1: $r1, R2: $r2")
  }
}

object StringProcessor extends OptionalProcessor[String, String]