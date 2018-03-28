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

import javax.inject.{Inject, Singleton}
import play.api.mvc._
import uk.gov.hmrc.customs.dit.licence.logging.LicencesLogger
import uk.gov.hmrc.customs.dit.licence.model.HeaderMap
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.HeaderCarrierConverter
import uk.gov.hmrc.play.microservice.controller.BaseController

import scala.concurrent.Future

case class ValidatedRequest[A](headerMap: HeaderMap, request: Request[A]) extends WrappedRequest[A](request)

@Singleton
class EntryUsageController @Inject() (validateAndExtractHeadersAction: ValidateAndExtractHeadersAction,
                                      validateRequestPayloadAction: ValidateRequestPayloadAction,
                                      logger: LicencesLogger) extends BaseController {

    def post(): Action[AnyContent] =
      (Action andThen validateAndExtractHeadersAction andThen validateRequestPayloadAction).async {
      implicit validatedRequest =>
        val msg = "Entered EntryUsageController after validating headers"
        logger.info(msg, validatedRequest.headers.headers)
        logger.debug(msg, validatedRequest.headers.headers, validatedRequest.body.toString)
        Future.successful(Ok)
  }

}
