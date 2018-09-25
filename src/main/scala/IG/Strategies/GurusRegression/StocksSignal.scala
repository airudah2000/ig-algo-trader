package IG.Strategies.GurusRegression

import java.util.logging.Logger

import IG.Connection.ApiConnection
import IG.{DataProcessor, EpicSnapshot}
import IG.Strategies._
import akka.actor.ActorSystem
import akka.http.scaladsl.model.HttpResponse
import akka.stream.ActorMaterializer

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class StocksSignal(conn: ApiConnection) extends Strategy {

  private[this] final val log = Logger.getLogger(classOf[StocksSignal].getName)
  implicit val system: ActorSystem = ActorSystem()
  implicit val mat: ActorMaterializer = ActorMaterializer()

  private val processor = new DataProcessor

  override val instruments: Seq[Instrument] = Seq(KA_D_MCSLN_DAILY_IP)

  override def marketData(instrument: Instrument, interval: Interval, timeFrame: Interval = OneH): Future[HttpResponse] = {
    conn.futMarketsRequest(instrument.toString.stripMargin)
  }


  override def tradeSignals: Unit = instruments.foreach{ ins: Instrument =>
      marketData(ins, OneH).onComplete{
        case scala.util.Success(response: HttpResponse) => {
          response.discardEntityBytes()
          val snapshot: EpicSnapshot = processor.getSnapshotData(response.entity)
          log.info(snapshot.toString)

          val direction: Direction = calculateSide(snapshot.bid, snapshot.offer)
          val spreadOkay: Boolean = isSpreadOkay(snapshot.bid, snapshot.offer)

          if(spreadOkay){
            log.info(s"Spread okay for: [${ins}]")
            Signal(ins, direction) //TODO: send signal to be traded. Perform other checks first (i.e. account balance, percentage, etc)
          } else {
            noSignal
          }
        }
        case scala.util.Failure(ex: Throwable) => {
          log.severe("Something went wrong: " + ex.getMessage)
          ex
        }
      }
    }

  def isSpreadOkay(bidPrice: Double, askPrice: Double): Boolean = Math.abs(bidPrice - askPrice) >= 1.0

  def calculateSide(bid: Double, offer: Double): Direction = if(bid > offer) Long else Short //TODO: This is wrong. Check Gurus code

}
