package com.github.dakatsuka.akka.http.oauth2.client.strategy

import akka.NotUsed
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model._
import akka.stream.scaladsl.Source
import com.github.dakatsuka.akka.http.oauth2.client.{ Client, GrantType }

class PasswordCredentialsStrategy extends Strategy(GrantType.PasswordCredentials) {
  override def getAuthorizeUrl(client: Client, params: Map[String, String] = Map.empty): Option[Uri] = None

  override def getAccessTokenSource(client: Client, params: Map[String, String] = Map.empty): Source[HttpResponse, NotUsed] = {
    require(params.contains("username"))
    require(params.contains("password"))

    val uri = Uri
      .apply(client.config.site.toASCIIString)
      .withPath(Uri.Path(client.config.tokenUrl))

    val request = HttpRequest(
      method = client.config.tokenMethod,
      uri = uri,
      headers = List(
        RawHeader("Accept", "*/*")
      ),
      FormData(
        params ++ Map(
          "grant_type"    -> grant.value,
          "client_id"     -> client.config.clientId,
          "client_secret" -> client.config.clientSecret
        )
      ).toEntity(HttpCharsets.`UTF-8`)
    )

    Source.single(request).via(client.connection)
  }
}
