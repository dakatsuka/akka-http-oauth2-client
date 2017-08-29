package com.github.dakatsuka.akka.http.oauth2.client

import java.net.URI

import akka.http.scaladsl.model.{ HttpMethod, HttpMethods }

case class Config(
    clientId: String,
    clientSecret: String,
    site: URI,
    authorizeUrl: String = "/oauth/authorize",
    tokenUrl: String = "/oauth/token",
    tokenMethod: HttpMethod = HttpMethods.POST
)
