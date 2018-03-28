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

package acceptance

import org.scalatest._
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import util.{CustomsDitLiteExternalServicesConfig, ExternalServicesConfig}

import scala.util.control.NonFatal
import scala.xml.{Node, Utility, XML}

trait AcceptanceTestSpec extends FeatureSpec with GivenWhenThen with GuiceOneAppPerSuite
   with BeforeAndAfterAll with BeforeAndAfterEach {

  private val protocol = "http"

  override implicit lazy val app: Application = new GuiceApplicationBuilder().configure(Map(
    "microservice.services.dit-lite-entry-usage.protocol" -> protocol,
    "microservice.services.dit-lite-entry-usage.host" -> ExternalServicesConfig.Host,
    "microservice.services.dit-lite-entry-usage.port" -> ExternalServicesConfig.Port,
    "microservice.services.dit-lite-entry-usage.context" -> CustomsDitLiteExternalServicesConfig.DitLiteEntryUsageServiceContext,
    "microservice.services.dit-lite-entry-usage.bearer-token" -> ExternalServicesConfig.AuthToken,
    "microservice.services.dit-lite-late-usage.protocol" -> protocol,
    "microservice.services.dit-lite-late-usage.host" -> ExternalServicesConfig.Host,
    "microservice.services.dit-lite-late-usage.port" -> ExternalServicesConfig.Port,
    "microservice.services.dit-lite-late-usage.context" -> CustomsDitLiteExternalServicesConfig.DitLiteLateUsageServiceContext,
    "microservice.services.dit-lite-late-usage.bearer-token" -> ExternalServicesConfig.AuthToken,
    "auditing.enabled" -> false
    )).build()

  protected def string2xml(s: String): Node = {
    val xml = try {
      XML.loadString(s)
    } catch {
      case NonFatal(thr) => fail("Not an xml: " + s, thr)
    }
    Utility.trim(xml)
  }

}