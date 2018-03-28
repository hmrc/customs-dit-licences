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

import org.mockito.ArgumentMatchers.{eq => meq}
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfterEach, Matchers}
import play.api.mvc.AnyContent
import uk.gov.hmrc.customs.dit.licence.controllers.{ValidateRequestPayloadAction, ValidatedRequest}
import uk.gov.hmrc.customs.dit.licence.logging.LicencesLogger
import uk.gov.hmrc.play.test.UnitSpec
import util.TestData._

import scala.xml.NodeSeq


class ValidateRequestPayloadActionSpec extends UnitSpec with Matchers with MockitoSugar with BeforeAndAfterEach {

  private val mockLogger = mock[LicencesLogger]

  private val action = new ValidateRequestPayloadAction(mockLogger)

  "ValidateRequestPayloadAction" should {
    "when a valid payload is submitted None is returned" in {
      val result = await(action.filter(validRequest(ValidXML)))

      result shouldBe None
    }

    "when an invalid payload is submitted an error result is returned" in {
      val result = await(action.filter(invalidRequest("<some>broken xml<some")))

      result shouldBe Some(ErrorUnauthorizedBadXml.XmlResult)
    }
  }

  private def validRequest(payload: NodeSeq): ValidatedRequest[AnyContent] = {
    ValidatedRequest(ValidRequest.headers.toSimpleMap, ValidRequest.withXmlBody(payload))
  }

  private def invalidRequest(payload: String): ValidatedRequest[AnyContent] = {
    ValidatedRequest(ValidRequest.headers.toSimpleMap, ValidRequest.withTextBody(payload) )
  }

}
