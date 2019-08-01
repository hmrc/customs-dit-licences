/*
 * Copyright 2019 HM Revenue & Customs
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
import play.api.http.MimeTypes
import play.api.mvc._
import uk.gov.hmrc.customs.api.common.config.ServiceConfigProvider
import uk.gov.hmrc.customs.api.common.controllers.ErrorResponse
import uk.gov.hmrc.customs.dit.licence.connectors.PublicNotificationServiceConnector
import uk.gov.hmrc.customs.dit.licence.controllers.CustomHeaderNames.X_CORRELATION_ID_HEADER_NAME
import uk.gov.hmrc.customs.dit.licence.domain.{ConfigKey, EntryUsage, LateUsage}
import uk.gov.hmrc.customs.dit.licence.logging.LicencesLogger
import uk.gov.hmrc.customs.dit.licence.model.{PublicNotificationRequest, PublicNotificationRequestHeader, RequestData, ValidatedRequest}
import uk.gov.hmrc.play.bootstrap.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

abstract class UsageController @Inject()(cc: ControllerComponents,
                                         validateAndExtractHeadersAction: ValidateAndExtractHeadersAction,
                                         connector: PublicNotificationServiceConnector,
                                         serviceConfigProvider: ServiceConfigProvider,
                                         logger: LicencesLogger,
                                         configKey: ConfigKey)(implicit ec: ExecutionContext) extends BackendController(cc) {

  private lazy val entryUsageUrlAndBasicToken: UrlAndBasicToken = urlAndBasicToken(EntryUsage)
  private lazy val lateEntryUrlAndBasicToken: UrlAndBasicToken = urlAndBasicToken(LateUsage)

  private def urlAndBasicToken = configKey match {
    case EntryUsage => entryUsageUrlAndBasicToken
    case LateUsage => lateEntryUrlAndBasicToken
  }

  private case class UrlAndBasicToken(url: String, basicToken: String)

  def process(): Action[AnyContent] = (Action andThen validateAndExtractHeadersAction).async(bodyParser = xmlOrEmptyBody) {
    implicit validatedRequest: ValidatedRequest[AnyContent] =>

      logger.info(s"processing ${getClass.getSimpleName} request after validating headers")
      validatedRequest.request.body.asXml match {
        case Some(xml) =>
          val publicNotificationRequest = PublicNotificationRequest(
            urlAndBasicToken.url,
            getMandatoryHeaders(validatedRequest.requestData, urlAndBasicToken.basicToken),
            xml.toString
          )
          connector.send(publicNotificationRequest).map { pnr =>
            logger.debug(s"sending the request to the public notification gateway with status ${pnr.status} and\nheaders=${pnr.headers} \npnr payload=${pnr.xmlPayload.toString}")
            val headers: Seq[(String, String)] = pnr.headers.map(h => (h.name, h.value))
            Results.Status(pnr.status)(pnr.xmlPayload).withHeaders(headers: _*).as(s"${MimeTypes.XML}; charset=UTF-8")
          }
          .recover{
            case NonFatal(e) =>
              logger.error("error sending the request to the public notification gateway", e)
              ErrorResponse.ErrorInternalServerError.XmlResult.withHeaders(correlationIdHeader(validatedRequest.requestData))
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

  private def urlAndBasicToken(configKey: ConfigKey) = {
    UrlAndBasicToken(
      serviceConfigProvider.getConfig(configKey.name).url,
      serviceConfigProvider.getConfig(configKey.name).bearerToken.getOrElse(throw new IllegalStateException(s"no basic token was found in config for ${configKey.name}"))
    )
  }

  private def getMandatoryHeaders(requestData: RequestData, basicToken: String): Seq[PublicNotificationRequestHeader] = {
    Seq(
      PublicNotificationRequestHeader(ACCEPT, MimeTypes.XML),
      PublicNotificationRequestHeader(CONTENT_TYPE, s"${MimeTypes.XML}; charset=UTF-8"),
      PublicNotificationRequestHeader("X-Correlation-ID", requestData.correlationId),
      PublicNotificationRequestHeader(AUTHORIZATION, "Basic " + basicToken)
    )
  }

}

@Singleton
class EntryUsageController @Inject()(cc: ControllerComponents,
                                     validateAndExtractHeadersAction: ValidateAndExtractHeadersAction,
                                     connector: PublicNotificationServiceConnector,
                                     serviceConfigProvider: ServiceConfigProvider,
                                     logger: LicencesLogger)(implicit ec: ExecutionContext)
  extends UsageController(cc ,validateAndExtractHeadersAction, connector, serviceConfigProvider, logger, EntryUsage) {

  def post(): Action[AnyContent] = {
    super.process()
  }
}

@Singleton
class LateUsageController @Inject()(cc: ControllerComponents,
                                    validateAndExtractHeadersAction: ValidateAndExtractHeadersAction,
                                    connector: PublicNotificationServiceConnector,
                                    serviceConfigProvider: ServiceConfigProvider,
                                    logger: LicencesLogger)(implicit ec: ExecutionContext)
  extends UsageController(cc, validateAndExtractHeadersAction, connector, serviceConfigProvider, logger, LateUsage) {

  def post(): Action[AnyContent] = {
    super.process()
  }
}
