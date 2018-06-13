package IG

import org.scalatest.AsyncFlatSpec
import Connection._
import akka.actor.ActorSystem
import akka.http.scaladsl.model._
import java.util.logging._

import akka.stream.ActorMaterializer

import scala.concurrent.duration.{FiniteDuration, SECONDS}
import scala.concurrent.{Await, Future}

class ConnectionManagerSpec extends AsyncFlatSpec {

  final val log = Logger.getLogger(classOf[ConnectionManagerSpec].getName)

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  val aipConnection: ApiConnection = new ApiConnection(DEMO)
  val connFut: Future[HttpResponse] = aipConnection.futHandShakeConnection

  behavior of "ConnectionManager"

  it should "open a connection to DEMO IG" in {

    connFut.map { response: HttpResponse =>
      val isSuccessful = response.status.isSuccess()
      val hasCst = aipConnection.CST_TOKEN_HEADER.value().nonEmpty
      assert(isSuccessful)
      assert(hasCst)
    }
  }

  it should "be able to get data for an epic" in {
    val WrongGBPUSD = "CS.D.GBPUSD.TODY.IP"
    val errorResponse =
      "HttpEntity.Strict(application/json,{\"errorCode\":\"error.service.marketdata.instrument.epic.invalid\"})"

    val wrongEpicMarketData = Await.result(aipConnection.futMarketsRequest(WrongGBPUSD), FiniteDuration(3000L, SECONDS))
    assert(errorResponse === wrongEpicMarketData.entity.toString)
    wrongEpicMarketData.discardEntityBytes()

    val GBPUSD = "CS.D.GBPUSD.TODAY.IP"
    val epicMarketData = Await.result(aipConnection.futMarketsRequest(GBPUSD), FiniteDuration(3000L, SECONDS))
    val rightResponse = epicMarketData.entity.toString
    epicMarketData.discardEntityBytes()
    assert(rightResponse !== errorResponse)
  }

}
