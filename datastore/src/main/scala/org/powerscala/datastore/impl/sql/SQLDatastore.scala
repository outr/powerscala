package org.powerscala.datastore.impl.sql

import javax.sql.DataSource
import org.h2.jdbcx.JdbcConnectionPool
import java.io.File
import org.powerscala.datastore.Datastore

/**
 * @author Matt Hicks <matt@outr.com>
 */
class SQLDatastore(val dataSource: DataSource = SQLDatastore.h2.inMemory()) extends Datastore {
  protected def createSession() = null    // TODO: implement
}

object SQLDatastore {
  object h2 {
    def inMemory(name: String = "db") = JdbcConnectionPool.create(s"jdbc:h2:mem:$name", "sa", "sa")
    def embedded(file: File = new File("db")) = JdbcConnectionPool.create(s"jdbc:h2:${file.getAbsolutePath}", "sa", "sa")
    def server(host: String = "localhost", path: String = "db") = JdbcConnectionPool.create(s"jdbc:h2:tcp://$host/$path", "sa", "sa")
  }
}