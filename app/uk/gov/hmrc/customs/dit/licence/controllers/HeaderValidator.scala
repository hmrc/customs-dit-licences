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
import play.api.http.MimeTypes.XML
import play.api.mvc.{Headers, Request}
import play.mvc.Http.Status.UNAUTHORIZED
import uk.gov.hmrc.customs.api.common.controllers.ErrorResponse
import uk.gov.hmrc.customs.api.common.controllers.ErrorResponse._
import uk.gov.hmrc.customs.api.common.logging.CdsLogger
import uk.gov.hmrc.customs.dit.licence.controllers.CustomHeaderNames.X_CORRELATION_ID_HEADER_NAME
import uk.gov.hmrc.customs.dit.licence.model.RequestData
import uk.gov.hmrc.customs.dit.licence.services.ConfigService

@Singleton
class HeaderValidator @Inject() (configService: ConfigService, logger: CdsLogger) {

  private val basicAuthTokenScheme = "Basic "
  private lazy val basicAuthToken: String = configService.basicAuthTokenInternal
  private lazy val ErrorUnauthorized = ErrorResponse(UNAUTHORIZED, UnauthorizedCode, "Basic token is missing or not authorized")
  private lazy val ErrorXCorrelationIdMissingOrInvalid = errorBadRequest("X-Correlation-ID is missing or invalid")
  private lazy val xCorrelationIdRegex = "^[A-Za-z0-9-]{36}$".r
  private lazy val validAcceptHeaders = Seq(XML)
  private lazy val validContentTypeHeaders = Seq(s"$XML;charset=utf-8", s"$XML; charset=utf-8")

  def validateHeaders[A](implicit request: Request[A]): Either[ErrorResponse, RequestData] = {
    implicit val headers = request.headers

    def hasAccept = validateHeader(ACCEPT, validAcceptHeaders.contains(_), ErrorAcceptHeaderInvalid)
    def hasContentType = validateHeader(CONTENT_TYPE, s => validContentTypeHeaders.contains(s.toLowerCase()), ErrorContentTypeHeaderInvalid)
    def hasAuth(basicAuthToken: String) = validateHeader(AUTHORIZATION, _ == basicAuthTokenScheme + basicAuthToken, ErrorUnauthorized)
    def hasXCorrelationId = validateHeader(X_CORRELATION_ID_HEADER_NAME, xCorrelationIdRegex.findFirstIn(_).nonEmpty, ErrorXCorrelationIdMissingOrInvalid)

    val theResult: Either[ErrorResponse, RequestData] = for {
      accept <- hasAccept.right
      contentType <- hasContentType.right
      auth <- hasAuth(basicAuthToken).right
      xCorrelationId <- hasXCorrelationId.right
    } yield {
      logger.debug(
        s"\n$ACCEPT header passed validation: $accept\n"
          + s"$CONTENT_TYPE header passed validation: $contentType\n"
          + s"$AUTHORIZATION header passed validation: $auth\n"
          + s"$X_CORRELATION_ID_HEADER_NAME header passed validation: $xCorrelationId\n")
      RequestData(xCorrelationId)
    }
    theResult
  }

  private def validateHeader(headerName: String, rule: String => Boolean, errorResponse: ErrorResponse)(implicit h: Headers): Either[ErrorResponse, String] = {
    val left = Left(errorResponse)
    def leftWithLog = {
      logger.error(s"${errorResponse.message} ")
      left
    }
    def leftWithLogContainingValue(s: String) = {
      logger.error(s"${errorResponse.message} '$s'")
      left
    }

    h.get(headerName).fold[Either[ErrorResponse, String]]{
      leftWithLog
    }{
      v => if (rule(v)) Right(v) else leftWithLogContainingValue(v)
    }
  }
}
