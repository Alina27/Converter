package route

import argonaut._
import Argonaut._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server._
import service.ExchangeService

import scala.util.{Failure, Success}

trait ExchangeRoute extends Directives {
  //exchange/currency="USD"&rate=20.0&date=02.10.20

  val route: Route = path("exchange") {
    post {
      parameters('currency.as[String], 'rate.as[Double], 'date.as[String].?) { (currency, rate, date) =>
        val respPB = ExchangeService.dataFromPB(date)

         onComplete(respPB) {
           case Success(res) => res match {
             case Right(infoFromPB) => complete(infoFromPB)
             case Left(err) => complete(StatusCodes.InternalServerError, err.toString.asJson.toString)
           }
           case Failure(e) => complete(StatusCodes.InternalServerError, e.toString.asJson.toString)
         }
      }
    }
  }

}