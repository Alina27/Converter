package route

import argonaut._
import Argonaut._
import akka.actor.ActorSystem
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server._
import akka.stream.ActorMaterializer
import com.typesafe.config.{Config, ConfigFactory}
import service.ExchangeService

import scala.util.{Failure, Success}

trait ExchangeRoute extends Directives {
  //exchange/currency="USD"&rate=20.0&date=02.10.20
  val route: Route = pathPrefix("exchange") {
    path("sale") {
      post{
        parameters('currency.as[String], 'rate.as[Double], 'date.as[String].?) { (currency, rate, date) =>
          val respSell = ExchangeService.getForSale(currency, rate, date)

          onComplete(respSell) {
            case Success(res) => res match {
              case Right(value) => complete(value.asJson.toString)
              case Left(e) => complete(StatusCodes.InternalServerError, e.toString.asJson.toString)
            }
            case Failure(e) => complete(StatusCodes.InternalServerError, e.toString.asJson.toString)
          }
        }
      }
    }~path("buy"){
      post{
        parameters('currency.as[String], 'rate.as[Double], 'date.as[String].?) { (currency, rate, date) =>
          val respSell = ExchangeService.getForBuy(currency, rate, date)

          onComplete(respSell) {
            case Success(res) => res match {
              case Right(value) => complete(value.asJson.toString)
              case Left(e) => complete(StatusCodes.InternalServerError, e.toString.asJson.toString)
            }
            case Failure(e) => complete(StatusCodes.InternalServerError, e.toString.asJson.toString)
          }
        }
      }
    }
  }
}
