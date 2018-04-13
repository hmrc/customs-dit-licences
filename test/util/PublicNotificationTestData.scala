/*
 * Copyright 2018 HM Revenue & Customs
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
import uk.gov.hmrc.customs.dit.licence.model._

object PublicNotificationTestData {

  val requestHeaders = Seq(
    PublicNotificationRequestHeader("request1", "request1Value"),
    PublicNotificationRequestHeader("request2", "request2Value")
  )
  val publicNotificationRequest = PublicNotificationRequest("/fooBar", requestHeaders, "<request>FOO</request>")
  val publicNotificationRequestAsJson =
    """{
      |  "url" : "/fooBar",
      |  "headers" : [ {
      |    "name" : "request1",
      |    "value" : "request1Value"
      |  }, {
      |    "name" : "request2",
      |    "value" : "request2Value"
      |  } ],
      |  "xmlPayload" : "<request>FOO</request>"
      |}""".stripMargin

  val responseHeaders = Seq(
    PublicNotificationResponseHeader("response1", "response1Value"),
    PublicNotificationResponseHeader("response2", "response2Value")
  )

  val publicNotificationResponse = PublicNotificationResponse(Default.OK, TestData.correlationId, responseHeaders, "<response>BAR</response>")
  val publicNotificationResponseAsJson =
    """{
      |  "status" : 200,
      |  "correlationId" : "e61f8eee-812c-4b8f-b193-06aedc60dca2",
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
