package IG

import akka.http.scaladsl.model.ResponseEntity
import akka.stream.Materializer
import akka.stream.scaladsl.Sink
import akka.util.ByteString

import scala.concurrent.duration.{FiniteDuration, SECONDS}
import scala.concurrent.Await
import play.api.libs.json._

trait Util {

  def jsonStrFromEntity(entity: ResponseEntity)(implicit mat: Materializer): String = {
    val bs: ByteString = Await.result(
      entity.dataBytes.runWith(Sink.fold(ByteString.empty)(_ ++ _)),
      FiniteDuration(3000L, SECONDS)
    )
    bs.utf8String
  }

  def stringToJsValue(jsonString: String): JsValue = Json.parse(jsonString)

  def prettyPrint(jsv: JsValue): String = Json.prettyPrint(jsv)

  def prettyPrintEntity(entity: ResponseEntity)(implicit mat: Materializer): String = {
    prettyPrint(stringToJsValue(jsonStrFromEntity(entity)))
  }
}
