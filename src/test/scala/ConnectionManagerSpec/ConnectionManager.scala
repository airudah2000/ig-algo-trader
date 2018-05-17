package ConnectionManagerSpec

import org.scalatest.FunSuite
import Connection._
import play.api.libs.json.Json


class ConnectionManager extends FunSuite {

  val connectionManager: Connection.ConnectionManager = new ApiConnection(DEMO)

  test("A ConnectionManager header should be verifiable JSON"){

    val expectedHeadersString: String =
      """{
        |  "Content-Type" : "application/json; charset=utf-8",
        |  "Accept" : "application/json; charset=utf-8",
        |  "X-IG-API-KEY" : "XXXXX",
        |  "Version" : "2"
        |}""".stripMargin

    val headersJsonPretty: String = Json.prettyPrint(connectionManager.headers)

    println(expectedHeadersString)
    println(headersJsonPretty)

    assert(expectedHeadersString == headersJsonPretty)
  }

  ignore("Open a connection to DEMO IG"){

  }

}
