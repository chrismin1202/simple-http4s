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

import java.net.URLEncoder

import com.chrism.commons.util.ProductUtils

final case class QueryParam(name: String, value: String) extends NameValueLike[QueryParam] {

  @transient
  lazy val urlEncodedName: String = QueryParam.urlEncode(name)

  @transient
  lazy val urlEncodedValue: String = QueryParam.urlEncode(value)

  override def toString: String = s"$urlEncodedName=$urlEncodedValue"
}

object QueryParam extends NameValueCompanionLike[QueryParam] {

  override def ofNameValue(name: String, value: String): QueryParam = QueryParam(name, value)

  override def ofTuple(pair: (String, String)): QueryParam = QueryParam(pair._1, pair._2)

  private def urlEncode(s: String): String = URLEncoder.encode(s, "UTF-8")
}

final class QueryParams private (override val pairs: Seq[QueryParam]) extends NameValuePairsLike[QueryParam] {

  override val productArity: Int = 1

  override def canEqual(that: Any): Boolean = that.isInstanceOf[QueryParams]

  override def equals(obj: Any): Boolean = ProductUtils.productEquals(this, obj)

  override def hashCode(): Int = ProductUtils.productHashCode(this)

  override def toString: String = pairs.mkString("&")
}

object QueryParams extends NameValuePairsCompanionLike[QueryParam] {

  val Empty: QueryParams = new QueryParams(Seq.empty)

  def apply(pairs: Seq[QueryParam]): QueryParams =
    pairs.size match {
      case 0 => Empty
      case _ => new QueryParams(pairs)
    }

  def ofQueryParam(param: QueryParam, moreParams: QueryParam*): QueryParams = QueryParams(param +: moreParams)

  def ofTuple(param: (String, String), moreParams: (String, String)*): QueryParams =
    QueryParams((param +: moreParams).map(QueryParam.ofTuple))

  def newBuilder(): Builder = Builder()

  final class Builder private () extends NameValuePairsBuilder[QueryParam.type, QueryParams](QueryParam) {

    /** Builds [[QueryParams]] with the [[QueryParam]] instances that have been added.
      *
      * Note that the builder uses a mutable collection; therefore,
      * the [[QueryParam]] instances are copied to a new sequence allow this builder instance to be reusable.
      *
      * @return the [[QueryParams]] instance
      */
    override def build(): QueryParams = QueryParams(Seq(_pairs: _*)) // copying the sequence as the builder is reusable.
  }

  private[this] object Builder {

    def apply(): Builder = new Builder()
  }
}
