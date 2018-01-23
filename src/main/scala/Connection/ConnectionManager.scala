package Connection

import play.api.libs.json._

trait MODE
case object DEMO extends MODE
case object LIVE extends MODE


class ConnectionManager(mode: MODE, apiKey: String) {
  final val LIVE_URL = "https://api.ig.com/gateway/deal"
  final val DEMO_URL = ""
  final val API_ENDPOINT: String = "session"

  final val headers: JsValue = Json.parse(s"""{
    "Content-Type":"application/json; charset=utf-8",
    "Accept":"application/json; charset=utf-8",
    "X-IG-API-KEY":"$apiKey",
    "Version":"2"
    }""")


}

object ConnectionManager {
  def apply(mode: MODE, apiKey: String): ConnectionManager = new ConnectionManager(mode, apiKey)
}
