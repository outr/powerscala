package org.powerscala.bus

import org.scalatest.WordSpec
import org.scalatest.matchers.ShouldMatchers
import org.powerscala.Priority

/**
 * @author Matt Hicks <mhicks@powerscala.org>
 */
class BusSpec extends WordSpec with ShouldMatchers {
  val bus = new Bus()

  "Bus" when {
    "a listener is added" should {
      var received = false
      val node = Node() {
        case message if (message == "Test1") => received = true
      }
      "start with an empty Bus" in {
        bus.isEmpty should equal(true)
      }
      "successfully add the listener" in {
        bus.add(node)
      }
      "have one listener on the Bus" in {
        bus.length should equal(1)
      }
      "not update 'received' if the value is not 'Test1'" in {
        bus("TestBad")
        received should equal(false)
      }
      "update 'received' if the value is 'Test1'" in {
        bus("Test1")
        received should equal(true)
      }
      "remove the node" in {
        bus.remove(node)
      }
      "have no nodes" in {
        bus.length should equal(0)
      }
    }
    "two listeners are added" should {
      var firstReceived = false
      var secondReceived = false
      val first = Node(Priority.High) {
        case message => firstReceived = true
      }
      val second = Node() {
        case message => secondReceived = true
      }
      "start with an empty Bus" in {
        bus.isEmpty should equal(true)
      }
      "successfully add the listeners" in {
        bus.add(first)
        bus.add(second)
      }
      "fire a message" in {
        bus("Test2")
      }
      "receive the message on first properly" in {
        firstReceived should equal(true)
      }
      "receive the message on second propery" in {
        secondReceived should equal(true)
      }
      "remove both nodes" in {
        bus.remove(first)
        bus.remove(second)
        bus.isEmpty should equal(true)
      }
    }
    "two listeners are added with the first stopping" should {
      var firstReceived = false
      var secondReceived = false
      val first = Node(Priority.High) {
        case message => {
          firstReceived = true
          Routing.Stop
        }
      }
      val second = Node() {
        case message => secondReceived = true
      }
      "start with an empty Bus" in {
        bus.isEmpty should equal(true)
      }
      "successfully add the listeners" in {
        bus.add(first)
        bus.add(second)
      }
      "fire a message" in {
        bus("Test3")
      }
      "receive the message on first properly" in {
        firstReceived should equal(true)
      }
      "not receive the message on the second" in {
        secondReceived should equal(false)
      }
      "remove both nodes" in {
        bus.remove(first)
        bus.remove(second)
        bus.isEmpty should equal(true)
      }
    }
    "a node with a response" should {
      var secondReceived = false
      var response: Routing = null
      val first = Node(Priority.High) {
        case message => Routing.Response("First!")
      }
      val second = Node(Priority.Normal) {
        case message => secondReceived = true
      }
      "start with an empty Bus" in {
        bus.isEmpty should equal(true)
      }
      "successfully add the nodes" in {
        bus.add(second)
        bus.add(first)
      }
      "fire a message" in {
        response = bus("test response")
      }
      "have a Routing.Response" in {
        response.getClass should equal(classOf[RoutingResponse])
      }
      "have the value 'First!' as the Routing.Response" in {
        response.asInstanceOf[RoutingResponse].response should equal("First!")
      }
      "not hit the second Node" in {
        secondReceived should equal(false)
      }
      "remove both nodes" in {
        bus.remove(first)
        bus.remove(second)
        bus.isEmpty should equal(true)
      }
    }
    "nodes with results" should {
      var response: Routing = null
      val first = Node(Priority.High) {
        case message => Routing.Results(List(1, 2, 3))
      }
      val second = Node(Priority.Normal) {
        case message => Routing.Results(List(4, 5, 6))
      }
      val third = Node(Priority.Low) {
        case message => Routing.Results(List(7, 8, 9))
      }
      "start with an empty Bus" in {
        bus.isEmpty should equal(true)
      }
      "successfully add the nodes" in {
        bus.add(third)
        bus.add(first)
        bus.add(second)
      }
      "fire a message" in {
        response = bus("test results")
      }
      "have a Routing.Results" in {
        response.getClass should equal(classOf[RoutingResults])
      }
      "have the List[1,2,3,4,5,6,7,8,9]" in {
        response.asInstanceOf[RoutingResults].results should equal(List(1, 2, 3, 4, 5, 6, 7, 8, 9))
      }
      "remove all three nodes" in {
        bus.remove(first)
        bus.remove(second)
        bus.remove(third)
      }
    }
  }
}