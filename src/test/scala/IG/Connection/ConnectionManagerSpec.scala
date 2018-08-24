package IG.Connection

import akka.actor.ActorSystem
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import IG.Util
import org.scalatest.AsyncFlatSpec

import java.util.logging.Logger

import scala.concurrent.{Await, Future}


class ConnectionManagerSpec extends AsyncFlatSpec with Util {

  private[this] final val log = Logger.getLogger(classOf[ConnectionManagerSpec].getName)

  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  val apiConnection: ApiConnection = new ApiConnection(DEMO)
  val connFut: Future[HttpResponse] = apiConnection.futHandShakeConnection

  behavior of "ConnectionManager"

  it should "open a connection to DEMO IG" in {

    connFut.map { response: HttpResponse =>
      val isSuccessful = response.status.isSuccess()
      val hasCst = apiConnection.CST_TOKEN_HEADER.value().nonEmpty

      apiConnection.handshakeResponseJson

      assert(isSuccessful)
      assert(hasCst)
    }

  }

  it should "be able to get data for an epic" in {
    val wrongGbpUsd = "CS.D.GBXUSX.TODAY.IP"
    val errorResponse =
      "HttpEntity.Strict(application/json,{\"errorCode\":\"error.service.marketdata.instrument.epic.invalid\"})"

    val wrongEpicMarketData = Await.result(apiConnection.futMarketsRequest(wrongGbpUsd), patienceDuration)
    assert(errorResponse === wrongEpicMarketData.entity.toString)
    wrongEpicMarketData.discardEntityBytes()

    val gbpUsd = "CS.D.GBPUSD.TODAY.IP"
    val epicMarketData = Await.result(apiConnection.futMarketsRequest(gbpUsd),patienceDuration)
    val rightResponse = epicMarketData.entity.toString
    epicMarketData.discardEntityBytes()

    log.info(prettyPrintEntity(epicMarketData.entity))

    assert(rightResponse !== errorResponse)
  }

}
