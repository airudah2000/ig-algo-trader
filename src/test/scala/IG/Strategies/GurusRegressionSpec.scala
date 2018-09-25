package IG.Strategies

import IG.Connection.{ApiConnection, DEMO}
import IG.Strategies.GurusRegression.StocksSignal
import IG.{LogUtil, Util}
import akka.actor.ActorSystem
import akka.http.scaladsl.model.HttpResponse
import akka.stream.ActorMaterializer
import org.scalatest.AsyncFlatSpec

import scala.concurrent.Future

class GurusRegressionSpec extends AsyncFlatSpec with Util with LogUtil {

  implicit val system: ActorSystem = ActorSystem()
  implicit val mat: ActorMaterializer = ActorMaterializer()

  val apiConnection: ApiConnection = new ApiConnection(DEMO)
  val connFut: Future[HttpResponse] = apiConnection.futHandShakeConnection
  val guruRegression = new StocksSignal(apiConnection)

  behavior of "GurusRegression"

  it should "print the json format of the response entity of an epic" in {
    guruRegression.marketData(KA_D_MCSLN_DAILY_IP, OneH).map{ response =>
      response.discardEntityBytes()
      val result = response.entity
      log.info(prettyPrintEntity(result))
      assert(!result.isKnownEmpty())
    }
  }
}
