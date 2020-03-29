package service

import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpMethods, HttpRequest, MediaTypes, StatusCodes}
import akka.actor.ActorSystem
import akka.http.scaladsl.model.headers.Accept
import com.typesafe.config.{Config, ConfigFactory}

import scala.concurrent.Future
import akka.stream.ActorMaterializer
import java.time.format.DateTimeFormatter

import argonaut.Parse
import model._
import scala.concurrent.duration._

object ExchangeService {
  val conf: Config = ConfigFactory.load()

  implicit val system: ActorSystem = ActorSystem("exchange-actor", conf)
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  val timeout: FiniteDuration = 300.millis

  import scala.concurrent.ExecutionContext.Implicits.global

  private def getDataFromPB(myDate: Option[String]): Future[Either[Errors, String]] = {

    val date = myDate match {
      case Some(value) => value
      case None => DateTimeFormatter.ofPattern("dd.MM.YYYY").format(java.time.LocalDate.now)
    }
    val url = s"https://api.privatbank.ua/p24api/exchange_rates?json&date=$date"

    for {
      resp <- Http().singleRequest(HttpRequest(HttpMethods.GET, url).addHeader(Accept(MediaTypes.`application/json`)))
    } yield if (resp.status == StatusCodes.OK) {
      resp.entity.toStrict(timeout).map(_.data).map(bs => Right(bs.utf8String.toString))
    } else {
      Future.successful(Left(PBError("Service temporary unavailable")))
    }
    }.flatten


  private def parseRespToExRate(json: String): Either[Errors, Set[ExchangeRate]] = {
    Parse.decodeOption[ResponseFromPB](json) match {
      case Some(respPB) => Right(respPB.exchangeRate)
      case None => Left(CannotParseJson("Cannot parse json"))
    }
  }


  private def findDataForCurrency(exchangeRate: Set[ExchangeRate], currency: String ): Either[Errors, Option[ExchangeRate]] = {
    if(exchangeRate.nonEmpty)
      Right(exchangeRate.find(_.currency match {
        case Some(curr) => curr == currency
        case None => false
      }))
    else
      Left(NoDataForDay("No conversion results for this day") )
  }

  private def createConversionResult(exchangeType: String, exchangeRate: Option[ExchangeRate], rate: Double ): Either[Errors, ConversionResult] = {

    exchangeRate match {

      case Some(exchange) =>
        val purchPB = exchange.purchaseRate
        val salePB = exchange.saleRate

        val purchNBU = exchange.purchaseRateNB
        val sellNBU = exchange.purchaseRateNB

        exchangeType match {
          case "buy" => salePB match {
            case Some(pbRate) => Right(ConversionResult(sellNBU *rate, Some(pbRate * rate)))
            case None => Right(ConversionResult(sellNBU *rate, None))
          }
          case "sell" => purchPB match {
            case Some(pbRate) => Right(ConversionResult(purchNBU *rate, Some(pbRate * rate)))
            case None => Right(ConversionResult(purchNBU *rate, None))
          }
        }

      case None => Left(UnavailableCurrency("Unfortunately, unavailable conversion for this currency"))

    }

  }

  def calculate(exchangeType: String, currency: String, rateToChange: Double, date: Option[String]): Future[Either[Errors, ConversionResult]] = {
    for {
      pbResponse <- getDataFromPB(date)
    } yield pbResponse match {

      case Right(jsonResponse) => parseRespToExRate(jsonResponse) match {

        case Right(exRateSet) => findDataForCurrency(exRateSet, currency) match {

          case Right(exchangeRate) => createConversionResult(exchangeType, exchangeRate, rateToChange) match {

            case Right(conversionResult) => Right(conversionResult)
            case Left(err) => Left(err)
          }

          case Left(err) => Left(err)
        }
        case Left(err) => Left(err)
      }
      case Left(err) => Left(err)
    }
  }
}