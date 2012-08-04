package org.powerscala.property.backing

import org.powerscala.property.MutableProperty
import org.powerscala.reflect.CaseValue

/**
 * CaseValueVariableBacking allows binding to a property with a case class along with an associated CaseValue.
 *
 * When the value of the associated property changes, the case class is copied with the updated CaseValue and applied
 * back to the property.
 *
 * For example:
 *
 * case class Person(name: String)
 * val person = Property[Person]("person", Person("John Doe"))
 * val caseValue = classOf[Person].caseValue("name").get
 * val backing = new CaseValueVariableBacking[Person, String](person, caseValue)
 * val personName = Property[String]("personName", person().name, backing)
 *
 * Changes to 'personName' will update the 'name' value of Person and apply it back to 'person' property.
 *
 * @author Matt Hicks <mhicks@powerscala.org>
 */
class CaseValueVariableBacking[T, C](caseProperty: MutableProperty[T], caseValue: CaseValue) extends Backing[C] {
  def getValue = caseValue[C](caseProperty().asInstanceOf[AnyRef])

  def setValue(value: C) = caseProperty := caseValue.copy[T](caseProperty(), value)
}
