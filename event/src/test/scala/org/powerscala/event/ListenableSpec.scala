package org.powerscala.event

import org.scalatest.WordSpec
import org.scalatest.matchers.ShouldMatchers
import org.powerscala.event.processor.{ListProcessor, InterceptProcessor, UnitProcessor, OptionProcessor}

/**
 * @author Matt Hicks <mhicks@powerscala.org>
 *         Date: 12/3/11
 */
class ListenableSpec extends WordSpec with ShouldMatchers {
  "Listenable" when {
    "using UnitProcessor" should {
      var received = false
      val listenable = new TestListenable
      val listener = listenable.basic.add(listenable) {
        case s => received = true
      }
      "have one listener" in {
        listenable.listeners().length should equal(1)
      }
      "fire an event" in {
        listenable.basic.fire("Test", listenable)
      }
      "have received the event on the listener" in {
        received should equal(true)
      }
      "remove the listener" in {
        listenable.listeners -= listener
      }
      "have no listeners" in {
        listenable.listeners().isEmpty should equal(true)
      }
    }
    "using OptionProcessor" should {
      val listenable = new TestListenable
      "add a listener" in {
        listenable.strings.add(listenable) {
          case s if (s == "Hello") => Some("World")
          case _ => None
        }
      }
      "fire an event with 'Test' and get None back" in {
        listenable.strings.fire("Test", listenable) should equal(None)
      }
      "fire an event with 'Hello' and get Some('World') back" in {
        listenable.strings.fire("Hello", listenable) should equal(Some("World"))
      }
      "clear all listeners" in {
        listenable.listeners.clear()
      }
      "have no listeners" in {
        listenable.listeners().isEmpty should equal(true)
      }
    }
    "using InterceptProcessor" should {
      val listenable = new TestListenable
      "add a listener" in {
        listenable.intercept.add(listenable) {
          case i if (i <= 0) => Intercept.Stop
          case _ => Intercept.Continue
        }
      }
      "fire an event with 5 and get Continue back" in {
        listenable.intercept.fire(5, listenable) should equal(Intercept.Continue)
      }
      "fire an event with -1 and get Stop back" in {
        listenable.intercept.fire(-1, listenable) should equal(Intercept.Stop)
      }
    }
    "using ListProcessor" should {
      val listenable = new TestListenable
      "add several listeners" in {
        listenable.list.add(listenable) {
          case s => Some(s"Characters: ${s.length}")
        }
        listenable.list.add(listenable) {
          case s => Some(s"Reverse: ${s.reverse}")
        }
        listenable.list.add(listenable) {
          case s => try {
            Some(s"Is a number: ${s.toInt}")
          } catch {
            case t: Throwable => None
          }
        }
      }
      "fire an event with 'Hello' and get back expected list" in {
        listenable.list.fire("Hello", listenable) should equal(List("Characters: 5", "Reverse: olleH"))
      }
      "fire an event with '123' and get back expected list" in {
        listenable.list.fire("123", listenable) should equal(List("Characters: 3", "Reverse: 321", "Is a number: 123"))
      }
    }
    "using Change" should {
      val listenable = new TestListenable
      var from = -1
      var to = -1
      "add a listener" in {
        listenable.change.add(listenable) {
          case c => {
            from = c.oldValue
            to = c.newValue
          }
        }
      }
      "fire an event with Change(5, 10)" in {
        listenable.change.fire(Change(5, 10), listenable)
        from should equal(5)
        to should equal(10)
      }
    }
    "listener stopping processing via Routing.Stop" should {
      var received1 = false
      var received2 = false
      val listenable = new TestListenable
      "add the first listener" in {
        listenable.basic.add(listenable) {
          case s => {
            received1 = true
            EventState.current.stopPropagation()
          }
        }
      }
      "add the second listener" in {
        listenable.basic.add(listenable) {
          case s => received2 = true
        }
      }
      "fire an event" in {
        listenable.basic.fire("Test", listenable)
      }
      "message should have been received on first listener" in {
        received1 should equal(true)
      }
      "message should not have been received on second listener" in {
        received2 should equal(false)
      }
    }
  }
}

class TestListenable extends Listenable {
  val basic = new UnitProcessor[String]
  def strings = StringProcessor
  val intercept = new InterceptProcessor[Int]
  val list = new ListProcessor[String, String]
  val change = new UnitProcessor[Change[Int]]
}

object StringProcessor extends OptionProcessor[String, String]