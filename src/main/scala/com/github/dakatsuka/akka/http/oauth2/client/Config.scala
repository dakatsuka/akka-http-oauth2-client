package com.github.dakatsuka.akka.http.oauth2.client

import java.net.URI

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ HttpMethod, HttpMethods, HttpRequest, HttpResponse }
import akka.stream.scaladsl.Flow

import scala.concurrent.Future
import scala.util.Try

case class Config(
    clientId: String,
    clientSecret: String,
    site: URI,
    authorizeUrl: String = "/oauth/authorize",
    tokenUrl: String = "/oauth/token",
    tokenMethod: HttpMethod = HttpMethods.POST
) extends ConfigLike {

  def connection(implicit system: ActorSystem): Flow[HttpRequest, HttpResponse, Future[Http.OutgoingConnection]] =
    site.getScheme match {
      case "http"  => Http().outgoingConnection(site.getHost, Try(site.getPort).getOrElse(80))
      case "https" => Http().outgoingConnectionHttps(site.getHost, Try(site.getPort).getOrElse(443))
    }
}
