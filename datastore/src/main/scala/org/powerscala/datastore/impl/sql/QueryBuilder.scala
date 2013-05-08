package org.powerscala.datastore.impl.sql

import java.sql.{ResultSet, Connection, PreparedStatement}
import org.powerscala.{Enumerated, EnumEntry}
import java.util.UUID

/**
 * @author Matt Hicks <matt@outr.com>
 */
case class QueryBuilder(_table: String,
                        _fields: List[String] = Nil,
                        _conditions: List[Condition] = Nil,
                        _groups: List[GroupBy] = Nil,
                        _orders: List[OrderBy] = Nil,
                        _joins: List[Join] = Nil,
                        _limit: Int = -1,
                        _offset: Int = -1) {
  def table(name: String): QueryBuilder = copy(_table = name)
  def field(name: String): QueryBuilder = copy(_fields = (name :: _fields.reverse).reverse)
  def fields(names: String*): QueryBuilder = copy(_fields = names.toList)
  def where(condition: Condition): QueryBuilder = copy(_conditions = (condition :: _conditions.reverse).reverse)
  def where(condition: String): QueryBuilder = where(StaticCondition(condition))
  def where(field: String, value: Any, conditionType: ConditionType = ConditionType.Equal): QueryBuilder = {
    where(SimpleCondition(field, conditionType, value))
  }
  def whereNull(field: String): QueryBuilder = where(new NullCheckCondition(field, not = false))
  def whereNotNull(field: String): QueryBuilder = where(new NullCheckCondition(field, not = true))
  def groupBy(field: String): QueryBuilder = copy(_groups = (GroupBy(field) :: _groups.reverse).reverse)
  def orderBy(field: String, ascending: Boolean = true): QueryBuilder = copy(_orders = (OrderBy(field, ascending) :: _orders.reverse).reverse)
  def join(join: Join): QueryBuilder = copy(_joins = (join :: _joins.reverse).reverse)
  def limit(value: Int) = copy(_limit = value)
  def offset(value: Int) = copy(_offset = value)

  def executeQuery[T](connection: Connection)(f: ResultSet => T) = {
    val ps = connection.prepareStatement(query())
    try {
      var index = 1
      _joins.foreach {
        case j => j.conditions.foreach {
          case c => if (c.prepared) {
            c(ps, index)
            index += 1
          }
        }
      }
      _conditions.foreach {
        case c => if (c.prepared) {
          c(ps, index)
          index += 1
        }
      }
      val results = ps.executeQuery()
      try {
        f(results)
      } finally {
        results.close()
      }
    } finally {
      ps.close()
    }
  }

  def query() = {
    val b = new StringBuilder("SELECT")
    if (_fields.nonEmpty) {
      b.append(" ")
      b.append(_fields.mkString(", "))
    } else {
      b.append(" *")
    }
    b.append(" FROM ")
    b.append(_table)
    if (_joins.nonEmpty) {
      b.append(" ")
      b.append(_joins.map(j => j.sql).mkString(" "))
    }
    if (_conditions.nonEmpty) {
      b.append(" WHERE ")
      b.append(_conditions.map(c => c.sql).mkString(" AND "))
    }
    if (_groups.nonEmpty) {
      b.append(" GROUP BY ")
      b.append(_groups.map(g => g.sql).mkString(", "))
    }
    if (_orders.nonEmpty) {
      b.append(" ORDER BY ")
      b.append(_orders.map(o => o.sql).mkString(", "))
    }
    if (_limit != -1) {
      b.append(" LIMIT ")
      b.append(_limit)
    }
    if (_offset != -1) {
      b.append(" OFFSET ")
      b.append(_offset)
    }
    b.toString()
  }
}

object QueryBuilder {
  def set(ps: PreparedStatement, index: Int, value: Any) = value match {
    case null => ps.setObject(index, null)
    case i: Int => ps.setInt(index, i)
    case s: String => ps.setString(index, s)
    case uuid: UUID => ps.setObject(index, uuid)
  }
}

trait Condition {
  def sql: String
  def prepared: Boolean = true

  def apply(ps: PreparedStatement, index: Int): Unit
}

case class StaticCondition(sql: String) extends Condition {
  override def prepared = false

  def apply(ps: PreparedStatement, index: Int) = throw new UnsupportedOperationException("Not prepared!")
}

case class SimpleCondition(field: String, conditionType: ConditionType, value: Any) extends Condition {
  def sql = s"$field ${conditionType.sql} ?"

  def apply(ps: PreparedStatement, index: Int) = QueryBuilder.set(ps, index, value)
}

class NullCheckCondition(field: String, not: Boolean = false) extends Condition {
  def sql = if (not) {
    s"$field IS NOT NULL"
  } else {
    s"$field IS NULL"
  }

  override def prepared = false

  def apply(ps: PreparedStatement, index: Int) = throw new UnsupportedOperationException("Not prepared!")
}

sealed class ConditionType(val sql: String) extends EnumEntry[ConditionType]

object ConditionType extends Enumerated[ConditionType] {
  val Equal = new ConditionType("=")
  val Like = new ConditionType("LIKE")
  val < = new ConditionType("<")
  val > = new ConditionType(">")
  val <= = new ConditionType("<=")
  val >= = new ConditionType(">=")
}

case class GroupBy(field: String) {
  def sql = field
}

case class OrderBy(field: String, ascending: Boolean = true) {
  def sql = s"$field ${if (ascending) "ASC" else "DESC"}"
}

trait Join {
  def sql: String
  def conditions: List[Condition]
}

case class SimpleJoin(table: String, joinType: JoinType, _conditions: List[Condition] = Nil) extends Join {
  def sql = s"${joinType.sql} $table ON ${_conditions.map(c => c.sql).mkString(" AND ")}"

  def where(condition: Condition): SimpleJoin = copy(_conditions = (condition :: _conditions.reverse).reverse)
  def where(condition: String): SimpleJoin = where(StaticCondition(condition))
  def where(field: String, value: Any, conditionType: ConditionType = ConditionType.Equal): SimpleJoin = {
    where(SimpleCondition(field, conditionType, value))
  }
  def whereNull(field: String): SimpleJoin = where(new NullCheckCondition(field, not = false))
  def whereNotNull(field: String): SimpleJoin= where(new NullCheckCondition(field, not = true))

  def conditions = _conditions
}

case class SubselectJoin(query: QueryBuilder, as: String, joinType: JoinType, _conditions: List[Condition] = Nil) extends Join {
  def sql = s"${joinType.sql} (${query.query()}) $as ON ${_conditions.map(c => c.sql).mkString(" AND ")}"

  def where(condition: Condition): SubselectJoin = copy(_conditions = (condition :: _conditions.reverse).reverse)
  def where(condition: String): SubselectJoin = where(StaticCondition(condition))
  def where(field: String, value: Any, conditionType: ConditionType = ConditionType.Equal): SubselectJoin = {
    where(SimpleCondition(field, conditionType, value))
  }
  def whereNull(field: String): SubselectJoin = where(new NullCheckCondition(field, not = false))
  def whereNotNull(field: String): SubselectJoin= where(new NullCheckCondition(field, not = true))

  def conditions = query._conditions ++ _conditions
}

sealed class JoinType(val sql: String) extends EnumEntry[JoinType]

object JoinType extends Enumerated[JoinType] {
  val Join = new JoinType("JOIN")
  val Left = new JoinType("LEFT JOIN")
  val LeftOuter = new JoinType("LEFT OUTER JOIN")
  val Inner = new JoinType("INNER JOIN")
}