package IG.Connection

import IG.Util
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.{Accept, RawHeader}
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import play.api.libs.json.{JsValue, Json}
import com.typesafe.scalalogging.Logger
import scala.concurrent.{Await, Future}


trait MODE
case object DEMO extends MODE
case object LIVE extends MODE

case class Credentials(un: String, pw: String, apiKey: String, url: String, accountId: String, version: Int)

trait ConnectionManager extends Util {

  final val acceptHeader: Accept = Accept.apply(MediaTypes.`application/json`)

  final val versionHeader: RawHeader = RawHeader("Version", credentials.version.toString)

  final val apiKeyHeader: RawHeader = RawHeader("X-IG-API-KEY", credentials.apiKey)

  val connectionEntity: JsValue

  val sessionEntity: JsValue

  val handshakeConnectionRequestBody: HttpRequest

  def failureResponse(r: HttpResponse): HttpHeader

  def futHandShakeConnection: Future[HttpResponse]

  def CST_TOKEN_HEADER: HttpHeader

  def X_SECURITY_TOKEN_HEADER: HttpHeader

  def credentials: Credentials

}

class ApiConnection(val connectionMode: MODE) extends ConnectionManager with Util {

  final val log = Logger(this.getClass.getName)

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  override def credentials: Credentials = connectionMode match {
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

  override val connectionEntity: JsValue = stringToJsValue(
    s"""
       |{
       |  "identifier": "${credentials.un}",
       |  "password": "${credentials.pw}"
       |}
       """.stripMargin)

  override val handshakeConnectionRequestBody: HttpRequest = HttpRequest(
    POST,
    uri = s"${credentials.url}/session",
    headers = handshakeRequestHeaders,
    entity = HttpEntity(ContentTypes.`application/json`, connectionEntity.toString)
  )

  override def futHandShakeConnection: Future[HttpResponse] = Http().singleRequest(handshakeConnectionRequestBody)

  val handshakeResponse: HttpResponse = Await.result(futHandShakeConnection, patienceDuration)

  override def CST_TOKEN_HEADER: HttpHeader =
    handshakeResponse.headers.find(h => h.name() == "CST").getOrElse(failureResponse(handshakeResponse))
  override def X_SECURITY_TOKEN_HEADER: HttpHeader =
    handshakeResponse.headers.find(h => h.name() == "X-SECURITY-TOKEN").getOrElse(failureResponse(handshakeResponse))

  override def failureResponse(r: HttpResponse): HttpHeader = {
    RawHeader(r.status.value, jsonStrFromEntity(r.entity))
  }

  def handshakeResponseJson: String = {
    val jsfe = jsonStrFromEntity(handshakeResponse.entity)
    val jsonResponse = prettyPrint(stringToJsValue(jsfe))
    log.info(jsonResponse)
    jsonResponse
  }

  override val sessionEntity: JsValue = stringToJsValue(
    s"""
       |{
       |  "accountId": "${credentials.accountId}",
       |  "defaultAccount": "True"
       |}
       """.stripMargin)

  def defaultHttpRequest: HttpRequest = {
    val defaultRequestHeaders: List[HttpHeader] = List(acceptHeader, apiKeyHeader, CST_TOKEN_HEADER, X_SECURITY_TOKEN_HEADER)
    HttpRequest(GET, headers = defaultRequestHeaders)
  }

  def futAccountDetailsRequest: Future[HttpResponse] =
    Http().singleRequest(defaultHttpRequest.copy(uri = s"${credentials.url}/accounts"))

  def futMarketsRequest(epicId: String): Future[HttpResponse] =
    Http().singleRequest(defaultHttpRequest.copy(uri = s"${credentials.url}/markets/$epicId"))

  def futAccountPositionsRequest: Future[HttpResponse] =
    Http().singleRequest(defaultHttpRequest.copy(uri = s"${credentials.url}/positions"))

}
