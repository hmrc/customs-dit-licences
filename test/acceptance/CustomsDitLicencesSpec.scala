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

import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.Helpers._
import uk.gov.hmrc.customs.dit.licence.model.PublicNotificationRequest
import util.CustomsDitLiteExternalServicesConfig._
import util.PublicNotificationTestData.{publicNotificationResponse, _}

import scala.concurrent.Future

class CustomsDitLicencesSpec extends AcceptanceTestSpec {

  override protected def beforeAll() {
    startMockServer()
  }

  override protected def beforeEach() {
    resetMockServer()
  }

  override protected def afterAll() {
    stopMockServer()
  }

  private val controllers = Table(("Message Type Description", "Request", "Request Payload", "External Service Context"),
    ("Entry Usage", ValidEntryUsageRequest, ValidXML1, DitLiteEntryUsageServiceContext),
    ("Late Usage", ValidLateUsageRequest, ValidXML2, DitLiteLateUsageServiceContext)
  )


  forAll(controllers) { case (messageTypeDesc, request, requestPayloadXml, url) =>

    feature(s"Backend submits $messageTypeDesc message") {
      scenario(s"Backend system successfully submits $messageTypeDesc") {
        Given("a valid request")
        setupPublicNotificationServiceToReturn(OK)

        When("a POST request with data is sent to the API")
        val result: Future[Result] = route(app = app, request.withXmlBody(requestPayloadXml)).value

        Then("propagate status returned from public notification gateway")
        status(result) shouldBe publicNotificationResponse.status

        And("propagate XML body returned from public notification gateway")
        contentAsString(result) should not be 'empty
        string2xml(contentAsString(result)) shouldBe string2xml(publicNotificationResponse.xmlPayload)

        And("propagate headers returned from public notification gateway")
        (headers(result) - CACHE_CONTROL).toSet shouldBe publicNotificationResponse.headers.map(h => (h.name, h.value)).toSet

        eventually {
          And("the public notification gateway was called with the expected JSON request")
          val requestMade = getTheCallMadeToPublicNotificationGateway
          val publicNotificationRequestMade = Json.parse(requestMade.getBodyAsString).as[PublicNotificationRequest]

          publicNotificationRequestMade.url shouldBe s"http://localhost:11111$url"

          val actualHeaderSet = publicNotificationRequestMade.headers.toSet
          actualHeaderSet shouldBe ExpectedPublicNotificationRequestHeaderSet

          string2xml(publicNotificationRequestMade.xmlPayload) shouldBe requestPayloadXml
        }
      }
    }
  }
}
