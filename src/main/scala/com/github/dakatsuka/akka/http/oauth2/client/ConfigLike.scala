package com.github.dakatsuka.akka.http.oauth2.client

import java.net.URI

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ HttpMethod, HttpRequest, HttpResponse }
import akka.stream.scaladsl.Flow

import scala.concurrent.Future

trait ConfigLike {
  val clientId: String
  val clientSecret: String
  val site: URI
  val authorizeUrl: String
  val tokenUrl: String
  val tokenMethod: HttpMethod

  def connection(implicit system: ActorSystem): Flow[HttpRequest, HttpResponse, Future[Http.OutgoingConnection]]
}
