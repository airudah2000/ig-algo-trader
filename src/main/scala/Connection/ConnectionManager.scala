package Connection

import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods.POST
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.{Accept, RawHeader}
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import play.api.libs.json.{JsValue, Json}

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.FiniteDuration
import scala.util.{Failure, Success}

trait MODE
case object DEMO extends MODE
case object LIVE extends MODE

case class Credentials(un: String, pw: String, apiKey: String, url: String, version: Int)

trait IGConnectionManager {

  val acceptHeader: Accept = Accept.apply(MediaTypes.`application/json`)

  val versionHeader: RawHeader = RawHeader("Version", getCredentials.version.toString)

  def apiKeyHeader: RawHeader = RawHeader("X-IG-API-KEY", getCredentials.apiKey)

  val connectionEntity: JsValue

  val connectionRequest: HttpRequest

  val connection: HttpResponse

  def getCredentials: Credentials

}

class ApiConnection(connectionMode: MODE) extends IGConnectionManager {

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  override def getCredentials: Credentials = connectionMode match {
    case DEMO => {
      val modeConfig = ConfigFactory.load()
      Credentials(
        modeConfig.getString("ig.demo.api.username"),
        modeConfig.getString("ig.demo.api.password"),
        modeConfig.getString("ig.demo.api.key"),
        modeConfig.getString("ig.demo.connection.url"),
        modeConfig.getInt("ig.demo.api.version")
      )
    }
    case LIVE => {
      val modeConfig = ConfigFactory.load("live")
      Credentials(
        modeConfig.getString("ig.live.api.username"),
        modeConfig.getString("ig.live.api.password"),
        modeConfig.getString("ig.live.api.key"),
        modeConfig.getString("ig.live.connection.url"),
        modeConfig.getInt("ig.live.api.version")
      )
    }
  }

  override val connectionEntity: JsValue = Json.parse(
    s"""
       |{
       |  "identifier": "${getCredentials.un}",
       |  "password": "${getCredentials.pw}",
       |  "encryptedPassword": null
       |}
       """.stripMargin)

  val apiHeaders = List(acceptHeader, versionHeader, apiKeyHeader)

  override val connectionRequest: HttpRequest = HttpRequest(
    POST,
    uri = getCredentials.url,
    headers = apiHeaders,
    entity = HttpEntity(ContentTypes.`application/json`, connectionEntity.toString)
  )

  val futConnection: Future[HttpResponse] = Http().singleRequest(connectionRequest)

  futConnection onComplete {
    case Success(response: HttpResponse) => println("\nCST Code: " + response.httpMessage.getHeader("CST").get().value())
    case Failure(f) => println(s"Failed: [${f.getMessage}]")
  }

  override lazy val connection: HttpResponse = Await.result(futConnection, FiniteDuration(5.toLong, TimeUnit.SECONDS))
}
