package IG

import akka.http.scaladsl.model.ResponseEntity
import akka.stream.Materializer
import akka.stream.scaladsl.Sink
import akka.util.ByteString
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory
import play.api.libs.json._

import scala.concurrent.Await
import scala.concurrent.duration.{FiniteDuration, SECONDS}

trait Util {
  final val patienceDuration = FiniteDuration(5000L, SECONDS)

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

trait LogUtil {
  final lazy val log = Logger(LoggerFactory.getLogger(getClass.getName))
}