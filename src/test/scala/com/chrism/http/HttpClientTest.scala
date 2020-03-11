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

import com.chrism.commons.FunTestSuite
import com.chrism.commons.json.json4s.Json4sFormatsLike
import com.chrism.commons.json.{JsonUtils, SnakeCasedJsonWritable}
import com.chrism.commons.util.WithResource

final class HttpClientTest extends FunTestSuite with Json4sFormatsLike {

  import HttpClientTest.Name

  test("GET example") {
    WithResource(HttpClient.secure()) { client =>
      val response = client.getBlocking(
        "https://httpbin.org/anything/get",
        queryParams = QueryParams.ofQueryParam(QueryParam("firstName", "Saul"), QueryParam("lastName", "Goodman")))
      assert(response.status === 200)

      val body = JsonUtils.fromJson(response.body)
      assert((body \ "method").extract[String] === "GET")
      assert((body \ "url").extract[String] === "https://httpbin.org/anything/get?firstName=Saul&lastName=Goodman")

      val args = body \ "args"
      assert((args \ "firstName").extract[String] === "Saul")
      assert((args \ "lastName").extract[String] === "Goodman")
    }
  }

  test("POST example") {
    WithResource(HttpClient.secure()) { client =>
      val response = client.postJsonBlocking("https://httpbin.org/anything/post", Name("Kim", "Wexler"))
      assert(response.status === 200)

      val body = JsonUtils.fromJson(response.body)
      assert((body \ "method").extract[String] === "POST")
      assert((body \ "url").extract[String] === "https://httpbin.org/anything/post")

      val json = body \ "json"
      assert((json \ "first_name").extract[String] === "Kim")
      assert((json \ "last_name").extract[String] === "Wexler")
    }
  }
}

private[this] object HttpClientTest {

  private final case class Name(firstName: String, lastName: String) extends SnakeCasedJsonWritable[Name]
}
