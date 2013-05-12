//package org.powerscala.reflect.doc
//
//import org.scalatest.matchers.ShouldMatchers
//import org.scalatest.WordSpec
//import java.util.GregorianCalendar
//import org.powerscala.reflect.{EnhancedMethod, EnhancedClass}
//
///**
// *
// *
// * @author Matt Hicks <mhicks@powerscala.org>
// */
//class JavaDocReflectionSpec extends WordSpec with ShouldMatchers {
//  var jdf: JavaDocReflection = _
//  val clazz = classOf[String]
//  val charAt = clazz.getMethod("charAt", classOf[Int])
//  val codePointCount = clazz.getMethod("codePointCount", classOf[Int], classOf[Int])
//  val intern = clazz.getMethod("intern")
//
//  lazy val gregorianCalendar = EnhancedClass(classOf[GregorianCalendar])
//
//  "JavaDocReflection" when {
//    "instantiating on String" should {
//      "not throw an exception" in {
//        jdf = new JavaDocReflection(clazz.getName)
//      }
//      "not be null" in {
//        jdf should not be (null)
//      }
//    }
//    "invoked on String.charAt" should {
//      var md: MethodDocumentation = null
//      "not return null" in {
//        md = jdf.method(charAt)
//        md should not be (null)
//      }
//      "have one arg" in {
//        md.args.length should be(1)
//      }
//      "have 'index' as the first argument" in {
//        md.args(0).name should be("index")
//      }
//      "have Int as the first argument type" in {
//        md.args(0).`type` should be(classOf[Int])
//      }
//      "have Char as the return type" in {
//        md.returnClass.`type` should be(classOf[Char])
//      }
//      "link to the proper URL" in {
//        md.url should be("http://download.oracle.com/javase/6/docs/api/java/lang/String.html#charAt(int)")
//      }
//    }
//    "invoked on String.codePointCount" should {
//      var md: MethodDocumentation = null
//      "not return null" in {
//        md = jdf.method(codePointCount)
//        md should not be (null)
//      }
//      "have two args" in {
//        md.args.length should be(2)
//      }
//      "have 'beginIndex' as the first argument" in {
//        md.args(0).name should be("beginIndex")
//      }
//      "have Int as the first argument type" in {
//        md.args(0).`type` should be(classOf[Int])
//      }
//      "have the proper documentation on the first argument" in {
//        md.args(0).doc.get.text should be("the index to the first char of the text range.")
//      }
//      "have 'endIndex' as the second argument" in {
//        md.args(1).name should be("endIndex")
//      }
//      "have Int as the second argument type" in {
//        md.args(1).`type` should be(classOf[Int])
//      }
//      "have the proper documentation on the second argument" in {
//        md.args(1).doc.get.text should be("the index after the last char of the text range.")
//      }
//      "have Int as the return type" in {
//        md.returnClass.`type` should be(classOf[Int])
//      }
//      "have the proper documentation on the return type" in {
//        md.returnClass.doc.get.text should be("the number of Unicode code points in the specified text range")
//      }
//      "link to the proper URL" in {
//        md.url should be("http://download.oracle.com/javase/6/docs/api/java/lang/String.html#codePointCount(int, int)")
//      }
//    }
//    "invoked on String.intern" should {
//      var md: MethodDocumentation = null
//      "not return null" in {
//        md = jdf.method(intern)
//        md should not be (null)
//      }
//      "have no args" in {
//        md.args.length should be(0)
//      }
//      "have String as the return type" in {
//        md.returnClass.`type` should be(classOf[String])
//      }
//      "link to the proper URL" in {
//        md.url should be("http://download.oracle.com/javase/6/docs/api/java/lang/String.html#intern()")
//      }
//    }
//    "invoked on Calendar.get" should {
//      var method: EnhancedMethod = null
//      var docs: Documentation = null
//      "lookup the method" in {
//        method = gregorianCalendar.methodByName("get").getOrElse(null)
//        method should not equal (null)
//      }
//      "get the documentation" in {
//        docs = method.docs.get
//      }
//      "have one arg" in {
//        method.args.length should equal(1)
//      }
//      "have Int as the return type" in {
//        method.returnType.`type` should be(classOf[Int])
//      }
//      "link to the proper URL" in {
//        method.docsURL should be("http://download.oracle.com/javase/6/docs/api/java/util/Calendar.html#get(int)")
//      }
//    }
//    "invoked on GregorianCalendar.yearLength" should {
//      var method: EnhancedMethod = null
//      "lookup the method" in {
//        method = gregorianCalendar.method("yearLength").getOrElse(null)
//        method should not equal (null)
//      }
//      "get the documentation" in {
//        method.docs should equal(None)
//      }
//      "have no args" in {
//        method.args.length should equal(0)
//      }
//      "have Int as the return type" in {
//        method.returnType.`type` should equal(classOf[Int])
//      }
//      "link to the proper URL" in {
//        method.docsURL should equal(null)
//      }
//    }
//    "invoked on GregorianCalendar.setGregorianChange" should {
//      var method: EnhancedMethod = null
//      "lookup the method" in {
//        method = gregorianCalendar.methodByName("setGregorianChange").getOrElse(null)
//        method should not equal (null)
//      }
//      "get the documentation" in {
//        method.docs should equal(None)
//      }
//      "have one arg" in {
//        method.args.length should equal(1)
//      }
//      "have proper argument name" in {
//        method.args.head.name should equal("arg0")
//      }
//      "have Int as the return type" in {
//        method.returnType.`type` should equal(classOf[Unit])
//      }
//      "link to the proper URL" in {
//        method.docsURL should equal(null)
//      }
//    }
//  }
//}