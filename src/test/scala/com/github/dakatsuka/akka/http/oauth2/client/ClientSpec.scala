package com.github.dakatsuka.akka.http.oauth2.client

import java.net.URI

import akka.actor.ActorSystem
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.ContentTypes.`application/json`
import akka.stream.scaladsl.{ Flow, Sink, Source }
import akka.stream.{ ActorMaterializer, Materializer }
import com.github.dakatsuka.akka.http.oauth2.client.Error.UnauthorizedException
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{ Millis, Seconds, Span }
import org.scalatest.{ BeforeAndAfterAll, DiagrammedAssertions, FlatSpec }

import scala.concurrent.duration.Duration
import scala.concurrent.{ Await, ExecutionContext }

class ClientSpec extends FlatSpec with DiagrammedAssertions with ScalaFutures with BeforeAndAfterAll {
  implicit val system: ActorSystem        = ActorSystem()
  implicit val ec: ExecutionContext       = system.dispatcher
  implicit val materializer: Materializer = ActorMaterializer()
  implicit val defaultPatience: PatienceConfig =
    PatienceConfig(timeout = Span(5, Seconds), interval = Span(700, Millis))

  override def afterAll(): Unit = {
    Await.ready(system.terminate(), Duration.Inf)
  }

  behavior of "Client"

  "#getAuthorizeUrl" should "delegate processing to strategy" in {
    import strategy._

    val config = Config("xxx", "yyy", site = URI.create("https://example.com"), authorizeUrl = "/oauth/custom_authorize")
    val client = Client(config)
    val result = client.getAuthorizeUrl(GrantType.AuthorizationCode, Map("redirect_uri" -> "https://example.com/callback"))
    val actual = result.get.toString
    val expect = "https://example.com/oauth/custom_authorize?redirect_uri=https://example.com/callback&response_type=code&client_id=xxx"
    assert(actual == expect)
  }

  "#getAccessToken" should "return Right[AccessToken] when oauth provider approves" in {
    import strategy._

    val response = HttpResponse(
      status = StatusCodes.OK,
      headers = Nil,
      entity = HttpEntity(
        `application/json`,
        s"""
           |{
           |  "access_token": "xxx",
           |  "token_type": "bearer",
           |  "expires_in": 86400,
           |  "refresh_token": "yyy"
           |}
         """.stripMargin
      )
    )

    val mockConnection = Flow[HttpRequest].map(_ => response)
    val config         = Config("xxx", "yyy", URI.create("https://example.com"))
    val client         = Client(config, mockConnection)
    val result         = client.getAccessToken(GrantType.AuthorizationCode, Map("code" -> "zzz", "redirect_uri" -> "https://example.com"))

    whenReady(result) { r =>
      assert(r.isRight)
    }
  }

  it should "return Left[UnauthorizedException] when oauth provider rejects" in {
    import strategy._

    val response = HttpResponse(
      status = StatusCodes.Unauthorized,
      headers = Nil,
      entity = HttpEntity(
        `application/json`,
        s"""
           |{
           |  "error": "invalid_client",
           |  "error_description": "description"
           |}
         """.stripMargin
      )
    )

    val mockConnection = Flow[HttpRequest].map(_ => response)
    val config         = Config("xxx", "yyy", URI.create("https://example.com"))
    val client         = Client(config, mockConnection)
    val result         = client.getAccessToken(GrantType.AuthorizationCode, Map("code" -> "zzz", "redirect_uri" -> "https://example.com"))

    whenReady(result) { r =>
      assert(r.isLeft)
      assert(r.left.exists(_.isInstanceOf[UnauthorizedException]))
    }
  }

  "#getConnectionWithAccessToken" should "return outgoing connection flow with access token" in {
    val accessToken = AccessToken(
      accessToken = "xxx",
      tokenType = "bearer",
      expiresIn = 86400,
      refreshToken = Some("yyy")
    )

    val request = HttpRequest(HttpMethods.GET, "/v1/foo/bar")
    val response = HttpResponse(
      status = StatusCodes.OK,
      headers = Nil,
      entity = HttpEntity(
        `application/json`,
        s"""
           |{
           |  "key": "value"
           |}
         """.stripMargin
      )
    )

    val mockConnection = Flow[HttpRequest]
      .filter { req =>
        req.headers.exists(_.is("authorization")) && req.headers.exists(_.value() == s"Bearer ${accessToken.accessToken}")
      }
      .map(_ => response)

    val config = Config("xxx", "yyy", URI.create("https://example.com"))
    val client = Client(config, mockConnection)
    val result = Source.single(request).via(client.getConnectionWithAccessToken(accessToken)).runWith(Sink.head)

    whenReady(result) { r =>
      assert(r.status.isSuccess())
    }
  }
}
