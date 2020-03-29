package service

import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpMethods, HttpRequest, MediaTypes, StatusCodes}
import akka.actor.ActorSystem
import akka.http.scaladsl.model.headers.Accept
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.util.ByteString
import com.typesafe.config.{Config, ConfigFactory}

import scala.concurrent.Future
import akka.stream.ActorMaterializer
import java.time.format.DateTimeFormatter

import argonaut.Parse
import model._

object ExchangeService {
  val conf: Config = ConfigFactory.load()

  implicit val system: ActorSystem = ActorSystem("exchange-actor", conf)
  implicit val materializer: ActorMaterializer = ActorMaterializer()


  import scala.concurrent.ExecutionContext.Implicits.global

// TODO: remove Unmarshal
  private def dataFromPB(myDate: Option[String]): Future[Either[Errors, String]] = {

    val date = myDate match {
      case Some(value) => value
      case None => DateTimeFormatter.ofPattern("dd.MM.YYYY").format(java.time.LocalDate.now)
    }
    val url = s"https://api.privatbank.ua/p24api/exchange_rates?json&date=$date"

    for {
      resp <- Http().singleRequest(HttpRequest(HttpMethods.GET, url).addHeader(Accept(MediaTypes.`application/json`)))
    } yield if (resp.status == StatusCodes.OK) {
      Unmarshal(resp.entity).to[ByteString].map(bs => Right(bs.utf8String.toString))
    } else {
      Unmarshal(resp.entity).to[ByteString].map(bs => Left(PBError("Service temporary unavailable")))
    }
    }.flatten


  // parse data from pb
  private def parseResponce(json: String): Either[Errors, Set[ExchangeRate]] = {
    Parse.decodeOption[ResponseFromPB](json) match {
      case Some(resp) => Right(resp.exchangeRate)
      case None => Left(CannotParseJson("cannot parse json"))
    }
  }

  private def checkSet(list: Set[ExchangeRate], currency: String ): Either[Errors, Option[ExchangeRate]] = {
    if(list.nonEmpty)
    // TODO: None case
      Right(list.find(_.currency match {
        case Some(value) => value == currency
      }))
    else
      Left(NoDataForDay("Sorry, no info for this day") )
  }

//  TODO: create one method
  private def createSell(rateNB: Double, ratePB: Option[Double], rate:Double): Sell = {
    ratePB match {
      case Some(pbRate) => Sell(rateNB *rate, Some(pbRate * rate))
      case None => Sell(rateNB *rate, None)
    }
  }

  private def createBuy(rateNB: Double, ratePB: Option[Double], rate:Double): Buy = {
    ratePB match {
      case Some(pbRate) => Buy(rateNB *rate, Some(pbRate * rate))
      case None => Buy(rateNB *rate, None)
    }
  }

  //  TODO: maybe also create one method
  def getForBuy(currency: String, rate: Double, date: Option[String]): Future[Either[Errors, Buy]] = {
    for {
      res <- dataFromPB(date)
    } yield res match {
      case Right(value) => parseResponce(value) match {
          case Right(value2) => checkSet(value2, currency) match {
            case Right(data) => Right(createBuy(data.get.saleRateNB, data.get.saleRate, rate))
            case Left(e) => Left(e)
          }
          case Left(err) => Left(err)
        }
      case Left(err) => Left(err)
    }
  }


  def getForSale(currency: String, rate: Double, date: Option[String]): Future[Either[Errors, Sell]] = {
    for {
      res <- dataFromPB(date)
    } yield res match {
      case Right(value) => parseResponce(value) match {
          case Right(value2) => checkSet(value2, currency) match {
            case Right(data) => Right(createSell(data.get.purchaseRateNB, data.get.purchaseRate, rate))
            case Left(e) => Left(e)
          }
          case Left(err) => Left(err)
        }
      case Left(err) => Left(err)
    }
  }

}