package model

import argonaut._
import Argonaut._

case class ResponseFromPB(
                           date: String,
                           bank: String,
                           baseCurrency: Double,
                           baseCurrencyLit: String,
                           exchangeRate: Set[ExchangeRate]
                         )

case class ExchangeRate(
                         baseCurrency: String,
                         currency: Option[String] ,
                         saleRateNB: Double,
                         purchaseRateNB: Double,
                         saleRate: Option[Double],
                         purchaseRate: Option[Double]
                       )

case class ConversionResult(NBU: Double, PV: Option[Double])

sealed trait Errors

case class CannotParseJson(msg: String) extends Errors
case class PBError(msg: String) extends Errors
case class NoDataForDay(msg: String) extends Errors
case class UnavailableCurrency(msg: String) extends Errors


object ExchangeRate {
  implicit def ExchangeRateCodec: CodecJson[ExchangeRate] =
    casecodec6(ExchangeRate.apply, ExchangeRate.unapply)("baseCurrency", "currency", "saleRateNB", "purchaseRateNB", "saleRate", "purchaseRate")
}

object ResponseFromPB {
  implicit def ResponseFromPBCodec: CodecJson[ResponseFromPB] =
    casecodec5(ResponseFromPB.apply, ResponseFromPB.unapply)("date","bank","baseCurrency","baseCurrencyLit","exchangeRate")
}


object ConversionResult {
  implicit def ConversionResultCodec: CodecJson[ConversionResult] =
  CodecJson((cr: ConversionResult) =>
    ("NBU" := cr.NBU) ->:
      ("PB" :=? cr.PV) ->?: jEmptyObject,
    c => for {
      nbu <- (c --\ "NBU").as[Double]
      pb <- (c --\ "PB").as[Option[Double]]
    } yield ConversionResult(nbu, pb)
  )
}
