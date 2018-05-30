package ConnectionManagerSpec

import org.scalatest.FunSuite
import Connection._
import akka.http.scaladsl.model._

class ConnectionManager extends FunSuite {

  test("Open a connection to DEMO IG") {

    val aipConnection = new ApiConnection(DEMO)

    val theResponse: HttpResponse = aipConnection.connection

    println("\nResponse Message:\n" + theResponse.httpMessage + "\n\n\n")
//    println("\nResponse Headers:\n" + theResponse.headers)
//    println("\nResponse Entity:\n" + theResponse.entity)
//    println("\nResponse Status:\n" + theResponse.status.isSuccess())

  }


}
