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

package unit.controllers

import org.scalatest.{BeforeAndAfterEach, Matchers}
import org.scalatest.mockito.MockitoSugar
import play.api.test.FakeRequest
import uk.gov.hmrc.customs.dit.licence.controllers.DummyController
import uk.gov.hmrc.play.test.UnitSpec
import play.api.test.Helpers._
import uk.gov.hmrc.customs.dit.licence.logging.LicencesLogger

class DummyControllerSpec extends UnitSpec with Matchers with MockitoSugar with BeforeAndAfterEach {

  private val mockLogger = mock[LicencesLogger]
  val controller = new DummyController(mockLogger)

  "DummyController" should {
    "return 200 OK for valid request" in {
      val result = controller.post().apply(FakeRequest())
      status(result) shouldBe ACCEPTED
    }
  }
}
