package ConnectionManagerSpec

import org.scalatest.FunSuite
import Connection._
import akka.http.scaladsl.model._

class ConnectionManager extends FunSuite {

  test("Open a connection to DEMO IG") {

    val aipConnection = new ApiConnection(DEMO)

    val theResponse: HttpResponse = aipConnection.connection

    assert(theResponse.status.isSuccess())

  }


}
