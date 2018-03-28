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

import uk.gov.hmrc.customs.dit.licence.logging.LoggingHelper
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec

import util.RequestHeaders._
import util.TestData.correlationIdValue

class LoggingHelperSpec extends UnitSpec {

  private val errorMsg = "ERROR"
  private val infoMsg = "INFO"
  private val debugMsg = "DEBUG"
  private val expectedFormattedSignificantHeaders = s"[correlationId=$correlationIdValue]"
  private val expectedHeaders = s"headers=List((X-Correlation-ID,$correlationIdValue))"
  private val miniXmlPayload: String =
        """<xml>
          | <content>This is well-formed XML</content>
          |</xml>""".stripMargin

  "LoggingHelper" should {

    "format ERROR" in {
      LoggingHelper.formatError(errorMsg, LoggingHeaders) shouldBe s"$expectedFormattedSignificantHeaders $errorMsg"
    }

    "format INFO" in {
      LoggingHelper.formatInfo(infoMsg, LoggingHeaders) shouldBe s"$expectedFormattedSignificantHeaders $infoMsg"
    }

    "format DEBUG" in {
      LoggingHelper.formatDebug(debugMsg, LoggingHeaders) shouldBe
        s"$expectedFormattedSignificantHeaders $debugMsg \n$expectedHeaders"
    }

    "format DEBUG with payload" in {
      LoggingHelper.formatDebug(debugMsg, LoggingHeaders, Some(miniXmlPayload.toString)) shouldBe
        s"$expectedFormattedSignificantHeaders $debugMsg \n$expectedHeaders\npayload=\n<xml>\n <content>This is well-formed XML</content>\n</xml>"
    }

  }
}
