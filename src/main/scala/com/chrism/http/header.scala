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

import com.chrism.commons.util.{ProductUtils, StringUtils}

final case class Header(name: String, value: String) extends NameValueLike[Header] {

  /** Checks whether the given instance is equivalent to this instance.
    *
    * As opposed to {{{ equals }}},
    * this method returns {{{ true }}} if the field names are case-insensitively equal, i.e.,
    * {{{ this.name.equalsIgnoreCase(that.name) }}}.
    *
    * Note that {{{ value }}} is compared case-sensitively, i.e.,
    * even if the characters match, if their cases do not match, the two instances are not considered equal.
    *
    * @param that the instance to compare this against
    * @return {{{ true }}} if the given instance is case-insensitively equal to this instance else {{{ false }}}
    */
  def equalsCaseInsensitive(that: Header): Boolean =
    name.equalsIgnoreCase(that.name) && value == that.value

  override def toString: String = s"$name: $value"
}

object Header extends NameValueCompanionLike[Header] {

  override def ofNameValue(name: String, value: String): Header = Header(name, value)

  override def ofTuple(pair: (String, String)): Header = Header(pair._1, pair._2)
}

final class Headers private (override val pairs: Seq[Header]) extends NameValuePairsLike[Header] {

  override val productArity: Int = 1

  override def canEqual(that: Any): Boolean = that.isInstanceOf[Headers]

  override def equals(obj: Any): Boolean = ProductUtils.productEquals(this, obj)

  override def hashCode(): Int = ProductUtils.productHashCode(this)

  override def toString: String = pairs.mkString(StringUtils.lineSeparator)
}

object Headers extends NameValuePairsCompanionLike[Header] {

  val Empty: Headers = new Headers(Seq.empty)

  // TODO: consider de-duplicating

  def apply(headers: Seq[Header]): Headers =
    headers.size match {
      case 0 => Empty
      case 1 => new Headers(Seq(headers.head))
      case _ => new Headers(headers.distinct.sorted)
    }

  def ofHeader(header: Header, moreHeaders: Header*): Headers = Headers(header +: moreHeaders)

  def ofTuple(header: (String, String), moreHeaders: (String, String)*): Headers =
    Headers((header +: moreHeaders).map(Header.ofTuple))

  def newBuilder(): Builder = Builder()

  final class Builder private () extends NameValuePairsBuilder[Header.type, Headers](Header) {

    override def build(): Headers = Headers(_pairs)
  }

  private[this] object Builder {

    def apply(): Builder = new Builder()
  }
}
