package com.github.dakatsuka.akka.http.oauth2.client

import akka.http.scaladsl.model.Uri
import akka.stream.Materializer
import com.github.dakatsuka.akka.http.oauth2.client.strategy.Strategy

import scala.concurrent.{ ExecutionContext, Future }

trait ClientLike {
  def getAuthorizeUrl[A <: GrantType](grant: A, params: Map[String, String] = Map.empty)(implicit s: Strategy[A]): Option[Uri]

  def getAccessToken[A <: GrantType](
      grant: A,
      params: Map[String, String] = Map.empty
  )(implicit s: Strategy[A], ec: ExecutionContext, mat: Materializer): Future[Either[Throwable, AccessToken]]
}
