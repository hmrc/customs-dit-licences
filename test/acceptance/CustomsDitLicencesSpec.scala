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

import play.api.mvc.Result
import play.api.test.Helpers._
import util.CustomsDitLiteExternalServicesConfig._
import util.TestData.{ValidEntryRequest, ValidLateRequest}

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

  private val controllers = Table(("Message Type Description", "Usage Message Type", "url"),
    ("Entry Usage", ValidEntryRequest,  DitLiteEntryUsageServiceContext),
    ("Late Usage", ValidLateRequest, DitLiteLateUsageServiceContext)
  )

  forAll(controllers) { case (messageTypeDesc, request, url) =>

    feature(s"Backend submits $messageTypeDesc message") {
      scenario(s"Backend system successfully submits $messageTypeDesc") {
        Given("a valid request")
        setupBackendServiceToReturn(url, OK)

        When("a POST request with data is sent to the API")
        val result: Future[Result] = route(app = app, request).value

        Then("a response with a 200 (OK) status is received")
        status(result) shouldBe OK

        And("the response body is not empty")
        contentAsString(result) should not be 'empty
      }
    }
  }
}
