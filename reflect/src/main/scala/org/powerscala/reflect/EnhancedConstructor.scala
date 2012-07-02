package org.powerscala.reflect

import java.lang.reflect.Constructor

/**
 * EnhancedConstructor wraps a java.lang.reflect.Constructor to provide more functionality and easier access.
 *
 * @author Matt Hicks <mhicks@powerscala.org>
 */
class EnhancedConstructor protected[reflect](val parent: EnhancedClass, val javaConstructor: Constructor[_]) {
  /**
   * The constructor's name.
   */
  def name = javaConstructor.getName

  /**
   * The arguments this method takes to invoke.
   */
  //  lazy val args: List[MethodArgument] = for ((dc, index) <- _docs.args.zipWithIndex) yield {
  //    new MethodArgument(index, dc.name, dc.`type`, getDefault(index), dc.doc)
  //  }

  // TODO: finish
}