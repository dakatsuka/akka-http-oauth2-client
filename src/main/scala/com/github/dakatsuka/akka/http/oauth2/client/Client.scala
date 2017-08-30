package com.github.dakatsuka.akka.http.oauth2.client

import akka.actor.ActorSystem
import akka.http.scaladsl.model.{ HttpResponse, Uri }
import akka.stream.Materializer
import akka.stream.scaladsl.Sink
import com.github.dakatsuka.akka.http.oauth2.client.Error.UnauthorizedException
import com.github.dakatsuka.akka.http.oauth2.client.strategy.Strategy

import scala.concurrent.{ ExecutionContext, Future }

class Client(config: ConfigLike)(implicit system: ActorSystem) extends ClientLike {
  def getAuthorizeUrl[A <: GrantType](grant: A, params: Map[String, String] = Map.empty)(implicit s: Strategy[A]): Option[Uri] =
    s.getAuthorizeUrl(config, params)

  def getAccessToken[A <: GrantType](
      grant: A,
      params: Map[String, String] = Map.empty
  )(implicit s: Strategy[A], ec: ExecutionContext, mat: Materializer): Future[Either[Throwable, AccessToken]] = {
    val source = s.getAccessTokenSource(config, params)

    source
      .via(config.connection)
      .mapAsync(1)(handleError)
      .mapAsync(1)(AccessToken.apply)
      .runWith(Sink.head)
      .map(Right.apply)
      .recover {
        case ex => Left(ex)
      }
  }

  private def handleError(response: HttpResponse)(implicit ec: ExecutionContext, mat: Materializer): Future[HttpResponse] = {
    if (response.status.isFailure()) UnauthorizedException.fromHttpResponse(response).flatMap(Future.failed(_))
    else Future.successful(response)
  }
}
