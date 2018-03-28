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

import java.util.UUID

import akka.util.ByteString
import javax.inject.{Inject, Singleton}
import play.api.http.HttpEntity.Strict
import play.api.http.MimeTypes
import play.api.mvc._
import uk.gov.hmrc.customs.dit.licence.connectors.DitLiteConnector
import uk.gov.hmrc.customs.dit.licence.logging.LicencesLogger
import uk.gov.hmrc.customs.dit.licence.logging.model.HeaderMap
import uk.gov.hmrc.play.microservice.controller.BaseController

import scala.concurrent.ExecutionContext.Implicits.global

case class ValidatedRequest[A](headerMap: HeaderMap, request: Request[A]) extends WrappedRequest[A](request)

@Singleton
class EntryUsageController @Inject() (validateAndExtractHeadersAction: ValidateAndExtractHeadersAction,
                                      validateRequestPayloadAction: ValidateRequestPayloadAction,
                                      ditLiteConnector: DitLiteConnector,
                                      logger: LicencesLogger) extends BaseController {

  private val configKey = "dit-lite-entry-usage"

  def post(): Action[AnyContent] =
      (Action andThen validateAndExtractHeadersAction andThen validateRequestPayloadAction).async {
      implicit validatedRequest =>
        val msg = "Entered EntryUsageController after validating headers"
        logger.info(msg, validatedRequest.headerMap.toSeq)
        logger.debug(msg, validatedRequest.headerMap.toSeq, validatedRequest.body.toString)

        //TODO set correct correlationId
        val response = ditLiteConnector.post(validatedRequest.request.body.asXml.get, UUID.fromString("e61f8eee-812c-4b8f-b193-06aedc60dca2"), configKey)
        response.map { response =>
          val headers = response.allHeaders.map {
            h => (h._1, h._2.head)
          }
          logger.debug(s"sending the DIT-LITE response to backend which has status ${response.status}", headers.toSeq, response.body)
          Result(ResponseHeader(response.status, headers), Strict(ByteString(response.body), Some(MimeTypes.XML + "; charset=utf-8")))
        }
  }

}
