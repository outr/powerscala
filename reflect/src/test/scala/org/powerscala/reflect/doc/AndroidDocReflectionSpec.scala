//package org.powerscala.reflect.doc
//
//import org.scalatest.matchers.ShouldMatchers
//import org.scalatest.WordSpec
//import javax.microedition.khronos.opengles.{GL11, GL10}
//import android.opengl.{GLES20, GLES11}
//
///**
// *
// *
// * @author Matt Hicks <mhicks@powerscala.org>
// */
//class AndroidDocReflectionSpec extends WordSpec with ShouldMatchers {
//  var adf1: AndroidDocReflection = _
//  var adf2: AndroidDocReflection = _
//  var adf3: AndroidDocReflection = _
//  val gl10 = classOf[GL10]
//  val gl11 = classOf[GL11]
//  val glLineWidthx = gl10.getMethod("glLineWidthx", classOf[Int])
//  val glGetString = gl10.getMethod("glGetString", classOf[Int])
//  val glBindBuffer = gl11.getMethod("glBindBuffer", classOf[Int], classOf[Int])
//
//  "AndroidDocReflection" when {
//    "instantiated" should {
//      "not throw an exception" in {
//        adf1 = new AndroidDocReflection(gl10.getName)
//        adf2 = new AndroidDocReflection(gl11.getName)
//      }
//      "not be null" in {
//        adf1 should not be (null)
//        adf2 should not be (null)
//      }
//    }
//    "invoked on GL10.glLineWidthx" should {
//      var md: MethodDocumentation = null
//      "not return null" in {
//        md = adf1.method(glLineWidthx)
//        md should not be (null)
//      }
//      "have one arg" in {
//        md.args.length should be(1)
//      }
//      "have 'width' as the first argument" in {
//        md.args(0).name should be("width")
//      }
//      "have Int as the first argument type" in {
//        md.args(0).`type` should be(classOf[Int])
//      }
//      "have void as the return type" in {
//        md.returnClass.`type`.name should be("Unit")
//      }
//      "link to the proper URL" in {
//        md.url should be("http://developer.android.com/reference/javax/microedition/khronos/opengles/GL10.html#glLineWidthx(int)")
//      }
//    }
//    "invoked on GL10.glGetString" should {
//      var md: MethodDocumentation = null
//      "not return null" in {
//        md = adf1.method(glGetString)
//        md should not be (null)
//      }
//      "have one arg" in {
//        md.args.length should be(1)
//      }
//      "have 'name' as the first argument" in {
//        md.args(0).name should be("name")
//      }
//      "have Int as the first argument type" in {
//        md.args(0).`type` should be(classOf[Int])
//      }
//      "have String as the return type" in {
//        md.returnClass.`type` should be(classOf[String])
//      }
//      "link to the proper URL" in {
//        md.url should be("http://developer.android.com/reference/javax/microedition/khronos/opengles/GL10.html#glGetString(int)")
//      }
//    }
//    "invoked on GLES11.glBindBuffer" should {
//      val ref = new AndroidDocReflection(classOf[GLES11].getName)
//      val glBindBuffer = classOf[GLES11].getMethod("glBindBuffer", classOf[Int], classOf[Int])
//      var md: MethodDocumentation = null
//      "not return null" in {
//        md = ref.method(glBindBuffer)
//        md should not be (null)
//      }
//      "have two args" in {
//        md.args.length should be(2)
//      }
//      "have 'target' and 'buffer' as arguments" in {
//        md.args(0).name should be("target")
//        md.args(1).name should be("buffer")
//      }
//      "have Int as both arguments" in {
//        md.args(0).`type` should be(classOf[Int])
//        md.args(1).`type` should be(classOf[Int])
//      }
//      "link to the proper URL" in {
//        md.url should be("http://developer.android.com/reference/android/opengl/GLES11.html#glBindBuffer(int, int)")
//      }
//    }
//    "invoked on GLES20.glDeleteBuffers" should {
//      val ref = new AndroidDocReflection(classOf[GLES20].getName)
//      val glDeleteBuffers = classOf[GLES20].getMethod("glDeleteBuffers", classOf[Int], classOf[Array[Int]], classOf[Int])
//      var md: MethodDocumentation = null
//      "not return null" in {
//        md = ref.method(glDeleteBuffers)
//        md should not be (null)
//      }
//      "have three args" in {
//        md.args.length should be(3)
//      }
//      "have 'n', 'buffers', and 'offset' as arguments" in {
//        md.args(0).name should be("n")
//        md.args(1).name should be("buffers")
//        md.args(2).name should be("offset")
//      }
//      "have Int, Array[Int], and Int as arguments" in {
//        md.args(0).`type` should be(classOf[Int])
//        md.args(1).`type` should be(classOf[Array[Int]])
//        md.args(2).`type` should be(classOf[Int])
//      }
//      "link to the proper URL" in {
//        md.url should be("http://developer.android.com/reference/android/opengl/GLES20.html#glDeleteBuffers(int, int[], int)")
//      }
//    }
//    "invoked on GL11.glBindBuffer" should {
//      var md: MethodDocumentation = null
//      "not return null" in {
//        md = adf2.method(glBindBuffer)
//        md should not be (null)
//      }
//      "have two args" in {
//        md.args.length should be(2)
//      }
//      "have 'target' and 'buffer' as arguments" in {
//        md.args(0).name should be("target")
//        md.args(1).name should be("buffer")
//      }
//      "have Int as both arguments" in {
//        md.args(0).`type` should be(classOf[Int])
//        md.args(1).`type` should be(classOf[Int])
//      }
//      "link to the proper URL" in {
//        md.url should be("http://developer.android.com/reference/javax/microedition/khronos/opengles/GL11.html#glBindBuffer(int, int)")
//      }
//    }
//    "invoked on GL11.glBindBuffer with remoteSources disabled" should {
//      var md: MethodDocumentation = null
//      "not throw an exception" in {
//        adf3 = new AndroidDocReflection(gl11.getName)
//      }
//      "not be null" in {
//        adf3 should not be (null)
//      }
//      "not return null" in {
//        DocumentationReflection.remoteSources = false
//        md = adf3.method(glBindBuffer)
//        md should not be (null)
//      }
//      "have two args" in {
//        md.args.length should be(2)
//      }
//      "have 'target' and 'buffer' as arguments" in {
//        md.args(0).name should be("target")
//        md.args(1).name should be("buffer")
//      }
//      "have Int as both arguments" in {
//        md.args(0).`type` should be(classOf[Int])
//        md.args(1).`type` should be(classOf[Int])
//      }
//      "link to the proper URL" in {
//        md.url should be("http://developer.android.com/reference/javax/microedition/khronos/opengles/GL11.html#glBindBuffer(int, int)")
//      }
//    }
//  }
//}