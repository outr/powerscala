/*
 * Copyright (c) 2011 PowerScala
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *  Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 *  Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 *  Neither the name of 'PowerScala' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.powerscala.hierarchy

import org.powerscala.{ScalaDelayedInitBug, ExtendedDelayedInit}
import org.powerscala.reflect._

/**
 * StaticContainer expects all children to be defined within the class itself and uses Reflection to add the children to
 * the container.
 *
 * @author Matt Hicks <mhicks@powerscala.org>
 */
class StaticContainer[T <: Element](implicit manifest: Manifest[T]) extends MutableContainer[T] with ExtendedDelayedInit {
  override def postInit() = {
    super.postInit()
    val children = loadElements(getClass.methods, Nil).reverse
    children.foreach(child => {
      contents += child
    })
  }

  private def loadElements(methods: List[EnhancedMethod], list: List[T]): List[T] = {
    if (!methods.isEmpty) {
      val m = methods.head
      val l = if (m.args.isEmpty &&
        manifest.erasure.isAssignableFrom(m.returnType.`type`.javaClass) &&
        m.name.indexOf('$') == -1 &&
        m.name != "toString") {
        val element = m.invoke[T](this)
        if (element == null) throw new ScalaDelayedInitBug
        element :: list
      } else {
        list
      }

      loadElements(methods.tail, l)
    } else {
      list
    }
  }
}