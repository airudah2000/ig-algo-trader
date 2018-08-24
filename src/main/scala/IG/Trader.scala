package IG

import java.util.logging.Logger

import IG.Account.Positions
import IG.Connection.{ApiConnection, DEMO, LIVE}

object Trader extends App with Util {

  private final val log = Logger.getLogger(this.getClass.getName)
  private final val conn = new ApiConnection(DEMO) //TODO: Get mode from config eventually
  private final val positions = new Positions(conn)


  log.info(
  s"""
       |****************************************
       |Connection mode: [${conn.connectionMode}]
       |Account ID     : [${conn.getCredentials.accountId}]
       |CST TOKEN      : [${conn.CST_TOKEN_HEADER}]
       |****************************************
    """.stripMargin)


  while (true) {

    positions.getCurrentPositions(conn.futAccountPositionsRequest)

    Thread.sleep(3000L)
  }

}
