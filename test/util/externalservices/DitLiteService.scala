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

package util.externalservices

import com.github.tomakehurst.wiremock.client.WireMock._
import play.api.http.MimeTypes
import play.api.test.Helpers._
import util.{ExternalServicesConfig, WireMockRunner}

trait DitLiteService extends WireMockRunner {

  def setupBackendServiceToReturn(requestPath: String, status: Int): Unit =
    stubFor(post(urlMatching(requestPath))
      .withHeader(ACCEPT, equalTo(MimeTypes.XML))
      .withHeader(CONTENT_TYPE, equalTo(s"${MimeTypes.XML}; charset=UTF-8"))
      .withHeader("X-Correlation-ID", matching("^[A-Za-z0-9-]{36}$"))
      .withHeader(AUTHORIZATION, equalTo(s"Basic ${ExternalServicesConfig.AuthToken}"))
      willReturn aResponse()
      .withStatus(status)
      .withBody("<some>xml</some>"))

  def verifyDitLiteServiceWasCalledWith(requestPath: String,
                                        requestBody: String,
                                        expectedAuthToken: String = ExternalServicesConfig.AuthToken,
                                        maybeUnexpectedAuthToken: Option[String] = None) {
    verify(1, postRequestedFor(urlMatching(requestPath))
      .withHeader(CONTENT_TYPE, equalTo(XML + "; charset=UTF-8"))
      .withHeader(ACCEPT, equalTo(XML))
      .withHeader(AUTHORIZATION, equalTo(s"Basic $expectedAuthToken"))
      .withHeader("X-Correlation-ID", notMatching(""))
      .withRequestBody(equalToXml(requestBody))
      )

    maybeUnexpectedAuthToken foreach { unexpectedAuthToken =>
      verify(0, postRequestedFor(urlMatching(requestPath)).withHeader(AUTHORIZATION, equalTo(s"Basic $unexpectedAuthToken")))
    }
  }
}
