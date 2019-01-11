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

package acceptance

import play.api.libs.json.{JsObject, JsString}
import play.api.mvc.{AnyContentAsXml, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import util.CustomsDitLiteExternalServicesConfig.{DitLiteEntryUsageServiceContext, DitLiteLateUsageServiceContext}
import util.RequestHeaders.XCorrelationIdHeaderName
import util.TestData._

import scala.concurrent.Future

class CustomsDitLicencesUnhappyPathSpec extends AcceptanceTestSpec {

  private val entryUsageEndpoint = "/send-entry-usage"
  private val lateUsageEndpoint = "/send-late-usage"

  override protected def beforeAll() {
    startMockServer()
  }

  override protected def beforeEach() {
    resetMockServer()
  }

  override protected def afterAll() {
    stopMockServer()
  }

  private val MalformedXmlBodyError =
    """<?xml version="1.0" encoding="UTF-8"?>
      |<errorResponse>
      |  <code>BAD_REQUEST</code>
      |  <message>Malformed XML</message>
      |</errorResponse>
    """.stripMargin

  private val InvalidXCorrelationIdHeaderError =
    """<?xml version="1.0" encoding="UTF-8"?>
      |<errorResponse>
      |  <code>BAD_REQUEST</code>
      |  <message>X-Correlation-ID is missing or invalid</message>
      |</errorResponse>
    """.stripMargin

  private val InvalidAcceptHeaderError =
    """<?xml version="1.0" encoding="UTF-8"?>
      |<errorResponse>
      |  <code>ACCEPT_HEADER_INVALID</code>
      |  <message>The accept header is missing or invalid</message>
      |</errorResponse>
    """.stripMargin

  private val controllers = Table(("Message Type Description", "Endpoint", "External Service Context"),
    ("Entry Usage", entryUsageEndpoint, DitLiteEntryUsageServiceContext),
    ("Late Usage", lateUsageEndpoint, DitLiteLateUsageServiceContext)
  )

  forAll(controllers) { case (messageTypeDesc, endpoint, externalServiceContext) =>
    feature(s"The $messageTypeDesc API handles errors as expected") {
      scenario(s"Response status 400 when user submits a malformed xml payload to $messageTypeDesc") {
        Given("the API is available")
        setupPublicNotificationServiceToReturn(OK)
        val request = MalformedXmlRequest.copyFakeRequest(method = POST, uri = endpoint)

        When("a POST request with data is sent to the API")
        val result: Option[Future[Result]] = route(app = app, request)

        Then(s"a response with a 400 status is received")
        result shouldBe 'defined
        val resultFuture = result.value

        status(resultFuture) shouldBe BAD_REQUEST
        headers(resultFuture).get(XCorrelationIdHeaderName) shouldBe 'defined

        And("the response body is a \"malformed xml body\" XML")
        string2xml(contentAsString(resultFuture)) shouldBe string2xml(MalformedXmlBodyError)

      }
    }

    scenario(s"Response status 400 when user submits a non-xml payload for $messageTypeDesc") {
      Given("the API is available")
      setupPublicNotificationServiceToReturn(OK)
      val request = ValidRequest
        .withJsonBody(JsObject(Seq("something" -> JsString("I am a json"))))
        .copyFakeRequest(method = POST, uri = endpoint)

      When("a POST request with data is sent to the API")
      val result: Option[Future[Result]] = route(app = app, request)

      Then(s"a response with a 400 status is received")
      result shouldBe 'defined
      val resultFuture = result.value

      status(resultFuture) shouldBe BAD_REQUEST
      headers(resultFuture).get(XCorrelationIdHeaderName) shouldBe 'defined

      And("the response body is a \"malformed xml body\" XML")
      string2xml(contentAsString(resultFuture)) shouldBe string2xml(MalformedXmlBodyError)
    }

    scenario(s"Response status 400 when user submits a request without an X-Correlation-ID header for $messageTypeDesc") {
      Given("the API is available")
      setupPublicNotificationServiceToReturn(OK)
      val request = InvalidRequestWithoutXCorrelationId.copyFakeRequest(method = POST, uri = endpoint)

      When("a POST request with data is sent to the API")
      val result: Option[Future[Result]] = route(app = app, request)

      Then(s"a response with a 400 status is received")
      result shouldBe 'defined
      val resultFuture = result.value

      status(resultFuture) shouldBe BAD_REQUEST

      And("the response body is an \"invalid X-Correlation-ID header\" XML")
      string2xml(contentAsString(resultFuture)) shouldBe string2xml(InvalidXCorrelationIdHeaderError)
    }

    scenario(s"Response status 406 when user submits a request without an Accept header for $messageTypeDesc") {
      Given("the API is available")
      setupPublicNotificationServiceToReturn(OK)
      val request = ValidRequest.copyFakeRequest(headers = ValidRequest.headers.remove(ACCEPT)).copyFakeRequest(method = POST, uri = endpoint)

      When("a POST request with data is sent to the API")
      val result: Option[Future[Result]] = route(app = app, request)

      Then(s"a response with a 406 status is received")
      result shouldBe 'defined
      val resultFuture = result.value

      status(resultFuture) shouldBe NOT_ACCEPTABLE

      And("the response body is an \"invalid Accept header\" XML")
      string2xml(contentAsString(resultFuture)) shouldBe string2xml(InvalidAcceptHeaderError)
    }

    scenario(s"Response status 500 when user submits a valid request but public notification gateway fails with 500 for $messageTypeDesc") {
      Given("the API is available")
      setupPublicNotificationServiceToReturn(INTERNAL_SERVER_ERROR)
      val request: FakeRequest[AnyContentAsXml] = ValidRequest.copyFakeRequest(method = "POST", uri = endpoint)

      When("a POST request with data is sent to the API")
      val result: Option[Future[Result]] = route(app = app, request)

      Then(s"a response with a 500 status is received")
      result shouldBe 'defined
      val resultFuture = result.value

      status(resultFuture) shouldBe INTERNAL_SERVER_ERROR
      headers(resultFuture).get(XCorrelationIdHeaderName) shouldBe 'defined

      And("the response body is the standard customs xml for Internal Error")
      string2xml(contentAsString(resultFuture)) shouldBe string2xml("<errorResponse><code>INTERNAL_SERVER_ERROR</code><message>Internal server error</message></errorResponse>")
    }
  }
}
