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
import play.api.mvc.{AnyContentAsXml, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import util.externalservices.DitLiteService
import util.TestData.ValidRequest

import scala.concurrent.Future

class CustomsDitLicencesSpec extends AcceptanceTestSpec
  with Matchers
  with OptionValues
  with BeforeAndAfterAll
  with BeforeAndAfterEach
  with DitLiteService {

  private val entryUsageEndpoint = "/send-entry-usage"

  override protected def beforeAll() {
    startMockServer()
  }

  override protected def beforeEach() {
    resetMockServer()
  }

  override protected def afterAll() {
    stopMockServer()
  }

//TODO complete when connector written
  feature("Submissions for entry usage") {
    scenario("Backend system successfully submits usage") {
      Given("The backend wants to submit valid licence usage")
      startDitLiteService()
      val request: FakeRequest[AnyContentAsXml] = ValidRequest.copyFakeRequest(method = "POST", uri = entryUsageEndpoint)

      When("a POST request with data is sent to the API")
      val result: Future[Result] = route(app = app, request).value

      Then("a response with a 200 (OK) status is received")
      status(result) shouldBe OK

    }

  }

}
