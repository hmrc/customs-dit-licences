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
import play.api.mvc.{AnyContentAsText, AnyContentAsXml}
import play.api.test.FakeRequest
import play.api.test.Helpers.{route, _}
import util.RequestHeaders._
import util.TestData._
import util.externalservices.DitLiteService

import scala.xml.NodeSeq


class EntryUsageControllerSpec extends IntegrationTestSpec with MockitoSugar
  with BeforeAndAfterAll with DitLiteService {

  private val EntryUsageUrl = "/send-entry-usage"

  override protected def beforeAll() {
    startMockServer()
  }

  override protected def afterEach(): Unit = {
    resetMockServer()
  }

  override protected def afterAll() {
    stopMockServer()
  }

  "EntryUsageController" should {
    "return Ok for a valid request" in {
      val request: FakeRequest[AnyContentAsXml] = FakeRequest("POST", EntryUsageUrl)
        .withHeaders(ValidHeaders.toSeq: _*).withXmlBody(ValidXML)

      val future = await(route(app, request).get)

      status(future) shouldBe OK
    }

    "return error for a invalid request headers" in {
      val request: FakeRequest[AnyContentAsXml] = FakeRequest("POST", EntryUsageUrl)
        .withHeaders(InvalidHeaders.toSeq: _*).withXmlBody(ValidXML)

      val future = await(route(app, request).get)

      status(future) shouldBe BAD_REQUEST
    }

    "return error for a malformed XML payload" in {
      val request: FakeRequest[AnyContentAsText] = FakeRequest("POST", EntryUsageUrl)
        .withHeaders(ValidHeaders.toSeq: _*).withTextBody("<xml>broken<xml")

      val future = await(route(app, request).get)

      status(future) shouldBe BAD_REQUEST
    }

    "return bad request for no XML payload" in {
      val request = FakeRequest("POST", EntryUsageUrl)
        .withHeaders(ValidHeaders.toSeq: _*).withXmlBody(NodeSeq.Empty)

      val future = await(route(app, request).get)

      status(future) shouldBe BAD_REQUEST
    }
  }

}
