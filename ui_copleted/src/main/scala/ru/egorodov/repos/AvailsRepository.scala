package ru.egorodov.repos

import io.getquill.{MysqlJdbcContext, SnakeCase, SqlMirrorContext}

case class ReportsRequests(id: Int, comments: String, queue: String)

class DefaultContext extends MysqlJdbcContext[SnakeCase]("ctx")

trait RequestsSchema {
  val c: DefaultContext
  import c._

  def requestsWithAssociations(id: Int) = quote {
    querySchema[ReportsRequests]("reports_requests").filter { r => r.id == lift(id)}
  }

  def requestByVirtualField(field: String) = quote {
    querySchema[ReportsRequests]("reports_requests").filter { r => r.comments + r.queue == lift(field)}
  }
}

case class RequestsDao(c: DefaultContext) extends RequestsSchema {
  import c._
  def requestById(id: Int) = c.run(requestsWithAssociations(id))

  def similarRequest(virtualattribute: String) = c.run(requestByVirtualField(virtualattribute))
}

private case class CombinedField(virtualField: String)

class AvailsRepository {
  lazy val ctx = new DefaultContext
  def getSimilarByid(availsId: Int): List[ReportsRequests] = {
    val dao = RequestsDao(ctx)

    val combinedField: Option[CombinedField] = dao.requestById(availsId) match {
      case List(ReportsRequests(id, comments, queue)) => Some(CombinedField(comments + queue))
      case _ => None
    }

    dao.similarRequest(combinedField.getOrElse(CombinedField("")).virtualField)
  }
}
