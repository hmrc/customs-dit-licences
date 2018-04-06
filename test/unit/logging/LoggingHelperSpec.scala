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

package unit.logging

import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar
import play.api.mvc.{AnyContent, Request}
import uk.gov.hmrc.customs.dit.licence.logging.LoggingHelper
import uk.gov.hmrc.customs.dit.licence.model.{RequestData, ValidatedRequest}
import uk.gov.hmrc.play.test.UnitSpec
import util.TestData.correlationId

class LoggingHelperSpec extends UnitSpec with MockitoSugar {

  trait Setup {
    val msg = "msg"
    val requestData: RequestData = mock[RequestData]
    val requestMock: Request[AnyContent] = mock[Request[AnyContent]]
    val validatedRequest: ValidatedRequest[AnyContent] = ValidatedRequest[AnyContent](requestData, requestMock)
    when(requestData.correlationId).thenReturn("e61f8eee-812c-4b8f-b193-06aedc60dca2")

    val expectedFormattedHeaders = s"[correlationId=$correlationId]"
  }

  "LoggingHelper" should {

    "format log" in new Setup {
      LoggingHelper.formatLog(msg, validatedRequest) shouldBe s"$expectedFormattedHeaders $msg"
    }

  }
}
