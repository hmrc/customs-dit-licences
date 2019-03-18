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

package uk.gov.hmrc.customs.dit.licence.connectors

import com.google.inject.Inject
import javax.inject.Singleton
import play.api.http.HeaderNames.{ACCEPT, CONTENT_TYPE}
import play.api.http.MimeTypes
import play.api.libs.json.Json
import play.api.mvc.AnyContent
import uk.gov.hmrc.customs.dit.licence.logging.LicencesLogger
import uk.gov.hmrc.customs.dit.licence.model.{PublicNotificationRequest, PublicNotificationResponse, ValidatedRequest}
import uk.gov.hmrc.customs.dit.licence.services.LicenceConfigService
import uk.gov.hmrc.http.{HeaderCarrier, HttpException}
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PublicNotificationServiceConnector @Inject()(http: HttpClient,
                                                   logger: LicencesLogger,
                                                   config: LicenceConfigService)(implicit ec: ExecutionContext) {

  private val outboundHeaders = Seq(
    (ACCEPT, MimeTypes.JSON),
    (CONTENT_TYPE, MimeTypes.JSON))

  def send(publicNotificationRequest: PublicNotificationRequest)(implicit validatedRequest: ValidatedRequest[AnyContent]): Future[PublicNotificationResponse] = {

    implicit val hc: HeaderCarrier = HeaderCarrier(extraHeaders = outboundHeaders)
    val msg = "Calling public notification service"
    val url = config.publicNotificationUrl
    val payloadAsJsonString = Json.prettyPrint(Json.toJson(publicNotificationRequest))
    logger.debug(s"$msg at $url with\nheaders=${hc.headers} and\npayload=$payloadAsJsonString publicNotificationRequest")

    val postFuture = http
      .POST[PublicNotificationRequest, PublicNotificationResponse](url, publicNotificationRequest)
      .recoverWith {
        case httpError: HttpException => Future.failed(new RuntimeException(httpError))
      }
      .recoverWith {
        case e: Throwable =>
          logger.error(s"Call to public notification service failed. POST url=$url", e)
          Future.failed(e)
      }
    postFuture
  }
}
