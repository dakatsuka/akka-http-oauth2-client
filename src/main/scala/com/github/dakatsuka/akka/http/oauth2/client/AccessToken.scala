package com.github.dakatsuka.akka.http.oauth2.client

import akka.http.scaladsl.model.{ ContentTypeRange, HttpResponse }
import akka.http.scaladsl.model.MediaTypes.`application/json`
import akka.http.scaladsl.unmarshalling.{ FromEntityUnmarshaller, Unmarshal, Unmarshaller }
import akka.stream.Materializer
import akka.util.ByteString
import io.circe.{ jawn, Decoder, Json }

import scala.concurrent.Future

case class AccessToken(
    accessToken: String,
    tokenType: String,
    expiresIn: Int,
    refreshToken: Option[String]
)

object AccessToken {
  def unmarshallerContentTypes: Seq[ContentTypeRange] =
    List(`application/json`)

  implicit def jsonUnmarshaller: FromEntityUnmarshaller[Json] =
    Unmarshaller.byteStringUnmarshaller
      .forContentTypes(unmarshallerContentTypes: _*)
      .map {
        case ByteString.empty => throw Unmarshaller.NoContentException
        case data             => jawn.parseByteBuffer(data.asByteBuffer).fold(throw _, identity)
      }

  implicit def unmarshaller[A: Decoder]: FromEntityUnmarshaller[A] = {
    def decode(json: Json) = implicitly[Decoder[A]].decodeJson(json).fold(throw _, identity)
    jsonUnmarshaller.map(decode)
  }

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
