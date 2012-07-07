package org.powerscala.convert

import json.Object2JSON
import org.scalatest.WordSpec
import org.scalatest.matchers.ShouldMatchers
import util.parsing.json.{JSONObject, JSON}

/**
 * @author Matt Hicks <mhicks@powerscala.org>
 */
class CaseClassConverterSpec extends WordSpec with ShouldMatchers {
  val person = Person("John Doe", 30)
  val company = Company("Veridian Dynamics", person, Person("Jane Doe", 31))
  "CaseClassConverter" when {
    "converting a Person instance directly" should {
      val caseClass = CaseClassDisassembler.disassemble(person)
      "have the proper class reference" in {
        caseClass.clazz.simpleName should equal("Person")
      }
      "have the proper arguments" in {
        caseClass.values.length should equal(2)
        val nameField = caseClass.values.head
        val ageField = caseClass.values.tail.head
        nameField.caseValue.name should equal("name")
        nameField.value should equal("John Doe")
        ageField.caseValue.name should equal("age")
        ageField.value should equal(30)
      }
    }
    "converting through an instance of ConversionBus" should {
      val bus = new ConversionBus
      val converted = bus.convert(person)
      val caseClass = converted.asInstanceOf[CaseClass]
      "have the proper class reference" in {
        caseClass.clazz.simpleName should equal("Person")
      }
      "have the proper arguments" in {
        val nameField = caseClass.values.head
        val ageField = caseClass.values.tail.head
        nameField.caseValue.name should equal("name")
        nameField.value should equal("John Doe")
        ageField.caseValue.name should equal("age")
        ageField.value should equal(30)
      }
    }
  }
  "JSON Conversion" when {
    "converting a Person instance" should {
      val json = Object2JSON.toJSON(person)
      "not be null" in {
        json should not equal(null)
      }
      "properly parse back into JSON" in {
        val result = JSON.parseRaw(json).get.asInstanceOf[JSONObject]
        result.obj("name") should equal("John Doe")
        result.obj("age") should equal(30)
        result.obj("class") should equal("org.powerscala.convert.Person")
      }
    }
    "converting a Company instance" should {
      val json = Object2JSON.toJSON(company)
      "not be null" in {
        json should not equal(null)
      }
      "property parse back into JSON" in {
        val result = JSON.parseRaw(json).get.asInstanceOf[JSONObject]
        result.obj("name") should equal("Veridian Dynamics")
        result.obj("class") should equal("org.powerscala.convert.Company")
      }
    }
  }
}

case class Person(name: String, age: Int)

case class Company(name: String, ceo: Person, coo: Person)