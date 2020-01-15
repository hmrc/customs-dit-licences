/*
 * Copyright 2020 HM Revenue & Customs
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

package util.externalservices

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.verification.LoggedRequest
import play.api.http.{HeaderNames, MimeTypes}
import play.api.libs.json.{JsValue, Json}
import play.api.test.Helpers._
import uk.gov.hmrc.customs.dit.licence.model.PublicNotificationRequest
import uk.gov.hmrc.customs.dit.licence.model.PublicNotificationResponse._
import util.PublicNotificationTestData.publicNotificationResponse
import util.{CustomsDitLiteExternalServicesConfig, PublicNotificationTestData, WireMockRunner}

trait PublicNotificationService extends WireMockRunner {
  private val urlMatchingRequestPath = urlMatching(CustomsDitLiteExternalServicesConfig.PublicNotificationServiceContext)

  def setupPublicNotificationServiceToReturn(status: Int = OK, expectedResponse: JsValue = Json.toJson(publicNotificationResponse) ): Unit =
    stubFor(post(urlMatchingRequestPath)
      .withHeader(HeaderNames.ACCEPT, equalTo(MimeTypes.JSON))
      .withHeader(HeaderNames.CONTENT_TYPE, equalTo(MimeTypes.JSON))
      willReturn aResponse()
      .withBody(PublicNotificationTestData.publicNotificationResponseAsJson)
      .withStatus(status))

  def verifyPublicNotificationServiceWasCalledWith(publicNotificationRequest: PublicNotificationRequest) {
    verify(1, postRequestedFor(urlMatchingRequestPath)
      .withHeader(HeaderNames.ACCEPT, equalTo(MimeTypes.JSON))
      .withHeader(HeaderNames.CONTENT_TYPE, equalTo(MimeTypes.JSON))
      .withRequestBody(equalToJson(Json.toJson(publicNotificationRequest).toString()))
    )
  }

  def verifyPublicNotificationServiceWasCalledWith(expectedPayload: JsValue) {
    verify(1, postRequestedFor(urlMatchingRequestPath)
      .withHeader(HeaderNames.ACCEPT, equalTo(MimeTypes.JSON))
      .withHeader(HeaderNames.CONTENT_TYPE, equalTo(MimeTypes.JSON))
      .withRequestBody(equalToJson(expectedPayload.toString()))
    )

  }

  def getTheCallMadeToPublicNotificationGateway :  LoggedRequest = {
    wireMockServer.findAll(postRequestedFor(urlMatchingRequestPath)).get(0)
  }
}
