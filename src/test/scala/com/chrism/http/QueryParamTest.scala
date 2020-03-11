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

final class QueryParamTest extends FunTestSuite {

  test("requiring 'name' in QueryParam") {
    intercept[IllegalArgumentException] {
      QueryParam(null, "James McGill")
    }
    intercept[IllegalArgumentException] {
      QueryParam("", "James McGill")
    }
  }

  test("requiring 'value' in QueryParam") {
    intercept[IllegalArgumentException] {
      QueryParam("name", null)
    }
    intercept[IllegalArgumentException] {
      QueryParam("name", "")
    }
  }

  test("comparing QueryParam by 'name'") {
    val p1 = QueryParam("firstName", "Saul")
    val p2 = QueryParam("lastName", "Saul")
    assert(p1.compare(p2) < 0)
    assert(p2.compare(p1) > 0)
  }

  test("comparing QueryParam by 'value'") {
    val p1 = QueryParam("firstName", "Kim")
    val p2 = QueryParam("firstName", "Saul")
    assert(p1.compare(p2) < 0)
    assert(p2.compare(p1) > 0)
  }

  test("QueryParam: toString returns url-encoded query string") {
    val p = QueryParam("name", "Saul Goodman")
    assert(p.toString === "name=Saul+Goodman")
  }

  test("QueryParams: equals/hashCode") {
    val params1 = QueryParams.ofTuple("name1" -> "value1", "name2" -> "value2", "name3" -> "value3")
    val params2 = QueryParams.ofTuple("name1" -> "value1", "name2" -> "value2", "name3" -> "value3")
    assert(params1 === params2)
    assert(params1.hashCode() === params2.hashCode())
  }

  test("QueryParams: toString with 1 param") {
    val params = QueryParams.ofQueryParam(QueryParam("name", "Saul Goodman"))
    assert(params.toString === "name=Saul+Goodman")
  }

  test("QueryParams: toString with more than 1 param") {
    val params = QueryParams.ofQueryParam(QueryParam("name", "Saul Goodman"), QueryParam("jobTitle", "Mailroom Clerk"))
    assert(params.toString === "name=Saul+Goodman&jobTitle=Mailroom+Clerk")
  }

  test("building QueryParams") {
    val builder = QueryParams.newBuilder()
    builder += "name1" -> "value1"
    builder ++= Seq(QueryParam("name2", "value2"), QueryParam("name3", "value3"), QueryParam("name4", "value4"))

    val params = builder.add("name5", "value5").build()
    params.pairs should contain theSameElementsInOrderAs Seq(
      QueryParam("name1", "value1"),
      QueryParam("name2", "value2"),
      QueryParam("name3", "value3"),
      QueryParam("name4", "value4"),
      QueryParam("name5", "value5"),
    )
  }

  test("building QueryParams multiple times using the same Builder instance") {
    val builder = QueryParams.newBuilder()
    builder += "name1" -> "value1"
    builder ++= Seq(QueryParam("name3", "value3"), QueryParam("name4", "value4"), QueryParam("name5", "value5"))

    val params1 = builder.build()
    params1.pairs should contain theSameElementsInOrderAs Seq(
      QueryParam("name1", "value1"),
      QueryParam("name3", "value3"),
      QueryParam("name4", "value4"),
      QueryParam("name5", "value5"),
    )

    val params2 = builder
      .add("name6", "value6")
      .add("name2", "value2")
      .build()
    params2.pairs should contain theSameElementsInOrderAs Seq(
      QueryParam("name1", "value1"),
      QueryParam("name3", "value3"),
      QueryParam("name4", "value4"),
      QueryParam("name5", "value5"),
      QueryParam("name6", "value6"),
      QueryParam("name2", "value2"),
    )

    // make sure that building params2 using the same Builder instance does not cause params1 to be altered
    assert(params1 !== params2)
    params1.pairs should have size 4
  }
}
