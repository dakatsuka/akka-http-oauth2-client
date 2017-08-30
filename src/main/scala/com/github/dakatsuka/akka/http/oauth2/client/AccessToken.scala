package com.github.dakatsuka.akka.http.oauth2.client

import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer
import com.github.dakatsuka.akka.http.oauth2.client.utils.JsonUnmarshaller
import io.circe.Decoder

import scala.concurrent.Future

case class AccessToken(
    accessToken: String,
    tokenType: String,
    expiresIn: Int,
    refreshToken: Option[String]
)

object AccessToken extends JsonUnmarshaller {
  implicit def decoder: Decoder[AccessToken] = Decoder.instance { c =>
    for {
      accessToken  <- c.downField("access_token").as[String].right
      tokenType    <- c.downField("token_type").as[String].right
      expiresIn    <- c.downField("expires_in").as[Int].right
      refreshToken <- c.downField("refresh_token").as[Option[String]].right
    } yield AccessToken(accessToken, tokenType, expiresIn, refreshToken)
  }

  def apply(response: HttpResponse)(implicit mat: Materializer): Future[AccessToken] = {
    Unmarshal(response).to[AccessToken]
  }
}
