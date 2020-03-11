/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.chrism.http

import java.io.IOException

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.chrism.commons.json.JsonWritable
import com.typesafe.sslconfig.ssl.SSLLooseConfig
import org.json4s.Formats
import play.api.libs.ws.ahc.{AhcWSClientConfig, AhcWSClientConfigFactory, StandaloneAhcWSClient}
import play.api.libs.ws.{BodyWritable, StandaloneWSClient, StandaloneWSResponse}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

/** A Generic HTTP client.
  *
  * Note that it is caller's responsibility to call [[close()]] to properly close the connections upon completion.
  * It is recommended to use this class with [[com.chrism.commons.util.WithResource]].
  * {{{
  *   WithResource(HttpClient.secure()) { client =>
  *     // Use the client here
  *   }
  * }}}
  *
  * Note that this implementation is not guaranteed to be thread-safe. Consider using [[ThreadLocal]].
  */
final class HttpClient private (config: AhcWSClientConfig) extends AutoCloseable {

  import HttpClient.DefaultTimeout

  private[this] var _system: ActorSystem = _
  private[this] var _client: StandaloneWSClient = _
  private[this] var _closed: Boolean = false

  /** Returns the underlying [[StandaloneWSClient]] instance, which is of type [[StandaloneAhcWSClient]].
    *
    * If you need to use the underlying client instance directly, you it at your own risk.
    *
    * @return the underlying [[StandaloneWSClient]] instance
    */
  def client(): StandaloneWSClient = {
    checkClosed()

    if (_system == null) {
      _system = ActorSystem()
    }
    if (_client == null) {
      _client = StandaloneAhcWSClient(config = config)(ActorMaterializer()(_system))
    }
    _client
  }

  // GET

  def getAsync(
    url: String,
    queryParams: QueryParams = QueryParams.Empty,
    headers: Headers = Headers.Empty
  ): Future[StandaloneWSResponse] =
    client().url(url).withQueryStringParameters(queryParams.tuples: _*).withHttpHeaders(headers.tuples: _*).get()

  def getBlocking(
    url: String,
    queryParams: QueryParams = QueryParams.Empty,
    headers: Headers = Headers.Empty,
    timeout: Duration = DefaultTimeout
  ): StandaloneWSResponse =
    Await.result(getAsync(url, queryParams = queryParams, headers = headers), timeout)

  // POST

  def postAsync[B: BodyWritable](
    url: String,
    body: B,
    queryParams: QueryParams = QueryParams.Empty,
    headers: Headers = Headers.Empty
  ): Future[StandaloneWSResponse] =
    client().url(url).withQueryStringParameters(queryParams.tuples: _*).withHttpHeaders(headers.tuples: _*).post(body)

  def postBlocking[B: BodyWritable](
    url: String,
    body: B,
    queryParams: QueryParams = QueryParams.Empty,
    headers: Headers = Headers.Empty,
    timeout: Duration = DefaultTimeout
  ): StandaloneWSResponse =
    Await.result(postAsync(url, body, queryParams = queryParams, headers = headers), timeout)

  def postJsonAsync[B <: JsonWritable[B]](
    url: String,
    body: B,
    queryParams: QueryParams = QueryParams.Empty,
    headers: Headers = Headers.Empty
  )(
    implicit
    formats: Formats,
    m: Manifest[B]
  ): Future[StandaloneWSResponse] =
    postAsync(url, body.toJson, queryParams = queryParams, headers = headers)

  def postJsonBlocking[B <: JsonWritable[B]](
    url: String,
    body: B,
    queryParams: QueryParams = QueryParams.Empty,
    headers: Headers = Headers.Empty,
    timeout: Duration = DefaultTimeout
  )(
    implicit
    formats: Formats,
    m: Manifest[B]
  ): StandaloneWSResponse =
    Await.result(postJsonAsync(url, body, queryParams = queryParams, headers = headers), timeout)

  // TODO: add support for multipart/form-data

  // PUT

  def putAsync[B: BodyWritable](
    url: String,
    body: B,
    queryParams: QueryParams = QueryParams.Empty,
    headers: Headers = Headers.Empty
  ): Future[StandaloneWSResponse] =
    client().url(url).withQueryStringParameters(queryParams.tuples: _*).withHttpHeaders(headers.tuples: _*).put(body)

  def putBlocking[B: BodyWritable](
    url: String,
    body: B,
    queryParams: QueryParams = QueryParams.Empty,
    headers: Headers = Headers.Empty,
    timeout: Duration = DefaultTimeout
  ): StandaloneWSResponse =
    Await.result(putAsync(url, body, queryParams = queryParams, headers = headers), timeout)

  def putJsonAsync[B <: JsonWritable[B]](
    url: String,
    body: B,
    queryParams: QueryParams = QueryParams.Empty,
    headers: Headers = Headers.Empty
  )(
    implicit
    formats: Formats,
    m: Manifest[B]
  ): Future[StandaloneWSResponse] =
    putAsync(url, body.toJson, queryParams = queryParams, headers = headers)

  def putJsonBlocking[B <: JsonWritable[B]](
    url: String,
    body: B,
    queryParams: QueryParams = QueryParams.Empty,
    headers: Headers = Headers.Empty,
    timeout: Duration = DefaultTimeout
  )(
    implicit
    formats: Formats,
    m: Manifest[B]
  ): StandaloneWSResponse =
    Await.result(putJsonAsync(url, body, queryParams = queryParams, headers = headers), timeout)

  // TODO: implement DELETE and PATCH

  /** Closes the client and terminates [[ActorSystem]]. */
  override def close(): Unit = {
    checkClosed()

    if (_client != null) {
      _client.close()
      _client = null
    }

    if (_system != null) {
      _system.terminate()
      _system = null
    }

    _closed = true
  }

  private[this] def checkClosed(): Unit =
    if (_closed) {
      throw new IOException("The client has already been closed!")
    }
}

object HttpClient {

  import scala.concurrent.duration._

  private val DefaultTimeout: Duration = 10.seconds

  private def apply(config: AhcWSClientConfig): HttpClient = new HttpClient(config)

  /** Returns a new instance with secure SSL configuration.
    *
    * @return a secure instance
    */
  def secure(): HttpClient = Secure.newClient()

  /** Returns a new instance without secure SSL configuration, i.e., accepting any certificate.
    *
    * This factory is intended mainly for testing purpose, not recommended for production use.
    *
    * @return an insecure instance
    */
  def insecure(): HttpClient = Insecure.newClient()

  private[this] sealed trait ClientType {

    def newClient(): HttpClient

    protected def newConfig(): AhcWSClientConfig = AhcWSClientConfigFactory.forConfig()
  }

  private[this] case object Secure extends ClientType {

    override def newClient(): HttpClient = HttpClient(newConfig())
  }

  private[this] case object Insecure extends ClientType {

    override def newClient(): HttpClient = {
      val conf = newConfig()
      HttpClient(
        conf
          .copy(wsClientConfig = conf.wsClientConfig
            .copy(ssl = conf.wsClientConfig.ssl.withLoose(SSLLooseConfig().withAcceptAnyCertificate(true)))))
    }
  }
}
