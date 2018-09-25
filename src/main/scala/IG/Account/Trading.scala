package IG.Account

import akka.http.scaladsl.model.HttpResponse
import com.typesafe.scalalogging.Logger
import IG.Connection.ApiConnection
import IG.Util

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}


class Positions(connection: ApiConnection) extends Util {
  private[this] final val log = Logger(classOf[Positions])

  def currentPositions(positions:  Future[HttpResponse] ): Unit = positions.onComplete {
    case Success(e: HttpResponse) =>
      val positionJson = prettyPrintEntity(e.entity)(connection.materializer)
      log.info(positionJson)
    case Failure(f) => log.warn(s"Failure: [${f.getMessage}]")
  }


}