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

package uk.gov.hmrc.customs.dit.licence.controllers

import akka.util.ByteString
import javax.inject.{Inject, Singleton}
import play.api.http.HttpEntity.Strict
import play.api.http.MimeTypes
import play.api.mvc._
import uk.gov.hmrc.customs.dit.licence.connectors.DitLiteConnector
import uk.gov.hmrc.customs.dit.licence.logging.LicencesLogger
import uk.gov.hmrc.customs.dit.licence.model.ValidatedRequest
import uk.gov.hmrc.play.microservice.controller.BaseController

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class EntryUsageController @Inject() (validateAndExtractHeadersAction: ValidateAndExtractHeadersAction,
                                      ditLiteConnector: DitLiteConnector,
                                      logger: LicencesLogger) extends BaseController {

  private val configKey = "dit-lite-entry-usage"

  def post(): Action[AnyContent] = (Action andThen validateAndExtractHeadersAction).async {
      implicit validatedRequest: ValidatedRequest[AnyContent] =>

      logger.info("entered EntryUsageController after validating headers")

      val response = ditLiteConnector.post(configKey)
      response.map { response =>
        val headers = response.allHeaders.map {
          h => (h._1, h._2.head)
        }
        logger.debug(s"sending the DIT-LITE response to backend with status ${response.status} and\nresponse headers=$headers \nresponse payload=${response.body}")
        Result(ResponseHeader(response.status, headers), Strict(ByteString(response.body), Some(MimeTypes.XML + "; charset=utf-8")))
      }
  }

}
