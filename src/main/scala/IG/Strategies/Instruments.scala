package IG.Strategies

trait Instrument {
  override def toString: String =
    super
      .getClass.
      getSimpleName
      .replace("_", ".")
      .replace("$", "") //TODO: Find a cleaner way to do this
}

case object EmptyInstrument extends Instrument

trait Stocks extends Instrument
case object KA_D_MCSLN_DAILY_IP extends Stocks

trait Fx extends Instrument
case object CS_D_GBPUSD_TODAY_IP extends Fx

