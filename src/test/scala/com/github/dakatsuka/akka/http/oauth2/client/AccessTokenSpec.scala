package com.github.dakatsuka.akka.http.oauth2.client

import akka.actor.ActorSystem
import akka.http.scaladsl.model.{ HttpEntity, HttpResponse, StatusCodes }
import akka.http.scaladsl.model.ContentTypes.`application/json`
import akka.stream.{ ActorMaterializer, Materializer }
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{ Millis, Seconds, Span }
import org.scalatest.{ BeforeAndAfterAll, DiagrammedAssertions, FlatSpec }

import scala.concurrent.{ Await, ExecutionContext }
import scala.concurrent.duration.Duration

class AccessTokenSpec extends FlatSpec with DiagrammedAssertions with ScalaFutures with BeforeAndAfterAll {
  implicit val system: ActorSystem        = ActorSystem()
  implicit val ec: ExecutionContext       = system.dispatcher
  implicit val materializer: Materializer = ActorMaterializer()
  implicit val defaultPatience: PatienceConfig =
    PatienceConfig(timeout = Span(5, Seconds), interval = Span(700, Millis))

  override def afterAll(): Unit = {
    Await.ready(system.terminate(), Duration.Inf)
  }

  behavior of "AccessToken"

  it should "apply from HttpResponse" in {
    val accessToken  = "xxx"
    val tokenType    = Some("bearer")
    val expiresIn    = Some(86400)
    val refreshToken = Some("yyy")
    val scope        = None

    val httpResponse = HttpResponse(
      status = StatusCodes.OK,
      headers = Nil,
      entity = HttpEntity(
        `application/json`,
        s"""
           |{
           |  "access_token": "$accessToken",
           |  "token_type": "bearer",
           |  "expires_in": 86400,
           |  "refresh_token": "yyy"
           |}
         """.stripMargin
      )
    )

    val result = AccessToken(httpResponse)

    whenReady(result) { token =>
      assert(token.accessToken == accessToken)
      assert(token.tokenType == tokenType)
      assert(token.scope == scope)
      assert(token.expiresIn == expiresIn)
      assert(token.refreshToken == refreshToken)
    }
  }
}
