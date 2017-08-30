package com.github.dakatsuka.akka.http.oauth2.client.strategy

import akka.NotUsed
import akka.http.scaladsl.model.{ HttpRequest, Uri }
import akka.stream.scaladsl.Source
import com.github.dakatsuka.akka.http.oauth2.client.{ ConfigLike, GrantType }

abstract class Strategy[A <: GrantType](val grant: A) {
  def getAuthorizeUrl(config: ConfigLike, params: Map[String, String]): Option[Uri]
  def getAccessTokenSource(config: ConfigLike, params: Map[String, String]): Source[HttpRequest, NotUsed]
}
