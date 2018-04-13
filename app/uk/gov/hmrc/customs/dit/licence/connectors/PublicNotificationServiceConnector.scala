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

import javax.inject.Singleton

import com.google.inject.Inject
import play.api.http.HeaderNames.{ACCEPT, CONTENT_TYPE}
import play.api.http.MimeTypes
import play.api.mvc.AnyContent
import uk.gov.hmrc.customs.api.common.config.ServiceConfigProvider
import uk.gov.hmrc.customs.dit.licence.logging.LicencesLogger
import uk.gov.hmrc.customs.dit.licence.model.{PublicNotificationRequest, PublicNotificationResponse, ValidatedRequest}
import uk.gov.hmrc.customs.dit.licence.services.WSHttp
import uk.gov.hmrc.http.{HeaderCarrier, HttpException}
import uk.gov.hmrc.play.config.inject.AppName

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class PublicNotificationServiceConnector @Inject()(httpPost: WSHttp,
                                                   appName: AppName,
                                                   logger: LicencesLogger,
                                                   serviceConfigProvider: ServiceConfigProvider) {

  private val outboundHeaders = Seq(
    (ACCEPT, MimeTypes.JSON),
    (CONTENT_TYPE, MimeTypes.JSON))

  def send(publicNotificationRequest: PublicNotificationRequest)(implicit validatedRequest: ValidatedRequest[AnyContent]): Future[PublicNotificationResponse] = {
    val url = serviceConfigProvider.getConfig("public-notification").url

    implicit val hc: HeaderCarrier = HeaderCarrier(extraHeaders = outboundHeaders)
    val msg = "Calling public notification service"
    logger.debug(s"$msg at $url with\nheaders=[${hc.headers}] and\npayload=$publicNotificationRequest") //TODO: json pretty print

    val postFuture = httpPost
      .POST[PublicNotificationRequest, PublicNotificationResponse](url, publicNotificationRequest)
      .recoverWith {
        case httpError: HttpException => Future.failed(new RuntimeException(httpError))
      }
      .recoverWith {
        case e: Throwable =>
          logger.error(s"Call to public notification service failed. POST url=$url")
          Future.failed(e)
      }
    postFuture
  }
}
