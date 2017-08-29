package com.github.dakatsuka.akka.http.oauth2.client.strategy

import akka.NotUsed
import akka.http.scaladsl.model.{ HttpResponse, Uri }
import akka.stream.scaladsl.Source
import com.github.dakatsuka.akka.http.oauth2.client.{ Client, GrantType }

abstract class Strategy[A <: GrantType](val grant: A) {
  def getAuthorizeUrl(client: Client, params: Map[String, String]): Option[Uri]
  def getAccessTokenSource(client: Client, params: Map[String, String]): Source[HttpResponse, NotUsed]
}
