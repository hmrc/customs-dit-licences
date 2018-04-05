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

package uk.gov.hmrc.customs.dit.licence.connectors

import javax.inject.{Inject, Singleton}
import play.api.http.HeaderNames.{ACCEPT, CONTENT_TYPE}
import play.api.http.MimeTypes.XML
import play.api.mvc.AnyContent
import uk.gov.hmrc.customs.api.common.config.ServiceConfigProvider
import uk.gov.hmrc.customs.dit.licence.logging.LicencesLogger
import uk.gov.hmrc.customs.dit.licence.model.{RequestData, ValidatedRequest}
import uk.gov.hmrc.customs.dit.licence.services.WSHttp
import uk.gov.hmrc.http.logging.Authorization
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class DitLiteConnector @Inject()(wsHttp: WSHttp,
                                 serviceConfigProvider: ServiceConfigProvider,
                                 logger: LicencesLogger) extends RawResponseReads {

  def post(configKey: String)(implicit validatedRequest: ValidatedRequest[AnyContent]): Future[HttpResponse] = {

    val config = Option(serviceConfigProvider.getConfig(configKey)).getOrElse(throw new IllegalArgumentException("config not found"))
    val basicToken = "Basic " + config.bearerToken.getOrElse(throw new IllegalStateException("no basic token was found in config"))

    implicit val hc: HeaderCarrier = HeaderCarrier(extraHeaders = getHeaders(validatedRequest.requestData), authorization = Some(Authorization(basicToken)))

    logger.debug(s"calling DIT-LITE at ${config.url} with\nheaders=[${hc.headers}] and\npayload=[${validatedRequest.request.body.asXml.get.toString}]")
    wsHttp.POSTString(config.url, validatedRequest.request.body.asXml.get.toString)
  }

  private def getHeaders(requestData: RequestData) = {
    Seq(
      (ACCEPT, XML),
      (CONTENT_TYPE, s"$XML; charset=UTF-8"),
      ("X-Correlation-ID", requestData.correlationId))
  }
}
