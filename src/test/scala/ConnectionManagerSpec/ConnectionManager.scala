package ConnectionManagerSpec

import org.scalatest.AsyncFlatSpec
import Connection._
import akka.actor.ActorSystem
import akka.http.scaladsl.model._

import scala.concurrent.Future

class ConnectionManager extends AsyncFlatSpec {

  implicit val system = ActorSystem()

  behavior of "ApiConnection"

  it should "open a connection to DEMO IG" in {

    val aipConnection: ApiConnection = new ApiConnection(DEMO)
    val connFut: Future[HttpResponse] = aipConnection.futConnection

    connFut.map { response: HttpResponse =>
      val isSuccessful = response.status.isSuccess()
      val CST: String = response.getHeader("CST").get().value()
      println(s"CST Value: [$CST]")
      assert(isSuccessful)
    }

  }


}
