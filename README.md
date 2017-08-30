# akka-http-oauth2-client

A Scala wrapper for OAuth 2.0 with Akka HTTP.

## Getting akka-http-useragent-support

akka-http-oauth2-client is available in sonatype repository and it targets Akka HTTP 10.0.x. There are scala 2.11 and 2.12 compatible jars available.

```

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

val client = new Client(config)

// Some(https://api.example.com/oauth/authorize?redirect_uri=https://example.com/oauth2/callback&response_type=code&client_id=xxxxxxxxx)
val authorizeUrl: Option[String] =
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
```

## Authors

* Dai Akatsuka <d.akatsuka@gmail.com>
