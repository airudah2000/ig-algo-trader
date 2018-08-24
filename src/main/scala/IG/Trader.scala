package IG


import com.typesafe.scalalogging.Logger
import IG.Account.Positions
import IG.Connection.{ApiConnection, DEMO}

object Trader extends App with Util {

  private[this] final val log = Logger(this.getClass.getName)
  private[this] final val conn = new ApiConnection(DEMO) //TODO: Get mode from config eventually
  private[this] final val positions = new Positions(conn)


  log.info(
  s"""
       |**********************************************************************************************
       |Connection mode : [${conn.connectionMode}]
       |Account ID      : [${conn.credentials.accountId}]
       |CST TOKEN       : [${conn.CST_TOKEN_HEADER}]
       |**********************************************************************************************
    """.stripMargin)


  while (true) {

    positions.currentPositions(conn.futAccountPositionsRequest)

    Thread.sleep(3000L)
  }

}
