/*
 * Copyright 2019 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package util

import controllers.Default
import play.api.http.HeaderNames._
import play.api.test.FakeRequest
import play.mvc.Http.MimeTypes._
import uk.gov.hmrc.customs.dit.licence.model._
import util.CustomsDitLiteExternalServicesConfig.{DitLiteEntryUsageServiceContext, DitLiteLateUsageServiceContext}

import scala.xml.NodeSeq

object PublicNotificationTestData {


  val ValidXML1: NodeSeq = <some1>xml1</some1>
  val ValidXML2: NodeSeq = <some2>xml2</some2>

  lazy val ValidEntryUsageRequest = FakeRequest()
    .withHeaders(RequestHeaders.ValidHeaders.toSeq: _*)
  lazy val ValidLateUsageRequest = FakeRequest()
    .withHeaders(RequestHeaders.ValidHeaders.toSeq: _*)

  val RequestHeader1 = PublicNotificationRequestHeader("header1", "value1")
  val RequestHeader2 = PublicNotificationRequestHeader("header2", "value2")
  val twoRequestHeaders = Seq(
    RequestHeader1,
    RequestHeader2
  )
  val oneRequestHeader = Seq(
    RequestHeader1
  )
  val publicNotificationEntryUsageRequest = PublicNotificationRequest(DitLiteEntryUsageServiceContext, twoRequestHeaders, "<request>FOO</request>")
  val publicNotificationEntryUsageRequestAsJson =
    """{
      |  "url" : "/ditLiteService/entry-usage",
      |  "headers" : [ {
      |    "name" : "header1",
      |    "value" : "value1"
      |  }, {
      |    "name" : "header2",
      |    "value" : "value2"
      |  } ],
      |  "xmlPayload" : "<request>FOO</request>"
      |}""".stripMargin

  val publicNotificationLateUsageRequest = PublicNotificationRequest(DitLiteLateUsageServiceContext, oneRequestHeader, "<request>FOO</request>")
  val publicNotificationLateUsageRequestAsJson =
    """{
      |  "url" : "/ditLiteService/late-usage",
      |  "headers" : [ {
      |    "name" : "header1",
      |    "value" : "value1"
      |  } ],
      |  "xmlPayload" : "<request>FOO</request>"
      |}""".stripMargin

  val ExpectedPublicNotificationRequestHeaderSet = Set(
    PublicNotificationRequestHeader(ACCEPT, XML),
    PublicNotificationRequestHeader(CONTENT_TYPE, s"$XML; charset=UTF-8"),
    PublicNotificationRequestHeader("X-Correlation-ID", TestData.CorrelationId),
    PublicNotificationRequestHeader(AUTHORIZATION, "Basic " + ExternalServicesConfig.AuthToken)
  )


  val responseHeaders = Seq(
    PublicNotificationResponseHeader("response1", "response1Value"),
    PublicNotificationResponseHeader("response2", "response2Value")
  )

  val publicNotificationResponse = PublicNotificationResponse(new Default().OK, responseHeaders, "<response>BAR</response>")
  val publicNotificationResponseAsJson =
    """{
      |  "status" : 200,
      |  "headers" : [ {
      |    "name" : "response1",
      |    "value" : "response1Value"
      |  }, {
      |    "name" : "response2",
      |    "value" : "response2Value"
      |  } ],
      |  "xmlPayload" : "<response>BAR</response>"
      |}""".stripMargin
}
