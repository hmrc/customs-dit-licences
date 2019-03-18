/*
 * Copyright 2019 HM Revenue & Customs
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
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers._
import uk.gov.hmrc.customs.dit.licence.connectors.PublicNotificationServiceConnector
import uk.gov.hmrc.customs.dit.licence.model.PublicNotificationResponse
import uk.gov.hmrc.http._
import util.CustomsDitLiteExternalServicesConfig
import util.ExternalServicesConfig.{Host, Port}
import util.PublicNotificationTestData._
import util.TestData.TestValidatedRequest
import util.externalservices.PublicNotificationService

class PublicNotificationServiceConnectorSpec extends IntegrationTestSpec with GuiceOneAppPerSuite with MockitoSugar
  with BeforeAndAfterAll with PublicNotificationService {

  private lazy val connector = app.injector.instanceOf[PublicNotificationServiceConnector]

  private implicit val hc: HeaderCarrier = HeaderCarrier()

  private implicit val vr = TestValidatedRequest

  override protected def beforeAll() {
    startMockServer()
  }

  override protected def afterEach(): Unit = {
    resetMockServer()
  }

  override protected def afterAll() {
    stopMockServer()
  }

  override implicit lazy val app: Application =
    GuiceApplicationBuilder().configure(Map(
      "auditing.enabled" -> false,
      "microservice.services.public-notification.host" -> Host,
      "microservice.services.public-notification.port" -> Port,
      "microservice.services.public-notification.context" -> CustomsDitLiteExternalServicesConfig.PublicNotificationServiceContext
    )).build()

  "PublicNotificationServiceConnector" should {

    "make a correct request" in {
      setupPublicNotificationServiceToReturn(OK)

      val response: PublicNotificationResponse = await(connector.send(publicNotificationEntryUsageRequest))

      response.status shouldBe OK
      verifyPublicNotificationServiceWasCalledWith(publicNotificationEntryUsageRequest)
      response shouldBe publicNotificationResponse
    }

    "return a failed future with wrapped HttpVerb NotFoundException when external service returns 404" in {
      setupPublicNotificationServiceToReturn(NOT_FOUND)

      val caught = intercept[RuntimeException](await(connector.send(publicNotificationEntryUsageRequest)))

      caught.getCause.getClass shouldBe classOf[NotFoundException]
    }

    "return a failed future with wrapped HttpVerbs BadRequestException when external service returns 400" in {
      setupPublicNotificationServiceToReturn(BAD_REQUEST)

      val caught = intercept[RuntimeException](await(connector.send(publicNotificationEntryUsageRequest)))

      caught.getCause.getClass shouldBe classOf[BadRequestException]
    }

    "return a failed future with Upstream5xxResponse when external service returns 500" in {
      setupPublicNotificationServiceToReturn(INTERNAL_SERVER_ERROR)

      intercept[Upstream5xxResponse](await(connector.send(publicNotificationEntryUsageRequest)))
    }

    "return a failed future with wrapped HttpVerbs BadRequestException when it fails to connect the external service" in
      withoutWireMockServer {
        val caught = intercept[RuntimeException](await(connector.send(publicNotificationEntryUsageRequest)))

        caught.getCause.getClass shouldBe classOf[BadGatewayException]
      }
  }

  private def withoutWireMockServer(thunk: => Any) {
    if (wireMockServer.isRunning) {
      stopMockServer()
      try thunk
      finally startMockServer()
    } else {
      thunk
    }
  }

}
