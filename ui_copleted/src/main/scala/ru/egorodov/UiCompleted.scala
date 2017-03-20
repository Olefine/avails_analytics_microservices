package ru.egorodov

import com.twitter.finagle.{Http, Service, http}
import com.twitter.util.{Await, Future}
import com.twitter.finagle.http.path._

object UiCompleted extends App{
  val service = new Service[http.Request, http.Response] {
    def apply(req: http.Request): Future[http.Response] = {
      Path(req.path) match {
        case Root / "avails_requests" / id =>
          val result = similarId(id.toInt)
          val response = http.Response(http.Version.Http11, http.Status.Ok)
          response.setContentString(constructResult(result))
          Future.value(response)
      }
    }
  }

  val server = Http.serve(":7070", service)
  Await.ready(server)

  private def similarId(availsId: Int): List[repos.ReportsRequests] = {
    val repo = new repos.AvailsRepository()
    repo.getSimilarByid(availsId)
  }

  private def constructResult(entities: List[repos.ReportsRequests]): String = {
    entities.map { request =>
      s"""{\"id\" : ${request.id}, \"comments\" : \"${request.comments}\", \"queue\" : \"${request.queue}\"}"""
    } mkString(", ") match {
      case r: String => s"[$r]"
    }
  }
}
