package ConnectionManagerSpec

import org.scalatest.FunSuite
import Connection._
import play.api.libs.json.Json


class ConnectionManager extends FunSuite {

  val apiKey = "abcd1234"
  val connectionManager: Connection.ConnectionManager = ConnectionManager(DEMO, apiKey)

  test("A ConnectionManager header should be verifiable JSON"){

    val expectedHeadersString: String =
      """{
        |  "Content-Type" : "application/json; charset=utf-8",
        |  "Accept" : "application/json; charset=utf-8",
        |  "X-IG-API-KEY" : "abcd1234",
        |  "Version" : "2"
        |}""".stripMargin

    val headersJsonPretty: String = Json.prettyPrint(connectionManager.headers)

    println(expectedHeadersString)
    println(headersJsonPretty)

    assert(expectedHeadersString == headersJsonPretty)
  }

}
