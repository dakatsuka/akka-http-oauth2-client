package com.github.dakatsuka.akka.http.oauth2.client

package object strategy {
  implicit val authorizationCodeStrategy: AuthorizationCodeStrategy     = new AuthorizationCodeStrategy
  implicit val clientCredentialsStrategy: ClientCredentialsStrategy     = new ClientCredentialsStrategy
  implicit val implicitStrategy: ImplicitStrategy                       = new ImplicitStrategy
  implicit val passwordCredentialsStrategy: PasswordCredentialsStrategy = new PasswordCredentialsStrategy
  implicit val refreshTokenStrategy: RefreshTokenStrategy               = new RefreshTokenStrategy
}
