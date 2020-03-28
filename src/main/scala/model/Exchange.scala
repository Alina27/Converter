package model

import argonaut._
import Argonaut._

case class Exchange()

case class ResponseFromPB(
                           //"date":"01.12.2014","bank":"PB","baseCurrency":980,"baseCurrencyLit":"UAH","exchangeRate":
                           date: String,
                           bank: String,
                           baseCurrency: Double,
                           baseCurrencyLit: String,
                           exchangeRate: List[ExchangeRate]
                         )

case class ExchangeRate(
                         baseCurrency: String,
                         currency: String,
                         saleRateNB: Double,
                         purchaseRateNB: Double,
                         saleRate: Option[Double],
                         purchaseRate: Option[Double]
                       )

case class Buy(NBU: Double, PV: Option[Double])
case class Sell(NBU: Double, PV: Option[Double])

sealed trait Errors

case class CannotParseJson(msg: String) extends Errors
case class PBError(msg: String) extends Errors
case class InvalidParam(param: String) extends Errors
case class NoDataForDay(msg: String) extends Errors

object ExchangeRate {
  implicit def ExchangeRateCodec: CodecJson[ExchangeRate] =
    casecodec6(ExchangeRate.apply, ExchangeRate.unapply)("baseCurrency", "currency", "saleRateNB", "purchaseRateNB", "saleRate", "purchaseRate")
}

object ResponseFromPB {
  implicit def ResponseFromPBCodec: CodecJson[ResponseFromPB] =
    casecodec5(ResponseFromPB.apply, ResponseFromPB.unapply)("date","bank","baseCurrency","baseCurrencyLit","exchangeRate")
}

object Buy {
  implicit def BuyCodec: CodecJson[Buy] =
    casecodec2(Buy.apply, Buy.unapply)("buyNBU","buyPB")
}

object Sell {
  implicit def SellCodec: CodecJson[Sell] =
    casecodec2(Sell.apply, Sell.unapply)("sellNBU","sellPB")
}
