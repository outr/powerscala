package org.powerscala.reflect


/**
 * Args trait can be mixed into an object to provide an argument-based main method. Simply mix in this trait and define
 * a main method with named arguments and default values to allow instantiation to occur dynamically.
 *
 * For example:
 *   def main(name: String, count: Int = 5) = {
 *   }
 *
 * May be invoked via the following command-line invocation:
 *   ArgsClass --name "John Doe" --count 25
 *
 * Excluding arg names will place them at the first undefined position without a default value. If required values are
 * excluded the method will be run with type defaults (null for refs).
 *
 * @author Matt Hicks <mhicks@outr.com>
 */
trait Args {
  def argPrefix = "--"

  def main(args: Array[String]): Unit = {
    val arguments = Args.args2List(args.toList, argPrefix)
    var unnamed = arguments.collect {
      case Left(value) => value
    }
    val named = arguments.collect {
      case Right((key, value)) => key.substring(argPrefix.length) -> value
    }.toMap
    getClass.methods.find(m => m.name == "main" && (m.args.length != 1 || m.args.head.`type`.javaClass != classOf[Array[String]])) match {
      case Some(main) => {
        val params = main.args.collect {
          case arg if (named.contains(arg.name)) => arg.name -> named(arg.name)
          case arg if (unnamed.nonEmpty && !arg.hasDefault) => {
            val value = unnamed.head
            unnamed = unnamed.tail
            arg.name -> value
          }
        }.toMap
        main(this, params)
      }
      case None => sys.error("No argument-based main method defined in %s.".format(getClass.getName))
    }
  }
}

object Args {
  final def args2List(args: List[String],
                     namePrefix: String = "--",
                     list: List[Either[String, (String, String)]] = List.empty): List[Either[String, (String, String)]] = {
    if (args.isEmpty) {
      list.reverse
    } else {
      val head = args.head
      if (head.startsWith(namePrefix)) {
        val arg = args.tail.head
        args2List(args.tail.tail, namePrefix, Right(head, arg) :: list)
      } else {
        args2List(args.tail, namePrefix, Left(head) :: list)
      }
    }
  }
}