package com.github.dakatsuka.akka.http.oauth2.client.strategy

import akka.NotUsed
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{ FormData, HttpCharsets, HttpRequest, Uri }
import akka.stream.scaladsl.Source
import com.github.dakatsuka.akka.http.oauth2.client.{ ConfigLike, GrantType }

class RefreshTokenStrategy extends Strategy(GrantType.RefreshToken) {
  override def getAuthorizeUrl(config: ConfigLike, params: Map[String, String] = Map.empty): Option[Uri] = None

  override def getAccessTokenSource(config: ConfigLike, params: Map[String, String] = Map.empty): Source[HttpRequest, NotUsed] = {
    require(params.contains("refresh_token"))

    val uri = Uri
      .apply(config.site.toASCIIString)
      .withPath(Uri.Path(config.tokenUrl))

    val request = HttpRequest(
      method = config.tokenMethod,
      uri = uri,
      headers = List(
        RawHeader("Accept", "*/*")
      ),
      FormData(
        params ++ Map(
          "grant_type"    -> grant.value,
          "client_id"     -> config.clientId,
          "client_secret" -> config.clientSecret
        )
      ).toEntity(HttpCharsets.`UTF-8`)
    )

    Source.single(request)
  }
}
