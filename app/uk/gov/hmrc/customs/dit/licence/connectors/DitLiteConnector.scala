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

import java.util.UUID

import javax.inject.{Inject, Singleton}
import play.api.http.HeaderNames.{ACCEPT, CONTENT_TYPE, DATE}
import play.api.http.MimeTypes
import uk.gov.hmrc.customs.api.common.config.ServiceConfigProvider
import uk.gov.hmrc.customs.dit.licence.logging.LicencesLogger
import uk.gov.hmrc.customs.dit.licence.services.{DateTimeService, WSHttp}
import uk.gov.hmrc.http.logging.Authorization
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.xml.NodeSeq

@Singleton
class DitLiteConnector @Inject()(wsHttp: WSHttp,
                                 serviceConfigProvider: ServiceConfigProvider,
                                 dateTimeService: DateTimeService,
                                 logger: LicencesLogger) {

  def post(body: NodeSeq, correlationId: UUID, configKey: String): Future[HttpResponse] = {

    val config = Option(serviceConfigProvider.getConfig(configKey)).getOrElse(throw new IllegalArgumentException("config not found"))
    val basicToken = "Basic " + config.bearerToken.getOrElse(throw new IllegalStateException("no basic token was found in config"))

    implicit val hc: HeaderCarrier = HeaderCarrier(extraHeaders = getHeaders(correlationId), authorization = Some(Authorization(basicToken)))

    logger.debug(s"calling DIT-LITE at ${config.url}", hc.headers, body.toString())
    wsHttp.POSTString(config.url, body.toString())
  }

  private def getHeaders(correlationId: UUID) = {
    Seq(
      (ACCEPT, MimeTypes.XML),
      (CONTENT_TYPE, MimeTypes.XML + "; charset=UTF-8"),
      (DATE, dateTimeService.nowUtc().toString("EEE, dd MMM yyyy HH:mm:ss z")),
      ("X-Correlation-ID", correlationId.toString))
  }
}
