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

final class HeaderTest extends FunTestSuite {

  test("requiring 'name' in Header") {
    intercept[IllegalArgumentException] {
      Header(null, "Bearer token")
    }
    intercept[IllegalArgumentException] {
      Header("", "Bearer token")
    }
  }

  test("requiring 'value' in Header") {
    intercept[IllegalArgumentException] {
      Header("Authorization", null)
    }
    intercept[IllegalArgumentException] {
      Header("Authorization", "")
    }
  }

  test("checking equality of Header case-insensitively") {
    val h1 = Header("Access-Control-Allow-Headers", "value")
    val h2 = Header("ACCESS-CONTROL-ALLOW-HEADERS", "value")
    val h3 = Header("access-control-allow-headers", "value")
    assert(h1.equalsCaseInsensitive(h2))
    assert(h1.equalsCaseInsensitive(h3))
    assert(h2.equalsCaseInsensitive(h1))
    assert(h2.equalsCaseInsensitive(h3))
    assert(h3.equalsCaseInsensitive(h1))
    assert(h3.equalsCaseInsensitive(h2))
  }

  test("comparing Header by 'name'") {
    val h1 = Header("Access-Control-Allow-Headers", "value")
    val h2 = Header("Access-Control-Allow-Origin", "value")
    assert(h1.compare(h2) < 0)
    assert(h2.compare(h1) > 0)
  }

  test("comparing Header by 'value'") {
    val h1 = Header("Access-Control-Request-Method", "value1")
    val h2 = Header("Access-Control-Request-Method", "value2")
    assert(h1.compare(h2) < 0)
    assert(h2.compare(h1) > 0)
  }

  test("Headers: equals/hashCode") {
    val headers1 = Headers.ofTuple("name1" -> "value1", "name2" -> "value2", "name3" -> "value3")
    val headers2 = Headers.ofTuple("name2" -> "value2", "name3" -> "value3", "name1" -> "value1")
    assert(headers1 === headers2)
    assert(headers1.hashCode() === headers2.hashCode())
  }

  test("building Headers") {
    val builder = Headers.newBuilder()
    builder += "name1" -> "value1"
    builder ++= Seq(Header("name3", "value3"), Header("name5", "value5"), Header("name4", "value4"))

    val headers = builder.add("name2", "value2").build()
    headers.pairs should contain theSameElementsInOrderAs Seq(
      Header("name1", "value1"),
      Header("name2", "value2"),
      Header("name3", "value3"),
      Header("name4", "value4"),
      Header("name5", "value5"),
    )
  }

  test("building Headers multiple times using the same Builder instance") {
    val builder = Headers.newBuilder()
    builder += "name1" -> "value1"
    builder ++= Seq(Header("name3", "value3"), Header("name5", "value5"), Header("name4", "value4"))

    val headers1 = builder.build()
    headers1.pairs should contain theSameElementsInOrderAs Seq(
      Header("name1", "value1"),
      Header("name3", "value3"),
      Header("name4", "value4"),
      Header("name5", "value5"),
    )

    val headers2 = builder
      .add("name6", "value6")
      .add("name2", "value2")
      .build()
    headers2.pairs should contain theSameElementsInOrderAs Seq(
      Header("name1", "value1"),
      Header("name2", "value2"),
      Header("name3", "value3"),
      Header("name4", "value4"),
      Header("name5", "value5"),
      Header("name6", "value6"),
    )

    // make sure that building headers2 using the same Builder instance does not cause headers1 to be altered
    assert(headers1 !== headers2)
    headers1.pairs should have size 4
  }
}
