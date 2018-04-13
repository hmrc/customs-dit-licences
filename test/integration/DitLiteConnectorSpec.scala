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

import org.scalatest.mockito.MockitoSugar
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.AnyContent
import play.api.test.Helpers._
import uk.gov.hmrc.customs.dit.licence.connectors.DitLiteConnector
import uk.gov.hmrc.customs.dit.licence.domain.{ConfigKey, EntryUsage, LateUsage}
import uk.gov.hmrc.customs.dit.licence.model.ValidatedRequest
import uk.gov.hmrc.http._
import uk.gov.hmrc.play.test.UnitSpec
import util.CustomsDitLiteExternalServicesConfig.{DitLiteEntryUsageServiceContext, DitLiteLateUsageServiceContext}
import util.TestData._
import util.externalservices.DitLiteService

class DitLiteConnectorSpec extends UnitSpec with BeforeAndAfterAll with BeforeAndAfterEach with MockitoSugar with TableDrivenPropertyChecks with GuiceOneAppPerSuite with DitLiteService {

  private lazy val connector = app.injector.instanceOf[DitLiteConnector]
  private val externalAuthToken = "external-token"
  private val internalAuthToken = s"Basic $externalAuthToken"
  private implicit val headerCarrier: HeaderCarrier = mock[HeaderCarrier]
  private implicit val vr: ValidatedRequest[AnyContent] = TestValidatedRequest

  override protected def beforeAll() {
    startMockServer()
  }

  override protected def afterEach(): Unit = {
    resetMockServer()
  }

  override protected def afterAll() {
    stopMockServer()
  }

  override implicit lazy val app: Application = new GuiceApplicationBuilder().configure(conf).build()

  private val controllers = Table(("Message Type Description", "External Service Context", "Config Key"),
    ("Entry Usage", DitLiteEntryUsageServiceContext, EntryUsage),
    ("Late Usage", DitLiteLateUsageServiceContext, LateUsage)
  )

  forAll(controllers) { case (messageTypeDesc, url, configKey) =>

    s"DitLiteConnector for $messageTypeDesc" should {

      s"make a correct request for $messageTypeDesc" in {
        setupBackendServiceToReturn(url, OK)
        await(sendValidXml(configKey))
        verifyDitLiteServiceWasCalledWith(requestPath= url, requestBody = ValidXML.toString(), maybeUnexpectedAuthToken = Some(internalAuthToken))
      }

      s"return a failed future when external service returns 404 for $messageTypeDesc" in {
        setupBackendServiceToReturn(url, NOT_FOUND)
        await(sendValidXml(configKey)).status shouldBe NOT_FOUND
      }

      s"return a failed future when external service returns 400 for $messageTypeDesc" in {
        setupBackendServiceToReturn(url, BAD_REQUEST)
        await(sendValidXml(configKey)).status shouldBe BAD_REQUEST
      }

      s"return a failed future when external service returns 500 for $messageTypeDesc" in {
        setupBackendServiceToReturn(url, INTERNAL_SERVER_ERROR)
        await(sendValidXml(configKey)).status shouldBe INTERNAL_SERVER_ERROR
      }

      s"return a failed future when fail to connect the external service for $messageTypeDesc" in {
        stopMockServer()
        intercept[Exception](await(sendValidXml(configKey))).getClass shouldBe classOf[BadGatewayException]
        startMockServer()
      }
    }
  }

  private def sendValidXml(configKey: ConfigKey) = {
    connector.post(configKey)
  }
}
