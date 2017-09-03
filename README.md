# akka-http-oauth2-client
[![Build Status](https://travis-ci.org/dakatsuka/akka-http-oauth2-client.svg?branch=master)](https://travis-ci.org/dakatsuka/akka-http-oauth2-client) [![Maven Central](https://img.shields.io/maven-central/v/com.github.dakatsuka/akka-http-oauth2-client_2.12.svg)](https://search.maven.org/#search%7Cga%7C1%7Ca%3A%22akka-http-oauth2-client_2.12%22)

A Scala wrapper for OAuth 2.0 with Akka HTTP.

## Getting akka-http-oauth2-client

akka-http-oauth2-client is available in sonatype repository and it targets Akka HTTP 10.0.x. There are scala 2.11 and 2.12 compatible jars available.

```sbt
libraryDependencies += "com.github.dakatsuka" %% "akka-http-oauth2-client" % "0.1.0"
```

## Usage

```scala
import java.net.URI

import akka.actor.ActorSystem
import akka.http.scaladsl.model.Uri
import akka.stream.{ ActorMaterializer, Materializer }
import com.github.dakatsuka.akka.http.oauth2.client.{ Client, Config }
import com.github.dakatsuka.akka.http.oauth2.client.Error.UnauthorizedException
import com.github.dakatsuka.akka.http.oauth2.client.strategy._

import scala.concurrent.{ ExecutionContext, Future }

implicit val system: ActorSystem  = ActorSystem()
implicit val ec: ExecutionContext = system.dispatcher
implicit val mat: Materializer    = ActorMaterializer()

val config = Config(
  clientId     = "xxxxxxxxx",
  clientSecret = "xxxxxxxxx",
  site         = URI.create("https://api.example.com")
)

val client = Client(config)

// Some(https://api.example.com/oauth/authorize?redirect_uri=https://example.com/oauth2/callback&response_type=code&client_id=xxxxxxxxx)
val authorizeUrl: Option[Uri] =
  client.getAuthorizeUrl(GrantType.AuthorizationCode, Map("redirect_uri" -> "https://example.com/oauth2/callback"))

val accessToken: Future[Either[Throwable, AccessToken]] =
  client.getAccessToken(GrantType.AuthorizationCode, Map("code" -> "yyyyyy", "redirect_uri" -> "https://example.com"))

accessToken.foreach {
  case Right(t) =>
    t.accessToken  // String
    t.tokenType    // String
    t.expiresIn    // Int
    t.refreshToken // Option[String]
  case Left(ex: UnauthorizedException) =>
    ex.code        // Code
    ex.description // String
    ex.response    // HttpResponse
}

val newAccessToken: Future[Either[Throwable, AccessToken]] =
  client.getAccessToken(GrantType.RefreshToken, Map("refresh_token" -> "zzzzzzzz"))
```

## Testing

`Client` can pass mock connection into constructor.

```scala
val mock = Flow[HttpRequest].map { _ =>
  HttpResponse(
    status = StatusCodes.OK,
    headers = Nil,
    entity = HttpEntity(
      `application/json`,
      s"""
         |{
         |  "access_token": "dummy",
         |  "token_type": "bearer",
         |  "expires_in": 86400,
         |  "refresh_token": "dummy"
         |}
      """.stripMargin
    )
  )
}

val client = Client(config, mock)
```

## Authors

* Dai Akatsuka <d.akatsuka@gmail.com>
