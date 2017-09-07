package com.github.dakatsuka.akka.http.oauth2.client

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.headers.OAuth2BearerToken
import akka.http.scaladsl.model.{ HttpRequest, HttpResponse, Uri }
import akka.stream.Materializer
import akka.stream.scaladsl.{ Flow, Sink }
import com.github.dakatsuka.akka.http.oauth2.client.Error.UnauthorizedException
import com.github.dakatsuka.akka.http.oauth2.client.strategy.Strategy

import scala.concurrent.{ ExecutionContext, Future }

class Client(config: ConfigLike, connection: Option[Flow[HttpRequest, HttpResponse, _]] = None)(implicit system: ActorSystem)
    extends ClientLike {
  def getAuthorizeUrl[A <: GrantType](grant: A, params: Map[String, String] = Map.empty)(implicit s: Strategy[A]): Option[Uri] =
    s.getAuthorizeUrl(config, params)

  def getAccessToken[A <: GrantType](
      grant: A,
      params: Map[String, String] = Map.empty
  )(implicit s: Strategy[A], ec: ExecutionContext, mat: Materializer): Future[Either[Throwable, AccessToken]] = {
    val source = s.getAccessTokenSource(config, params)

    source
      .via(connection.getOrElse(defaultConnection))
      .mapAsync(1)(handleError)
      .mapAsync(1)(AccessToken.apply)
      .runWith(Sink.head)
      .map(Right.apply)
      .recover {
        case ex => Left(ex)
      }
  }

  def getConnectionWithAccessToken(accessToken: AccessToken): Flow[HttpRequest, HttpResponse, _] =
    Flow[HttpRequest]
      .map(_.addCredentials(OAuth2BearerToken(accessToken.accessToken)))
      .via(connection.getOrElse(defaultConnection))

  private def defaultConnection: Flow[HttpRequest, HttpResponse, _] =
    config.site.getScheme match {
      case "http"  => Http().outgoingConnection(config.getHost, config.getPort)
      case "https" => Http().outgoingConnectionHttps(config.getHost, config.getPort)
    }

  private def handleError(response: HttpResponse)(implicit ec: ExecutionContext, mat: Materializer): Future[HttpResponse] = {
    if (response.status.isFailure()) UnauthorizedException.fromHttpResponse(response).flatMap(Future.failed(_))
    else Future.successful(response)
  }
}

object Client {
  def apply(config: ConfigLike)(implicit system: ActorSystem): Client =
    new Client(config)

  def apply(config: ConfigLike, connection: Flow[HttpRequest, HttpResponse, _])(implicit system: ActorSystem): Client =
    new Client(config, Some(connection))
}
