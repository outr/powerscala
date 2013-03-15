package org.powerscala.datastore.query

import org.powerscala.{EnumEntry, Enumerated}
import java.util.regex.Pattern

sealed class Operator extends EnumEntry[Operator]

object Operator extends Enumerated[Operator] {
  val < = new Operator
  val <= = new Operator
  val > = new Operator
  val >= = new Operator
  val regex = new Operator
  val subfilter = new Operator
  val in = new Operator
  val and = new Operator
  val or = new Operator
  val equal = new Operator
  val nequal = new Operator
  val exists = new Operator
}

sealed class RegexFlag(val flag: Int) extends EnumEntry[RegexFlag]

object RegexFlag extends Enumerated[RegexFlag] {
  val CaseInsensitive = new RegexFlag(Pattern.CASE_INSENSITIVE)
  val MultiLine = new RegexFlag(Pattern.MULTILINE)
  val DotAll = new RegexFlag(Pattern.DOTALL)
  val UnicodeCase = new RegexFlag(Pattern.UNICODE_CASE)
  val CanonEq = new RegexFlag(Pattern.CANON_EQ)
  val UnixLines = new RegexFlag(Pattern.UNIX_LINES)
  val Literal = new RegexFlag(Pattern.LITERAL)
  val UnicodeCharacterClass = new RegexFlag(Pattern.UNICODE_CHARACTER_CLASS)
  val Comments = new RegexFlag(Pattern.COMMENTS)
}