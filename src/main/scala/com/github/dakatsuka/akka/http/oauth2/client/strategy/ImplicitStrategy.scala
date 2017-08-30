package com.github.dakatsuka.akka.http.oauth2.client.strategy

import akka.NotUsed
import akka.http.scaladsl.model.{ HttpRequest, Uri }
import akka.stream.scaladsl.Source
import com.github.dakatsuka.akka.http.oauth2.client.{ ConfigLike, GrantType }

class ImplicitStrategy extends Strategy(GrantType.Implicit) {
  override def getAuthorizeUrl(config: ConfigLike, params: Map[String, String] = Map.empty): Option[Uri] = {
    val uri = Uri
      .apply(config.site.toASCIIString)
      .withPath(Uri.Path(config.authorizeUrl))
      .withQuery(Uri.Query(params ++ Map("response_type" -> "token", "client_id" -> config.clientId)))

    Option(uri)
  }

  override def getAccessTokenSource(config: ConfigLike, params: Map[String, String] = Map.empty): Source[HttpRequest, NotUsed] =
    Source.empty
}
