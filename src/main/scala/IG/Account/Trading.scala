package IG.Account

import java.util.logging.Logger

import IG.Connection.ApiConnection
import IG.Util
import akka.http.scaladsl.model.HttpResponse

import scala.concurrent.Future
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global


class Positions(connection: ApiConnection) extends Util {
  private final val log = Logger.getLogger(classOf[Positions].getName)

  def getCurrentPositions(positions:  Future[HttpResponse] ) = positions.onComplete {
    case Success(e: HttpResponse) => {
      val positionJson = prettyPrintEntity(e.entity)(connection.materializer)
      log.info(positionJson)
    }
    case Failure(f) => log.warning(s"Failure: [${f.getMessage}]")
  }


}