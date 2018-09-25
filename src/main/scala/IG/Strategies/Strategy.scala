package IG.Strategies

import akka.http.scaladsl.model.HttpResponse

import scala.concurrent.Future

trait Direction
case object Long        extends Direction
case object Short       extends Direction
case object NoDirection extends Direction

trait Interval {val value: Int}
case object TenM     extends Interval {override val value: Int = 10}
case object FifteenM extends Interval {override val value: Int = 15}
case object ThirtyM  extends Interval {override val value: Int = 30}
case object OneH     extends Interval {override val value: Int = 60}

trait Strategy {
  /**
    * Get a future of marketData for an instrument with a particular interval,
    * going back a given timeFrame
    * @param instrument
    * @param interval
    * @param timeFrame
    * @return
    */
  def marketData(instrument: Instrument, interval: Interval, timeFrame: Interval): Future[HttpResponse]

  def isSpreadOkay(bidPrice: Double, askPrice: Double): Boolean

  def tradeSignals: Unit

  val instruments: Seq[Instrument]

  val noSignal = Signal(EmptyInstrument, NoDirection)

}

case class Signal(instrument: Instrument, direction: Direction)
