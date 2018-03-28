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
import org.mockito.Mockito._
import org.scalatest.Matchers
import org.scalatest.mockito.MockitoSugar
import uk.gov.hmrc.customs.dit.licence.controllers.{HeaderValidator, ValidateAndExtractHeadersAction, ValidatedRequest}
import uk.gov.hmrc.customs.dit.licence.logging.LicencesLogger
import uk.gov.hmrc.play.test.UnitSpec
import util.RequestHeaders._
import util.TestData._


class ValidateAndExtractHeadersActionSpec extends UnitSpec with Matchers with MockitoSugar {

  private val mockHeaderValidator = mock[HeaderValidator]
  private val mockLogger = mock[LicencesLogger]

  private val action = new ValidateAndExtractHeadersAction(mockHeaderValidator, mockLogger)

  "ValidateAndExtractHeadersAction" should {
    "when valid headers are submitted then return a ValidatedRequest" in {
      when(mockHeaderValidator.validateHeaders(ValidHeaders)).thenReturn(Right(()))

      val result = await(action.refine(ValidRequest))

      result shouldBe Right(ValidatedRequest(ValidHeaders, ValidRequest))
    }

    "when invalid headers are submitted then return an ErrorResponse" in {
      when(mockHeaderValidator.validateHeaders(InvalidHeaders)).thenReturn(Left(ErrorXCorrelationIdMissingOrInvalid))

      val result = await(action.refine(InvalidRequestWithoutXCorrelationId))

      result shouldBe Left(ErrorXCorrelationIdMissingOrInvalid.XmlResult)
    }

  }


}
