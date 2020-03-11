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
object Dependencies {

  import sbt._

  // Make sure to use the versions that are compatible with commons4s
  private val PlayVersion: String = "2.7.3"
  private val ScalacheckVersion: String = "1.14.0"
  private val ScalatestVersion: String = "3.0.8"
  private val Specs2CoreVersion: String = "4.7.0"
  private val ScalatestPlusPlayVersion: String = "4.0.3"

  val Commons4s: ModuleID = "com.chrism" %% "commons4s" % "0.0.5"

  val PlayAhcWs: ModuleID = ("com.typesafe.play" %% "play-ahc-ws" % PlayVersion)
    .exclude("org.slf4j", "slf4j-api")

  val PlayLogback: ModuleID = "com.typesafe.play" %% "play-logback" % PlayVersion

  val SwaggerPlay2: ModuleID = "io.swagger" %% "swagger-play2" % "1.7.1"
  val WebjarsPlay: ModuleID = "org.webjars" %% "webjars-play" % PlayVersion
  val SwaggerUi: ModuleID = "org.webjars" % "swagger-ui" % "3.23.8"

  val Scalacheck: ModuleID = "org.scalacheck" %% "scalacheck" % ScalacheckVersion
  val Scalatest: ModuleID = "org.scalatest" %% "scalatest" % ScalatestVersion
  val Specs2Core: ModuleID = "org.specs2" %% "specs2-core" % Specs2CoreVersion
  val ScalatestPlusPlay: ModuleID = "org.scalatestplus.play" %% "scalatestplus-play" % ScalatestPlusPlayVersion
}
