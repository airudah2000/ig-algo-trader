package IG.Connection

import java.util.logging._

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.{Accept, RawHeader}
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import play.api.libs.json.{JsValue, Json}

import scala.concurrent.duration.{FiniteDuration, SECONDS}
import scala.concurrent.{Await, Future}

trait MODE
case object DEMO extends MODE
case object LIVE extends MODE

case class Credentials(un: String, pw: String, apiKey: String, url: String, accountId: String, version: Int)

trait ConnectionManager {

  final val acceptHeader: Accept = Accept.apply(MediaTypes.`application/json`)

  final val versionHeader: RawHeader = RawHeader("Version", getCredentials.version.toString)

  final val apiKeyHeader: RawHeader = RawHeader("X-IG-API-KEY", getCredentials.apiKey)

  final val defaultHeader = RawHeader("", "")

  val connectionEntity: JsValue

  val sessionEntity: JsValue

  val handshakeConnectionRequestBody: HttpRequest

  def futHandShakeConnection: Future[HttpResponse]

  def CST_TOKEN_HEADER: HttpHeader

  def X_SECURITY_TOKEN_HEADER: HttpHeader

  def getCredentials: Credentials

}

class ApiConnection(connectionMode: MODE) extends ConnectionManager {

  final val log = Logger.getLogger(classOf[ApiConnection].getName)

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
        modeConfig.getString("ig.demo.api.accountid"),
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
        modeConfig.getString("ig.live.api.accountid"),
        modeConfig.getInt("ig.live.api.version")
      )
    }
  }

  val handshakeRequestHeaders = List(acceptHeader, versionHeader, apiKeyHeader)

  override val connectionEntity: JsValue = Json.parse(
    s"""
       |{
       |  "identifier": "${getCredentials.un}",
       |  "password": "${getCredentials.pw}"
       |}
       """.stripMargin)

  override val handshakeConnectionRequestBody: HttpRequest = HttpRequest(
    POST,
    uri = s"${getCredentials.url}/session",
    headers = handshakeRequestHeaders,
    entity = HttpEntity(ContentTypes.`application/json`, connectionEntity.toString)
  )

  override def futHandShakeConnection: Future[HttpResponse] = Http().singleRequest(handshakeConnectionRequestBody)

  private lazy val handshakeResponse: HttpResponse = Await.result(futHandShakeConnection, FiniteDuration(3000L, SECONDS))

  override def CST_TOKEN_HEADER =
    handshakeResponse.headers.find(h => h.name() == "CST").getOrElse(defaultHeader)
  override def X_SECURITY_TOKEN_HEADER =
    handshakeResponse.headers.find(h => h.name() == "X-SECURITY-TOKEN").getOrElse(defaultHeader)

  //TODO: Get the values out in a clean way
  def getHandshakeResponseEntity = handshakeResponse.entity

  override val sessionEntity: JsValue = Json.parse(
    s"""
       |{
       |  "accountId": "${getCredentials.accountId}",
       |  "defaultAccount": "True"
       |}
       """.stripMargin)

  def getDefaultRequest: HttpRequest = {
    val defaultRequestHeaders: List[HttpHeader] = List(acceptHeader, apiKeyHeader, CST_TOKEN_HEADER, X_SECURITY_TOKEN_HEADER)
    HttpRequest(GET, headers = defaultRequestHeaders)
  }

  def futAccountDetailsRequest: Future[HttpResponse] =
    Http().singleRequest(getDefaultRequest.copy(uri = s"${getCredentials.url}/accounts"))

  def futMarketsRequest(epicId: String): Future[HttpResponse] =
    Http().singleRequest(getDefaultRequest.copy(uri = s"${getCredentials.url}/markets/$epicId"))

  def futAccountPositionsRequest: Future[HttpResponse] =
    Http().singleRequest(getDefaultRequest.copy(uri = s"${getCredentials.url}/positions"))

}
