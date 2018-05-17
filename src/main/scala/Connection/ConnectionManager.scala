package Connection

import play.api.libs.json._
import com.typesafe.config.{Config, ConfigFactory}

trait MODE
case object DEMO extends MODE
case object LIVE extends MODE


trait ConnectionManager {

  val mode: MODE

  val headers: JsValue

  def isConnected: Boolean

  def getConnection: Any

  def connection: String

  def getApiKey: String
}

class ApiConnection(connectionMode: MODE) extends ConnectionManager {

  override val mode: MODE = connectionMode

  override val headers: JsValue = Json.parse(s"""{
    "Content-Type":"application/json; charset=utf-8",
    "Accept":"application/json; charset=utf-8",
    "X-IG-API-KEY":"$getApiKey",
    "Version":"2"
    }""")

  override def isConnected: Boolean = ???

  override def getConnection: Unit = ???

  override def connection: String = ???

  override def getApiKey: String = mode match {
    case DEMO => ConfigFactory.load().getString ("ig.demo.api.key")
    case LIVE => ConfigFactory.load("live").getString ("ig.live.api.key")
  }
}
