import akka.actor.ActorSystem
import akka.http.scaladsl.server._
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.typesafe.config.{Config, ConfigFactory}
import route._


object Boot extends App with ExchangeRoute {

  val conf: Config = ConfigFactory.load()

  implicit val system: ActorSystem = ActorSystem("exchange-admin", conf)
  implicit val m: ActorMaterializer = ActorMaterializer()
  val rt: Route = route

  Http().bindAndHandle(rt, "0.0.0.0", 8080)
}
