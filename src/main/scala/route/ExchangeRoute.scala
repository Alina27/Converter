package route

import argonaut._
import Argonaut._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server._
import service.ExchangeService

import scala.util.{Failure, Success}

trait ExchangeRoute extends Directives {
  val route: Route = pathPrefix("exchange") {
    path("sell") {
      get{
        parameters('currency.as[String], 'rate.as[Double], 'date.as[String].?) { (currency, rate, date) =>
          val respSell = ExchangeService.calculate("sell", currency, rate, date)

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
      get{
        parameters('currency.as[String], 'rate.as[Double], 'date.as[String].?) { (currency, rate, date) =>
          val respSell = ExchangeService.calculate("buy", currency, rate, date)

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
