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
import play.api.http.HeaderNames._
import play.api.http.MimeTypes
import play.mvc.Http.Status.UNAUTHORIZED
import uk.gov.hmrc.customs.api.common.controllers.ErrorResponse
import uk.gov.hmrc.customs.api.common.controllers.ErrorResponse._
import uk.gov.hmrc.customs.dit.licence.logging.LicencesLogger
import uk.gov.hmrc.customs.dit.licence.logging.model.HeaderMap
import uk.gov.hmrc.customs.dit.licence.services.ConfigService

@Singleton
class HeaderValidator @Inject() (configService: ConfigService, logger: LicencesLogger) {

  private val basicAuthTokenScheme = "Basic "
  private lazy val basicAuthToken: String = configService.basicAuthToken
  private lazy val ErrorUnauthorized = ErrorResponse(UNAUTHORIZED, UnauthorizedCode, "Basic token is missing or not authorized")
  private lazy val ErrorXCorrelationIdMissingOrInvalid = errorBadRequest("X-Correlation-ID is missing or invalid")
  private lazy val xCorrelationIdRegex = "^[A-Za-z0-9-]{36}$".r

  def validateHeaders[A](headers: HeaderMap): Either[ErrorResponse, Unit] = {

    if (!hasContentType(headers)) {
      Left(ErrorContentTypeHeaderInvalid)
    } else if (!hasAccept(headers)) {
      Left(ErrorAcceptHeaderInvalid)
    } else if (!hasAuth(headers, basicAuthToken)) {
      Left(ErrorUnauthorized)
    } else if (!hasXCorrelationId(headers)) {
      Left(ErrorXCorrelationIdMissingOrInvalid)
    } else {
      Right(())
    }
  }

  private lazy val validAcceptHeaders = Seq("application/xml")
  private lazy val validContentTypeHeaders = Seq(MimeTypes.XML + ";charset=utf-8", MimeTypes.XML + "; charset=utf-8")

  private def hasAccept(h: HeaderMap) = h.get(ACCEPT).fold(false)(validAcceptHeaders.contains(_))
  private def hasContentType(h: HeaderMap) = h.get(CONTENT_TYPE).fold(false)(h => validContentTypeHeaders.contains(h.toLowerCase()))
  private def hasAuth(h: HeaderMap, basicAuthToken: String) = h.get(AUTHORIZATION).fold(false)(_ == basicAuthTokenScheme + basicAuthToken)
  private def hasXCorrelationId(h: HeaderMap) = h.get(CustomHeaderNames.X_CORRELATION_ID_HEADER_NAME).fold(false)(xCorrelationIdRegex.findFirstIn(_).nonEmpty)
}
