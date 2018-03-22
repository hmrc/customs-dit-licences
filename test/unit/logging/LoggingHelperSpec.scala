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
  private val warnMsg = "WARN"
  private val infoMsg = "INFO"
  private val debugMsg = "DEBUG"
  private val url = "http://some-url"
  private val expectedFormattedSignificantHeaders = s"[correlationId=$correlationIdValue]"
  private def expectedHeaders(requestChain: String) = s"headers=List((X-Request-Chain,$requestChain), (X-Correlation-ID,$correlationIdValue))"
  private implicit val hc: HeaderCarrier = HeaderCarrier(extraHeaders = LoggingHeaders)
  private val miniXmlPayload: String =
        """<xml>
          | <content>This is well-formed XML</content>
          |</xml>""".stripMargin

  "LoggingHelper" should {

    "format ERROR" in {
      LoggingHelper.formatError(errorMsg) shouldBe s"$expectedFormattedSignificantHeaders $errorMsg"
    }

    "format WARN"  in {
      LoggingHelper.formatWarn(warnMsg) shouldBe s"$expectedFormattedSignificantHeaders $warnMsg"
    }

    "format INFO with HeaderCarrier" in {
      LoggingHelper.formatInfo(infoMsg) shouldBe s"$expectedFormattedSignificantHeaders $infoMsg"
    }

    "format INFO with headers" in {
      LoggingHelper.formatInfo(infoMsg, LoggingHeaders) shouldBe s"$expectedFormattedSignificantHeaders $infoMsg"
    }

    "format DEBUG with HeaderCarrier" in {
      val requestChain = hc.requestChain.value
      LoggingHelper.formatDebug(debugMsg) shouldBe
        s"$expectedFormattedSignificantHeaders $debugMsg \n${expectedHeaders(requestChain)}"
    }

    "format DEBUG with headers" in {
      LoggingHelper.formatDebug(debugMsg, LoggingHeaders) shouldBe
        s"$expectedFormattedSignificantHeaders $debugMsg \nheaders=List((X-Correlation-ID,$correlationIdValue))"
    }

    "format DEBUG with url and payload" in {
      val requestChain = hc.requestChain.value
      LoggingHelper.formatDebug(debugMsg, Some(url), Some(miniXmlPayload.toString)) shouldBe
        s"$expectedFormattedSignificantHeaders $debugMsg url=http://some-url\n${expectedHeaders(requestChain)}\npayload=\n<xml>\n <content>This is well-formed XML</content>\n</xml>"
    }

    "format DEBUG with url and no payload" in {
      val requestChain = hc.requestChain.value
      LoggingHelper.formatDebug(debugMsg, Some(url)) shouldBe
        s"$expectedFormattedSignificantHeaders $debugMsg url=http://some-url\n${expectedHeaders(requestChain)}"
    }

    "format DEBUG with payload and no url" in {
      val requestChain = hc.requestChain.value
      LoggingHelper.formatDebug(debugMsg, None, Some(miniXmlPayload.toString)) shouldBe
        s"$expectedFormattedSignificantHeaders $debugMsg \n${expectedHeaders(requestChain)}\npayload=\n<xml>\n <content>This is well-formed XML</content>\n</xml>"
    }

  }
}
