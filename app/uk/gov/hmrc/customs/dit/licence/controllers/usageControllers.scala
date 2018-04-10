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
import uk.gov.hmrc.customs.api.common.controllers.ErrorResponse
import uk.gov.hmrc.customs.dit.licence.connectors.DitLiteConnector
import uk.gov.hmrc.customs.dit.licence.controllers.CustomHeaderNames.X_CORRELATION_ID_HEADER_NAME
import uk.gov.hmrc.customs.dit.licence.domain.{ConfigKey, EntryUsage, LateUsage}
import uk.gov.hmrc.customs.dit.licence.logging.LicencesLogger
import uk.gov.hmrc.customs.dit.licence.model.{RequestData, ValidatedRequest}
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.play.microservice.controller.BaseController

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

abstract class UsageController @Inject() (validateAndExtractHeadersAction: ValidateAndExtractHeadersAction,
                                          ditLiteConnector: DitLiteConnector,
                                          logger: LicencesLogger,
                                          configKey: ConfigKey) extends BaseController {

  def process(): Action[AnyContent] = (Action andThen validateAndExtractHeadersAction).async(bodyParser = xmlOrEmptyBody) {
    implicit validatedRequest: ValidatedRequest[AnyContent] =>

      logger.info(s"entered ${getClass.getSimpleName} after validating headers")
      validatedRequest.request.body.asXml match {
        case Some(_) =>
          ditLiteConnector.post(configKey).map { response =>
            val headers = extractHeaders(response) + correlationIdHeader(validatedRequest.requestData)
            logger.debug(s"sending the DIT-LITE response to backend with status ${response.status} and\nresponse headers=$headers \nresponse payload=${response.body}")
            Result(ResponseHeader(response.status, headers), Strict(ByteString(response.body), Some(s"${MimeTypes.XML}; charset=UTF-8")))
          }

        case _ =>
          logger.error("Malformed XML")
          Future.successful(ErrorResponse.errorBadRequest("Malformed XML").XmlResult.withHeaders(correlationIdHeader(validatedRequest.requestData)))
      }

  }
      private def xmlOrEmptyBody: BodyParser[AnyContent] = BodyParser(rq => parse.xml(rq).map {
      case Right(xml) => Right(AnyContentAsXml(xml))
      case _ => Right(AnyContentAsEmpty)
    })

  private def correlationIdHeader(requestData: RequestData) = {
    X_CORRELATION_ID_HEADER_NAME -> requestData.correlationId
  }

  private def extractHeaders(response: HttpResponse): Map[String, String] = {
    response.allHeaders.map(h => (h._1, h._2.head))
  }

}


@Singleton
class EntryUsageController @Inject() (validateAndExtractHeadersAction: ValidateAndExtractHeadersAction,
                                      ditLiteConnector: DitLiteConnector,
                                      logger: LicencesLogger)
  extends UsageController(validateAndExtractHeadersAction, ditLiteConnector, logger, EntryUsage) {

  def post(): Action[AnyContent] = {
    super.process()
  }
}

@Singleton
class LateUsageController @Inject() (validateAndExtractHeadersAction: ValidateAndExtractHeadersAction,
                                      ditLiteConnector: DitLiteConnector,
                                      logger: LicencesLogger)
  extends UsageController(validateAndExtractHeadersAction, ditLiteConnector, logger, LateUsage) {

  def post(): Action[AnyContent] = {
    super.process()
  }
}

