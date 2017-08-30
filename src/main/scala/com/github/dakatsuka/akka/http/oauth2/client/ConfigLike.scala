package com.github.dakatsuka.akka.http.oauth2.client

import java.net.URI

import akka.actor.ActorSystem
import akka.http.scaladsl.model.{ HttpMethod, HttpRequest, HttpResponse }
import akka.stream.scaladsl.Flow

trait ConfigLike {
  def clientId: String
  def clientSecret: String
  def site: URI
  def authorizeUrl: String
  def tokenUrl: String
  def tokenMethod: HttpMethod

  def connection(implicit system: ActorSystem): Flow[HttpRequest, HttpResponse, _]
}
