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

import com.chrism.commons.util.StringUtils

/** This trait is intended for immutable classes, i.e.,
  * if {{{ def name: String }}} and {{{ def value: String }}} need to be overwritten as {{{ def }}} and
  * they return different value each time they are invoked, this trait should not be used.
  *
  * @tparam NV the type that extends this trait
  */
private[http] trait NameValueLike[NV <: NameValueLike[NV]] extends Ordered[NV] {

  /** Returns the name.
    *
    * This should be overridden as {{{ val }}} or {{{ lazy val }}}.
    *
    * @return the name
    */
  def name: String

  /** Returns the value.
    *
    * This should be overridden as {{{ val }}} or {{{ lazy val }}}.
    *
    * @return the value
    */
  def value: String

  require(StringUtils.isNotBlank(name), "The name is required!")
  require(StringUtils.isNotBlank(value), "The value is required!")

  @transient
  lazy val tuple: (String, String) = (name, value)

  /** Compares the given instance with this instance and returns
    *   - a negative number if this instance takes precedence
    *   - a positive number if the given instance takes precedence
    *   - zero if the two instances are equal, i.e., {{{ this == that }}}
    *
    * First, {{{ name }}} is compared in lexicographic order and then
    * {{{ value }}} is compared in lexicographic order if {{{ name }}} is same for both instances.
    *
    * Override if the comparison logic needs to be customized.
    *
    * @param that the instance to compare
    * @return a negative number if this instance has the precedence over the given instance,
    *         a positive number if the given instance has the precedence,
    *         or zero if the two instances are equal
    */
  override /* overridable */ def compare(that: NV): Int =
    compareByName(that) match {
      case 0       => compareByValue(that)
      case nonZero => nonZero
    }

  protected /* overridable */ def compareByName(that: NV): Int = name.compareTo(that.name)

  protected /* overridable */ def compareByValue(that: NV): Int = value.compareTo(that.value)
}

private[http] trait NameValueCompanionLike[NV <: NameValueLike[NV]] {

  def ofNameValue(name: String, value: String): NV

  def ofTuple(pair: (String, String)): NV
}
