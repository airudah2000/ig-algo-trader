package IG

import java.util.logging.Logger

import IG.Connection.{ApiConnection, DEMO}
import IG.Strategies.GurusRegression.StocksSignal
import IG.Strategies.{KA_D_MCSLN_DAILY_IP, OneH}
import akka.actor.ActorSystem
import akka.http.scaladsl.model.HttpResponse
import akka.stream.ActorMaterializer
import org.scalatest.AsyncFlatSpec
import play.api.libs.json.{JsArray, JsNull, JsValue}

import scala.concurrent.{Await, Future}

class DataProcessorTest extends AsyncFlatSpec with Util {
  private[this] final val log = Logger.getLogger(classOf[DataProcessorTest].getName)

  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  val apiConnection: ApiConnection = new ApiConnection(DEMO)
  val connFut: Future[HttpResponse] = apiConnection.futHandShakeConnection
  val guruRegression = new StocksSignal(apiConnection)
  val processor = new DataProcessor

  behavior of "DataProcessor"

  it should "retrieve snapshot data from an epic entity response data" in {
    guruRegression.marketData(KA_D_MCSLN_DAILY_IP, OneH).map{ response =>
      response.discardEntityBytes()
      val entityCaseClass: EpicSnapshot = processor.entityToSnapshot(response.entity)
      log.info(entityCaseClass.toString)

      assert(Seq("EDITS_ONLY", "OFFLINE", "TRADEABLE").contains(entityCaseClass.marketStatus))
    }
  }

  it should "build a collection of AccountInfo from an account query response" in {
    val sampleJsonResponseString =
      """
        |{
        |  "accounts" : [ {
        |    "accountId" : "XVP2H",
        |    "accountName" : "Demo-Spread bet",
        |    "accountAlias" : null,
        |    "status" : "ENABLED",
        |    "accountType" : "SPREADBET",
        |    "preferred" : true,
        |    "balance" : {
        |      "balance" : 10754.21,
        |      "deposit" : 0,
        |      "profitLoss" : 0,
        |      "available" : 10754.21
        |    },
        |    "currency" : "GBP",
        |    "canTransferFrom" : true,
        |    "canTransferTo" : true
        |  }, {
        |    "accountId" : "XVP2I",
        |    "accountName" : "Demo-CFD",
        |    "accountAlias" : null,
        |    "status" : "ENABLED",
        |    "accountType" : "CFD",
        |    "preferred" : false,
        |    "balance" : {
        |      "balance" : 12532.8,
        |      "deposit" : 0,
        |      "profitLoss" : 0,
        |      "available" : 12532.8
        |    },
        |    "currency" : "GBP",
        |    "canTransferFrom" : true,
        |    "canTransferTo" : true
        |  } ]
        |}
      """.stripMargin

    val jsv: JsValue = stringToJsValue(sampleJsonResponseString)

    val accntArr: JsArray = (jsv \ "accounts").as[JsArray]

    val response: HttpResponse = Await.result(apiConnection.futAccountDetailsRequest, patienceDuration)
    response.discardEntityBytes()

    val processedAccountsData: Map[String, AccountInfo] = processor.entityToAccount(response.entity)

    val actualSpreadBetAccount: AccountInfo = processedAccountsData("SPREADBET")
    val expectedSpreadBetAccount: JsValue = accntArr.value.headOption.getOrElse(JsNull)

    log.info(s"Expected Account Json: [$expectedSpreadBetAccount]")
    log.info(s"Actual Account Object: [${actualSpreadBetAccount.toString}]")

    assert(actualSpreadBetAccount.accountType === (expectedSpreadBetAccount \ "accountType").as[String])
    assert(actualSpreadBetAccount.accountName === (expectedSpreadBetAccount \ "accountName").as[String])
    assert(actualSpreadBetAccount.status      === (expectedSpreadBetAccount \ "status").as[String])
    assert(actualSpreadBetAccount.currency    === (expectedSpreadBetAccount \ "currency").as[String])

  }
}
