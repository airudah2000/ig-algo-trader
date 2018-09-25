package IG.Strategies

import java.util.logging.Logger

import IG.Connection.{ApiConnection, DEMO}
import IG.Strategies.GurusRegression.StocksSignal
import IG.Util
import akka.actor.ActorSystem
import akka.http.scaladsl.model.HttpResponse
import akka.stream.ActorMaterializer
import org.scalatest.AsyncFlatSpec

import scala.concurrent.Future

class GurusRegressionSpec extends AsyncFlatSpec with Util {

  private[this] final val log = Logger.getLogger(classOf[GurusRegressionSpec].getName)

  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()

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
