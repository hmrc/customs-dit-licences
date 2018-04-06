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

package integration

import org.scalatest.BeforeAndAfterAll
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.AnyContent
import play.api.test.Helpers._
import uk.gov.hmrc.customs.dit.licence.connectors.DitLiteConnector
import uk.gov.hmrc.customs.dit.licence.model.{RequestData, ValidatedRequest}
import uk.gov.hmrc.http._
import util.TestData._
import util.externalservices.DitLiteService
import util.{CustomsDitLiteExternalServicesConfig, ExternalServicesConfig}

class DitLiteConnectorSpec extends IntegrationTestSpec with GuiceOneAppPerSuite with MockitoSugar
  with BeforeAndAfterAll with DitLiteService {

  private lazy val connector = app.injector.instanceOf[DitLiteConnector]
  private val protocol = "http"

  private val externalAuthToken = "external-token"
  private val internalAuthToken = s"Basic $externalAuthToken"
  private val configKey = "dit-lite-entry-usage"
  private implicit val headerCarrier: HeaderCarrier = mock[HeaderCarrier]
  private val requestData: RequestData = RequestData("e61f8eee-812c-4b8f-b193-06aedc60dca2")
  private implicit val validatedRequest: ValidatedRequest[AnyContent] = ValidatedRequest[AnyContent](requestData, ValidRequest)

  override protected def beforeAll() {
    startMockServer()
  }

  override protected def afterEach(): Unit = {
    resetMockServer()
  }

  override protected def afterAll() {
    stopMockServer()
  }

  override implicit lazy val app: Application = new GuiceApplicationBuilder().configure(Map(
    "microservice.services.dit-lite-entry-usage.protocol" -> protocol,
    "microservice.services.dit-lite-entry-usage.host" -> ExternalServicesConfig.Host,
    "microservice.services.dit-lite-entry-usage.port" -> ExternalServicesConfig.Port,
    "microservice.services.dit-lite-entry-usage.context" -> CustomsDitLiteExternalServicesConfig.DitLiteEntryUsageServiceContext,
    "microservice.services.dit-lite-entry-usage.bearer-token" -> ExternalServicesConfig.AuthToken,
    "auditing.enabled" -> false
    )).build()


  "DitLiteConnector" should {

    "make a correct request" in {
      startDitLiteService(OK)
      await(sendValidXml())
      verifyDitLiteServiceWasCalledWith(requestBody = ValidXML.toString(), maybeUnexpectedAuthToken = Some(internalAuthToken))
    }

    "return a failed future when external service returns 404" in {
      startDitLiteService(NOT_FOUND)
      await(sendValidXml()).status shouldBe NOT_FOUND
    }

    "return a failed future when external service returns 400" in {
      startDitLiteService(BAD_REQUEST)
      await(sendValidXml()).status shouldBe BAD_REQUEST
    }

    "return a failed future when external service returns 500" in {
      startDitLiteService(INTERNAL_SERVER_ERROR)
      await(sendValidXml()).status shouldBe INTERNAL_SERVER_ERROR
    }

    "return a failed future when fail to connect the external service" in {
      stopMockServer()
      intercept[Exception](await(sendValidXml())).getClass shouldBe classOf[BadGatewayException]
      startMockServer()
    }

  }

  private def sendValidXml() = {
    connector.post(configKey)
  }
}
