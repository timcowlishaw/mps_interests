package mps_expenses

import scala.slick.driver.SQLiteDriver.simple._
import Models._

object Persistence {

  class MPs(tag: Tag) extends Table[MP](tag, "mps") {
    def name = column[String]("name")
    def interests = column[String]("interests", O.DBType("TEXT"))
    def * = (name, interests) <> (MP.tupled, MP.unapply)
  }

  val database = Database.forURL("jdbc:sqlite:data/mps_expenses.sqlite", driver="org.sqlite.JDBC")
  val mps = TableQuery[MPs]

  def initializeDB = {
    database withSession { implicit session =>
      mps.ddl.create
    }
  }

  def persistMP(mp : MP) = {
    database withSession { implicit session =>
      mps += mp
    }
  }
}
