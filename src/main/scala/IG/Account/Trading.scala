package IG.Account

import IG.Connection.ApiConnection
import IG.Util
import akka.http.scaladsl.model.HttpResponse
import com.typesafe.scalalogging.Logger

import scala.concurrent.Future
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global


class Positions(connection: ApiConnection) extends Util {
  private[this] final val log = Logger(classOf[Positions])

  def currentPositions(positions:  Future[HttpResponse] ) = positions.onComplete {
    case Success(e: HttpResponse) => {
      val positionJson = prettyPrintEntity(e.entity)(connection.materializer)
      log.info("\n" + positionJson)
    }
    case Failure(f) => log.warn(s"Failure: [${f.getMessage}]")
  }


}