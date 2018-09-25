package IG.Account

import akka.http.scaladsl.model.HttpResponse
import IG.Connection.ApiConnection
import IG.{LogUtil, Util}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}


class Positions(connection: ApiConnection) extends Util with LogUtil {
  def currentPositions(positions:  Future[HttpResponse] ): Unit = positions.onComplete {
    case Success(e: HttpResponse) =>
      val positionJson: String = prettyPrintEntity(e.entity)(connection.mat)
      log.info(positionJson)
    case Failure(f) => log.error(s"Failure: [${f.getMessage}]")
  }
}