/*
 * Copyright 2021 HM Revenue & Customs
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

package component

import org.scalatest._
import org.scalatest.concurrent.Eventually
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import util.TestData._
import util.externalservices.PublicNotificationService

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.control.NonFatal
import scala.xml.{Node, Utility, XML}

trait AcceptanceTestSpec extends FeatureSpec
    with GivenWhenThen
    with GuiceOneAppPerSuite
    with BeforeAndAfterAll
    with BeforeAndAfterEach
    with Matchers
    with OptionValues
    with PublicNotificationService
    with TableDrivenPropertyChecks
    with Eventually {

  override implicit val patienceConfig = PatienceConfig(timeout = 5 seconds)

  override implicit lazy val app: Application = new GuiceApplicationBuilder().configure(conf).build()

  protected def string2xml(s: String): Node = {
    val xml = try {
      XML.loadString(s)
    } catch {
      case NonFatal(thr) => fail("Not an xml: " + s, thr)
    }
    Utility.trim(xml)
  }

}
