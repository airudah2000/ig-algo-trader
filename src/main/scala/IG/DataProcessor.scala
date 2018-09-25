package IG

import java.util.{Calendar, Date}

import akka.http.scaladsl.model.ResponseEntity
import akka.stream.Materializer
import play.api.libs.json._

case class EpicSnapshot(marketStatus             : String,
                        netChange                : Double,
                        percentageChange         : Double,
                        updateTime               : Date, //TODO: Retrieve just the timestamp
                        delayTime                : Int,
                        bid                      : Double,
                        offer                    : Double,
                        high                     : Option[Double],
                        low                      : Option[Double],
                        controlledRiskExtraSpread: Int)

case class AccountInfo(accountId      : String,
                       accountName    : String,
                       status         : String,
                       accountType    : String,
                       preferred      : Boolean,
                       accountBalance : Balance,
                       currency       : String,
                       canTransferFrom: Boolean,
                       canTransferTo  : Boolean)

case class Balance(balance: Double, deposit: Double, profitLoss: Double, available: Double)

class DataProcessor extends Util {

  def entityToSnapshot(entity: ResponseEntity)(implicit mat: Materializer): EpicSnapshot = {
    val snapshot = "snapshot"
    val snapshotJValue: JsValue = stringToJsValue(jsonStrFromEntity(entity))

    val ms  : String         = (snapshotJValue \ snapshot \ "marketStatus").as[String]
    val nc  : Double         = (snapshotJValue \ snapshot \ "percentageChange").as[Double]
    val pc  : Double         = (snapshotJValue \ snapshot \ "netChange").as[Double]
    val ut  : Date           = Calendar.getInstance().getTime
    val dt  : Int            = (snapshotJValue \ snapshot \ "delayTime").as[Int]
    val b   : Double         = (snapshotJValue \ snapshot \ "bid").as[Double]
    val o   : Double         = (snapshotJValue \ snapshot \ "offer").as[Double]
    val h   : Option[Double] = (snapshotJValue \ snapshot \ "high").asOpt[Double]
    val l   : Option[Double] = (snapshotJValue \ snapshot \ "low").asOpt[Double]
    val cres: Int            = (snapshotJValue \ snapshot \ "controlledRiskExtraSpread").as[Int]

    val epicSnapshot = EpicSnapshot(ms, nc, pc, ut, dt, b, o, h, l, cres)
    epicSnapshot
  }

  def entityToAccount(entity: ResponseEntity)(implicit mat: Materializer): Map[String, AccountInfo] = {
    val accountsJValue: JsValue = stringToJsValue(jsonStrFromEntity(entity))
    val accountsList: Seq[JsValue] = (accountsJValue \ "accounts").as[JsArray].value
    val balance = "balance"

    (for (account <- accountsList) yield {
      val accountBalance: Balance = Balance(
        (account \ balance \ balance).as[Double],
        (account \ balance \ "deposit").as[Double],
        (account \ balance \ "profitLoss").as[Double],
        (account \ balance \ "available").as[Double])

      val acId     = (account \ "accountId").as[String]
      val acName   = (account \ "accountName").as[String]
      val acTyp    = (account \ "accountType").as[String]
      val status   = (account \ "status").as[String]
      val pref     = (account \ "preferred").as[Boolean]
      val ccy      = (account \ "currency").as[String]
      val canTFrom = (account \ "canTransferFrom").as[Boolean]
      val canTTo   = (account \ "canTransferTo").as[Boolean]

      acTyp -> AccountInfo(acId, acName, status, acTyp, pref, accountBalance, ccy, canTFrom, canTTo)

    }).toMap

  }
}