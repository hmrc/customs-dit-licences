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

import org.joda.time.{DateTime, DateTimeZone}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{eq => ameq, _}
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.Eventually
import org.scalatest.mockito.MockitoSugar
import play.api.http.HeaderNames
import play.mvc.Http.MimeTypes
import uk.gov.hmrc.customs.api.common.config.{ServiceConfig, ServiceConfigProvider}
import uk.gov.hmrc.customs.dit.licence.connectors.DitLiteConnector
import uk.gov.hmrc.customs.dit.licence.logging.LicencesLogger
import uk.gov.hmrc.customs.dit.licence.logging.model.SeqOfHeader
import uk.gov.hmrc.customs.dit.licence.services.{DateTimeService, WSHttp}
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, HttpResponse}
import uk.gov.hmrc.play.test.UnitSpec
import util.TestData._

import scala.concurrent.{ExecutionContext, Future}

class DitLiteConnectorSpec extends UnitSpec with MockitoSugar with BeforeAndAfterEach with Eventually {

  private val mockWsPost = mock[WSHttp]
  private val mockLicencesLogger = mock[LicencesLogger]
  private val mockServiceConfigProvider = mock[ServiceConfigProvider]
  private val mockDateTimeService = mock[DateTimeService]

  private val connector = new DitLiteConnector(mockWsPost, mockServiceConfigProvider, mockDateTimeService, mockLicencesLogger)

  private val configKey = "dit-lite-entry-usage"
  private val config = ServiceConfig("some-url", Some("bearer-token"), "default")

  private val xml = <xml></xml>
  private implicit val hc: HeaderCarrier = HeaderCarrier()

  override protected def beforeEach() {
    reset(mockWsPost, mockServiceConfigProvider, mockLicencesLogger)
    when(mockServiceConfigProvider.getConfig(configKey)).thenReturn(config)
    when(mockDateTimeService.nowUtc()).thenReturn(date)
  }

  private val year = 2017
  private val monthOfYear = 7
  private val dayOfMonth = 4
  private val hourOfDay = 13
  private val minuteOfHour = 45
  private val date = new DateTime(year, monthOfYear, dayOfMonth, hourOfDay, minuteOfHour, DateTimeZone.UTC)

  private val httpFormattedDate = "Tue, 04 Jul 2017 13:45:00 UTC"

  private val correlationId = correlationIdUuid

  "DitLiteConnector" can {

    "when making a successful request" should {

      "pass URL from config" in {
        returnResponseForRequest(Future.successful(mock[HttpResponse]))

        awaitRequest

        verify(mockWsPost).POSTString(ameq(config.url), anyString, any[SeqOfHeader])(
          any[HttpReads[HttpResponse]](), any[HeaderCarrier](), any[ExecutionContext])
      }

      "pass the xml in the body" in {
        returnResponseForRequest(Future.successful(mock[HttpResponse]))

        awaitRequest

        verify(mockWsPost).POSTString(anyString, ameq(xml.toString()), any[SeqOfHeader])(
          any[HttpReads[HttpResponse]](), any[HeaderCarrier](), any[ExecutionContext])
      }

      "set the content type header" in {
        returnResponseForRequest(Future.successful(mock[HttpResponse]))

        awaitRequest

        val headersCaptor: ArgumentCaptor[HeaderCarrier] = ArgumentCaptor.forClass(classOf[HeaderCarrier])
        verify(mockWsPost).POSTString(anyString, anyString, any[SeqOfHeader])(
          any[HttpReads[HttpResponse]](), headersCaptor.capture(), any[ExecutionContext])
        headersCaptor.getValue.extraHeaders should contain(HeaderNames.CONTENT_TYPE -> (MimeTypes.XML + "; charset=UTF-8"))
      }

      "set the accept header" in {
        returnResponseForRequest(Future.successful(mock[HttpResponse]))

        awaitRequest

        val headersCaptor: ArgumentCaptor[HeaderCarrier] = ArgumentCaptor.forClass(classOf[HeaderCarrier])
        verify(mockWsPost).POSTString(anyString, anyString, any[SeqOfHeader])(
          any[HttpReads[HttpResponse]](), headersCaptor.capture(), any[ExecutionContext])
        headersCaptor.getValue.extraHeaders should contain(HeaderNames.ACCEPT -> MimeTypes.XML)
      }

      "set the date header" in {
        returnResponseForRequest(Future.successful(mock[HttpResponse]))

        awaitRequest

        val headersCaptor: ArgumentCaptor[HeaderCarrier] = ArgumentCaptor.forClass(classOf[HeaderCarrier])
        verify(mockWsPost).POSTString(anyString, anyString, any[SeqOfHeader])(
          any[HttpReads[HttpResponse]](), headersCaptor.capture(), any[ExecutionContext])
        headersCaptor.getValue.extraHeaders should contain(HeaderNames.DATE -> httpFormattedDate)
      }

      "set the X-Correlation-Id header" in {
        returnResponseForRequest(Future.successful(mock[HttpResponse]))

        awaitRequest

        val headersCaptor: ArgumentCaptor[HeaderCarrier] = ArgumentCaptor.forClass(classOf[HeaderCarrier])
        verify(mockWsPost).POSTString(anyString, anyString, any[SeqOfHeader])(
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
    await(connector.post(xml, correlationId, configKey))
  }

  private def returnResponseForRequest(eventualResponse: Future[HttpResponse]) = {
    when(mockWsPost.POSTString(anyString, anyString, any[SeqOfHeader])(
      any[HttpReads[HttpResponse]](), any[HeaderCarrier](), any[ExecutionContext]))
      .thenReturn(eventualResponse)
  }
}
