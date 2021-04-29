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

package uk.gov.hmrc.customs.dit.licence.logging

import play.api.mvc.AnyContent
import uk.gov.hmrc.customs.dit.licence.model.ValidatedRequest

object LoggingHelper {

  def formatLog(msg: String, validatedRequest: ValidatedRequest[AnyContent]): String = {
    formatMessage(msg, validatedRequest)
  }

  private def formatMessage(msg: String, validatedRequest: ValidatedRequest[AnyContent]): String = {
    s"${format(validatedRequest)} $msg".trim
  }

  private def format(validatedRequest: ValidatedRequest[AnyContent]): String = {
    s"[correlationId=${validatedRequest.requestData.correlationId}]"
  }

}
