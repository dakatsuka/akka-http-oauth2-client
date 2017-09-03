package com.github.dakatsuka.akka.http.oauth2.client

import java.net.URI

import akka.http.scaladsl.model.HttpMethod

trait ConfigLike {
  def clientId: String
  def clientSecret: String
  def site: URI
  def authorizeUrl: String
  def tokenUrl: String
  def tokenMethod: HttpMethod
  def getHost: String
  def getPort: Int
}
