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

import org.mockito.Mockito._
import org.scalatest.Matchers
import org.scalatest.mockito.MockitoSugar
import play.api.mvc.Result
import play.api.mvc.Results.Ok
import play.api.test.FakeRequest
import uk.gov.hmrc.customs.dit.licence.controllers.{HeaderValidator, ValidateAndExtractHeadersAction}
import uk.gov.hmrc.customs.dit.licence.logging.LicencesLogger
import uk.gov.hmrc.customs.dit.licence.model.ValidatedRequest
import uk.gov.hmrc.play.test.UnitSpec
import util.RequestHeaders._
import util.TestData._

import scala.concurrent.Future


class ValidateAndExtractHeadersActionSpec extends UnitSpec with Matchers with MockitoSugar {

  val blockReturningOk = (_: ValidatedRequest[_]) => Future.successful(Ok)

  private val mockHeaderValidator = mock[HeaderValidator]
  private val mockLogger = mock[LicencesLogger]
  private val actionRefiner = new ValidateAndExtractHeadersAction(mockHeaderValidator, mockLogger)

  "ValidateAndExtractHeadersAction" should {
    "when valid headers are submitted then return a ValidatedRequest" in {
      val request = FakeRequest().withXmlBody(ValidXML).withHeaders(ValidHeaders.toSeq:_*)

      when(mockHeaderValidator.validateHeaders(request)).thenReturn(Right(TestRequestData))

      val result: Result = await(actionRefiner.invokeBlock(request, blockReturningOk))

      result shouldBe Ok
    }

    "when invalid headers are submitted then return an ErrorResponse" in {
      val request = FakeRequest().withXmlBody(ValidXML).withHeaders(InvalidHeaders.toSeq:_*)

      when(mockHeaderValidator.validateHeaders(InvalidRequestWithoutXCorrelationId)).thenReturn(Left(ErrorXCorrelationIdMissingOrInvalid))

      val result: Result = await(actionRefiner.invokeBlock(request, blockReturningOk))

      result shouldBe ErrorXCorrelationIdMissingOrInvalid.XmlResult
    }

  }
}
