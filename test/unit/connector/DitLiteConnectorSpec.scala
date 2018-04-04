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

package unit.connector

import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{eq => ameq, _}
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.Eventually
import org.scalatest.mockito.MockitoSugar
import play.api.http.HeaderNames
import play.api.mvc.AnyContent
import play.mvc.Http.MimeTypes
import uk.gov.hmrc.customs.api.common.config.{ServiceConfig, ServiceConfigProvider}
import uk.gov.hmrc.customs.dit.licence.connectors.DitLiteConnector
import uk.gov.hmrc.customs.dit.licence.logging.LicencesLogger
import uk.gov.hmrc.customs.dit.licence.model.{RequestData, ValidatedRequest}
import uk.gov.hmrc.customs.dit.licence.services.WSHttp
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, HttpResponse}
import uk.gov.hmrc.play.test.UnitSpec
import util.TestData._

import scala.concurrent.{ExecutionContext, Future}

class DitLiteConnectorSpec extends UnitSpec with MockitoSugar with BeforeAndAfterEach with Eventually {

  private val mockWsPost = mock[WSHttp]
  private val mockLicencesLogger = mock[LicencesLogger]
  private val mockServiceConfigProvider = mock[ServiceConfigProvider]

  private val connector = new DitLiteConnector(mockWsPost, mockServiceConfigProvider, mockLicencesLogger)

  private val configKey = "dit-lite-entry-usage"
  private val config = ServiceConfig("some-url", Some("bearer-token"), "default")

  private implicit val hc: HeaderCarrier = HeaderCarrier()

  override protected def beforeEach() {
    reset(mockWsPost, mockServiceConfigProvider, mockLicencesLogger)
    when(mockServiceConfigProvider.getConfig(configKey)).thenReturn(config)
  }

  private val requestData: RequestData = RequestData(correlationId)
  private implicit val validatedRequest: ValidatedRequest[AnyContent] = ValidatedRequest[AnyContent](requestData, ValidRequest)

  "DitLiteConnector" can {

    "when making a successful request" should {

      "pass URL from config" in {
        returnResponseForRequest(Future.successful(mock[HttpResponse]))

        awaitRequest

        verify(mockWsPost).POSTString(ameq(config.url), anyString, any[Seq[(String, String)]])(
          any[HttpReads[HttpResponse]](), any[HeaderCarrier](), any[ExecutionContext])
      }

      "pass the xml in the body" in {
        returnResponseForRequest(Future.successful(mock[HttpResponse]))

        awaitRequest

        verify(mockWsPost).POSTString(anyString, ameq(ValidXML.toString()), any[Seq[(String, String)]])(
          any[HttpReads[HttpResponse]](), any[HeaderCarrier](), any[ExecutionContext])
      }

      "set the content type header" in {
        returnResponseForRequest(Future.successful(mock[HttpResponse]))

        awaitRequest

        val headersCaptor: ArgumentCaptor[HeaderCarrier] = ArgumentCaptor.forClass(classOf[HeaderCarrier])
        verify(mockWsPost).POSTString(anyString, anyString, any[Seq[(String, String)]])(
          any[HttpReads[HttpResponse]](), headersCaptor.capture(), any[ExecutionContext])
        headersCaptor.getValue.extraHeaders should contain(HeaderNames.CONTENT_TYPE -> (MimeTypes.XML + "; charset=UTF-8"))
      }

      "set the accept header" in {
        returnResponseForRequest(Future.successful(mock[HttpResponse]))

        awaitRequest

        val headersCaptor: ArgumentCaptor[HeaderCarrier] = ArgumentCaptor.forClass(classOf[HeaderCarrier])
        verify(mockWsPost).POSTString(anyString, anyString, any[Seq[(String, String)]])(
          any[HttpReads[HttpResponse]](), headersCaptor.capture(), any[ExecutionContext])
        headersCaptor.getValue.extraHeaders should contain(HeaderNames.ACCEPT -> MimeTypes.XML)
      }

      "set the X-Correlation-Id header" in {
        returnResponseForRequest(Future.successful(mock[HttpResponse]))

        awaitRequest

        val headersCaptor: ArgumentCaptor[HeaderCarrier] = ArgumentCaptor.forClass(classOf[HeaderCarrier])
        verify(mockWsPost).POSTString(anyString, anyString, any[Seq[(String, String)]])(
          any[HttpReads[HttpResponse]](), headersCaptor.capture(), any[ExecutionContext])
        headersCaptor.getValue.extraHeaders should contain("X-Correlation-ID" -> correlationId.toString)
      }

    }

    "when making an failing request" should {
      "propagate an underlying error when DIT-LITE call fails with a non-http exception" in {
        returnResponseForRequest(Future.failed(emulatedServiceFailure))

        val caught = intercept[EmulatedServiceFailure] {
          awaitRequest
        }
        caught shouldBe emulatedServiceFailure
      }

    }
  }

  private def awaitRequest = {
    await(connector.post(configKey))
  }

  private def returnResponseForRequest(eventualResponse: Future[HttpResponse]) = {
    when(mockWsPost.POSTString(anyString, anyString, any[Seq[(String, String)]])(
      any[HttpReads[HttpResponse]](), any[HeaderCarrier](), any[ExecutionContext]))
      .thenReturn(eventualResponse)
  }
}
