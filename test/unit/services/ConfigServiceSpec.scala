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

package unit.services

import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest.mockito.MockitoSugar
import play.api.{Configuration, Environment, Mode}
import uk.gov.hmrc.customs.api.common.config.{ConfigValidatedNelAdaptor, ServicesConfig}
import uk.gov.hmrc.customs.api.common.logging.CdsLogger
import uk.gov.hmrc.customs.dit.licence.services.LicenceConfigService
import uk.gov.hmrc.play.test.UnitSpec

class ConfigServiceSpec extends UnitSpec with MockitoSugar {

  private val validAppConfig: Config = ConfigFactory.parseString(
    """
      |microservice = {
      |  services = {
      |    public-notification = {
      |      host = "localhost"
      |      port = "9822"
      |      context = "/send-dit-lite"
      |    }
      |  }
      |}
      |
      |auth.token.internal = "dummy-token-internal"
    """.stripMargin)

  private val emptyAppConfig: Config = ConfigFactory.parseString("")

  private val validServicesConfiguration = Configuration(validAppConfig)
  private val emptyServicesConfiguration = Configuration(emptyAppConfig)

  private val mockCdsLogger = mock[CdsLogger]

  private def customsConfigService(configuration: Configuration): LicenceConfigService =
    new LicenceConfigService(new ConfigValidatedNelAdaptor(testServicesConfig(configuration), configuration), mockCdsLogger)

  "CustomsConfigService" should {
    "return config as object model when configuration is valid" in {
      val configModel = customsConfigService(validServicesConfiguration)

      configModel.publicNotificationUrl shouldBe "http://localhost:9822/send-dit-lite"
      configModel.basicAuthTokenInternal shouldBe "dummy-token-internal"
    }

    "throw an exception when configuration is invalid, that contains AGGREGATED error messages" in {
      val expectedErrorMessage =
        """
          |Could not find config key 'auth.token.internal'
          |Could not find config public-notification.host
          |Service configuration not found for key: public-notification.context""".stripMargin

      val caught = intercept[IllegalStateException](customsConfigService(emptyServicesConfiguration))
      caught.getMessage shouldBe expectedErrorMessage
    }
  }

  private def testServicesConfig(configuration: Configuration) = new ServicesConfig(configuration, mock[Environment]) {
    override val mode: Mode.Value = play.api.Mode.Test
  }

}
