package com.github.dakatsuka.akka.http.oauth2.client.strategy

import akka.NotUsed
import akka.http.scaladsl.model.{ HttpResponse, Uri }
import akka.stream.scaladsl.Source
import com.github.dakatsuka.akka.http.oauth2.client.{ Client, GrantType }

class ImplicitStrategy extends Strategy(GrantType.Implicit) {
  override def getAuthorizeUrl(client: Client, params: Map[String, String] = Map.empty): Option[Uri] = {
    val uri = Uri
      .apply(client.config.site.toASCIIString)
      .withPath(Uri.Path(client.config.authorizeUrl))
      .withQuery(Uri.Query(params ++ Map("response_type" -> "token", "client_id" -> client.config.clientId)))

    Option(uri)
  }

  override def getAccessTokenSource(client: Client, params: Map[String, String] = Map.empty): Source[HttpResponse, NotUsed] =
    Source.empty
}
