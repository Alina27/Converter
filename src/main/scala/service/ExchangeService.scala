package service

import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpMethods, HttpRequest, MediaTypes, StatusCodes}
import akka.actor.ActorSystem
import akka.http.scaladsl.model.headers.Accept
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.util.{ByteString}
import com.typesafe.config.{Config, ConfigFactory}

import scala.concurrent.Future
import akka.stream.ActorMaterializer

import java.time.format.DateTimeFormatter

import model._

object ExchangeService {

  import scala.concurrent.ExecutionContext.Implicits.global

  val conf: Config = ConfigFactory.load()

  implicit val system: ActorSystem = ActorSystem("exchange-actor", conf)
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  def dataFromPB(myDate: Option[String]): Future[Either[Errors, String]] = {

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


}