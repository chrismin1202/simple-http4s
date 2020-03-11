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

import com.chrism.commons.io.CanBeEmpty

import scala.collection.mutable

private[http] trait NameValuePairsLike[NV <: NameValueLike[NV]] extends Product with Serializable with CanBeEmpty {

  def pairs: Seq[NV]

  @transient
  lazy val tuples: Seq[(String, String)] = if (isEmpty) Seq.empty else pairs.map(_.tuple)

  final def map[B](f: NV => B): Seq[B] = pairs.map(f)

  override final def isEmpty: Boolean = pairs.isEmpty

  override /* overridable */ def productElement(n: Int): Any =
    n match {
      case 0 => pairs
      case i => throw new IndexOutOfBoundsException(s"There is no element at $i!")
    }
}

private[http] trait NameValuePairsCompanionLike[NV <: NameValueLike[NV]] {

  private[http] abstract class NameValuePairsBuilder[
    NVC <: NameValueCompanionLike[NV],
    NVs <: NameValuePairsLike[NV]
  ] protected (
    nvCompanion: NVC) {

    protected final val _pairs: mutable.ListBuffer[NV] = mutable.ListBuffer.empty

    def build(): NVs

    final def add(pair: NV): this.type = {
      _pairs += pair
      this
    }

    final def add(name: String, value: String): this.type = add(nvCompanion.ofNameValue(name, value))

    final def add(pair: (String, String)): this.type = add(nvCompanion.ofTuple(pair))

    final def addAll(pairs: NVs): this.type = addAll(pairs.pairs)

    final def addAll(pairs: Iterable[NV]): this.type = {
      _pairs ++= pairs
      this
    }

    final def +=(pair: NV): this.type = add(pair)

    final def +=(kv: (String, String)): this.type = add(kv._1, kv._2)

    final def ++=(pairs: NVs): this.type = addAll(pairs)

    final def ++=(pairs: Iterable[NV]): this.type = addAll(pairs)
  }
}
