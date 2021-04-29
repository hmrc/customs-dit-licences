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

package unit.model

import play.api.libs.json.Json
import uk.gov.hmrc.customs.dit.licence.model.PublicNotificationResponse._
import uk.gov.hmrc.customs.dit.licence.model.{PublicNotificationRequest, PublicNotificationResponse}
import util.UnitSpec
import util.PublicNotificationTestData._

class PublicNotificationModelsSpec extends UnitSpec {

  "Response model" should {
    "marshal to Json" in {
      val jsValue = Json.toJson(publicNotificationResponse)
      val json = Json.prettyPrint(jsValue)

      json shouldBe publicNotificationResponseAsJson
    }

    "un-marshal to model" in {
      val jsValue = Json.parse(publicNotificationResponseAsJson)

      jsValue.as[PublicNotificationResponse] shouldBe publicNotificationResponse
    }
  }

  "Request model" should {
    "marshal to Json" in {
      val jsValue = Json.toJson(publicNotificationEntryUsageRequest)
      val json = Json.prettyPrint(jsValue)

      json shouldBe publicNotificationEntryUsageRequestAsJson
    }

    "un-marshal to model" in {
      val jsValue = Json.parse(publicNotificationEntryUsageRequestAsJson)

      jsValue.as[PublicNotificationRequest] shouldBe publicNotificationEntryUsageRequest
    }
  }

}
