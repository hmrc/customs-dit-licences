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

package acceptance

import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, Matchers, OptionValues}
import play.api.libs.json.{JsObject, JsString}
import play.api.mvc.{AnyContentAsXml, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import util.RequestHeaders.X_CORRELATION_ID_NAME
import util.TestData.{InvalidRequestWithoutXCorrelationId, MalformedXmlRequest, ValidRequest}
import util.externalservices.DitLiteService

import scala.concurrent.Future

class CustomsDitLicencesUnhappyPathSpec extends AcceptanceTestSpec
  with Matchers
  with OptionValues
  with BeforeAndAfterAll
  with BeforeAndAfterEach
  with DitLiteService {

  private val entryUsageEndpoint = "/send-entry-usage"

  override protected def beforeAll() {
    startMockServer()
    startDitLiteService(OK)
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

  feature("The API handles errors as expected") {
    scenario("Response status 400 when user submits a malformed xml payload") {
      Given("the API is available")
      val request = MalformedXmlRequest.copyFakeRequest(method = POST, uri = entryUsageEndpoint)

      When("a POST request with data is sent to the API")
      val result: Option[Future[Result]] = route(app = app, request)

      Then(s"a response with a 400 status is received")
      result shouldBe 'defined
      val resultFuture = result.value

      status(resultFuture) shouldBe BAD_REQUEST
      headers(resultFuture).get(X_CORRELATION_ID_NAME) shouldBe 'defined

      And("the response body is a \"malformed xml body\" XML")
      string2xml(contentAsString(resultFuture)) shouldBe string2xml(MalformedXmlBodyError)

    }
  }

  scenario("Response status 400 when user submits a non-xml payload") {
    Given("the API is available")
    val request = ValidRequest
      .withJsonBody(JsObject(Seq("something" -> JsString("I am a json"))))
      .copyFakeRequest(method = POST, uri = entryUsageEndpoint)

    When("a POST request with data is sent to the API")
    val result: Option[Future[Result]] = route(app = app, request)

    Then(s"a response with a 400 status is received")
    result shouldBe 'defined
    val resultFuture = result.value

    status(resultFuture) shouldBe BAD_REQUEST
    headers(resultFuture).get(X_CORRELATION_ID_NAME) shouldBe 'defined

    And("the response body is a \"malformed xml body\" XML")
    string2xml(contentAsString(resultFuture)) shouldBe string2xml(MalformedXmlBodyError)
  }

  scenario("Response status 400 when user submits a request without an X-Conversation-ID header") {
    Given("the API is available")
    val request = InvalidRequestWithoutXCorrelationId.copyFakeRequest(method = POST, uri = entryUsageEndpoint)

    When("a POST request with data is sent to the API")
    val result: Option[Future[Result]] = route(app = app, request)

    Then(s"a response with a 400 status is received")
    result shouldBe 'defined
    val resultFuture = result.value

    status(resultFuture) shouldBe BAD_REQUEST

    And("the response body is an \"invalid X-Correlation-ID header\" XML")
    string2xml(contentAsString(resultFuture)) shouldBe string2xml(InvalidXCorrelationIdHeaderError)
  }

  scenario("Response status 406 when user submits a request without an Accept header") {
    Given("the API is available")
    val request = ValidRequest.copyFakeRequest(headers = ValidRequest.headers.remove(ACCEPT)).copyFakeRequest(method = POST, uri = entryUsageEndpoint)

    When("a POST request with data is sent to the API")
    val result: Option[Future[Result]] = route(app = app, request)

    Then(s"a response with a 406 status is received")
    result shouldBe 'defined
    val resultFuture = result.value

    status(resultFuture) shouldBe NOT_ACCEPTABLE

    And("the response body is an \"invalid Accept header\" XML")
    string2xml(contentAsString(resultFuture)) shouldBe string2xml(InvalidAcceptHeaderError)
  }

  scenario("Response status 500 when user submits a valid request but DIT-LITE fails with 500") {

    Given("the API is available")
    startDitLiteService(INTERNAL_SERVER_ERROR)
    val request: FakeRequest[AnyContentAsXml] = ValidRequest.copyFakeRequest(method = "POST", uri = entryUsageEndpoint)

    When("a POST request with data is sent to the API")
    val result: Option[Future[Result]] = route(app = app, request)

    Then(s"a response with a 500 status is received")
    result shouldBe 'defined
    val resultFuture = result.value

    status(resultFuture) shouldBe INTERNAL_SERVER_ERROR
    headers(resultFuture).get(X_CORRELATION_ID_NAME) shouldBe 'defined

    And("the response body is the xml supplied by DIT-LITE")
    string2xml(contentAsString(resultFuture)) shouldBe string2xml("<some>xml</some>")
  }

}
