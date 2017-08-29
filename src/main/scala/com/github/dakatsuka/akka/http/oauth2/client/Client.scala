package com.github.dakatsuka.akka.http.oauth2.client

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ HttpRequest, HttpResponse, Uri }
import akka.stream.Materializer
import akka.stream.scaladsl.{ Flow, Sink }
import com.github.dakatsuka.akka.http.oauth2.client.strategy.Strategy

import scala.concurrent.{ ExecutionContext, Future }

class Client(config: Config)(implicit system: ActorSystem) {
  import Client._

  def getAuthorizeUrl[A <: GrantType](grant: A, params: Map[String, String] = Map.empty)(implicit s: Strategy[A]): Option[Uri] =
    s.getAuthorizeUrl(config, params)

  def getAccessToken[A <: GrantType](
      grant: A,
      params: Map[String, String] = Map.empty
  )(implicit s: Strategy[A], ec: ExecutionContext, mat: Materializer): Future[Either[Throwable, AccessToken]] = {
    val source = s.getAccessTokenSource(config, params)

    source
      .via(connection)
      .map { response =>
        if (response.status.isFailure()) throw new UnauthorizedException(response)
        response
      }
      .mapAsync(1)(AccessToken.apply)
      .runWith(Sink.head)
      .map(Right.apply)
      .recover {
        case ex => Left(ex)
      }
  }

  val connection: Flow[HttpRequest, HttpResponse, Future[Http.OutgoingConnection]] = config.site.getScheme match {
    case "http"  => Http().outgoingConnection(config.site.getHost, config.site.getPort)
    case "https" => Http().outgoingConnectionHttps(config.site.getHost, config.site.getPort)
  }
}

object Client {
  class UnauthorizedException(response: HttpResponse) extends RuntimeException(response.toString)
}
