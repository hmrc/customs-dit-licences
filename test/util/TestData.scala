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

package util

import java.util.UUID

import play.api.http.HeaderNames.{ACCEPT, CONTENT_TYPE}
import play.api.http.{HeaderNames, MimeTypes}
import play.api.mvc.AnyContentAsXml
import play.api.test.FakeRequest
import play.mvc.Http.Status.UNAUTHORIZED
import uk.gov.hmrc.customs.api.common.controllers.ErrorResponse
import uk.gov.hmrc.customs.api.common.controllers.ErrorResponse.{UnauthorizedCode, errorBadRequest}
import util.RequestHeaders._
import util.TestData._

import scala.xml.Elem

object TestData {

  val correlationIdValue = "e61f8eee-812c-4b8f-b193-06aedc60dca2"
  val correlationIdUuid: UUID = UUID.fromString(correlationIdValue)

  val ValidXML: Elem = <some>xml</some>

  lazy val ValidRequest: FakeRequest[AnyContentAsXml] = FakeRequest()
    .withHeaders(ValidHeaders.toSeq: _*)
    .withXmlBody(ValidXML)

  lazy val InvalidRequestWithoutXCorrelationId: FakeRequest[AnyContentAsXml] = ValidRequest
    .copyFakeRequest(headers =
      ValidRequest.headers.remove(X_CORRELATION_ID_NAME))

  lazy val ErrorXCorrelationIdMissingOrInvalid = errorBadRequest("X-Correlation-ID is missing or invalid")
  lazy val ErrorUnauthorizedBadXml = ErrorResponse(UNAUTHORIZED, UnauthorizedCode, "Payload is not well-formed XML")
  lazy val ErrorUnauthorizedBasicToken = ErrorResponse(UNAUTHORIZED, UnauthorizedCode, "Basic token is missing or not authorized")

}

object RequestHeaders {

  val CONTENT_TYPE_HEADER: (String, String) = CONTENT_TYPE -> (MimeTypes.XML + "; charset=UTF-8")
  val CONTENT_TYPE_HEADER_INVALID: (String, String) = CONTENT_TYPE -> "somethinginvalid"

  val ACCEPT_HEADER: (String, String) = ACCEPT -> "application/xml"
  val ACCEPT_HEADER_INVALID: (String, String) = ACCEPT -> "invalid"

  val AUTH_HEADER_TOKEN = "dummy-token"
  val AUTH_HEADER_VALUE: String = s"Basic $AUTH_HEADER_TOKEN"
  val AUTH_HEADER: (String, String) = HeaderNames.AUTHORIZATION -> AUTH_HEADER_VALUE
  val AUTH_HEADER_INVALID: (String, String) = HeaderNames.AUTHORIZATION -> "some-invalid-auth"

  val X_CORRELATION_ID_NAME = "X-Correlation-ID"
  val X_CORRELATION_ID_HEADER: (String, String) = X_CORRELATION_ID_NAME -> correlationIdUuid.toString
  val X_CORRELATION_ID_HEADER_INVALID: (String, String) = X_CORRELATION_ID_NAME -> "invalid-uuid"

  val LoggingHeaders = Seq(X_CORRELATION_ID_HEADER)

  val ValidHeaders = Map(
    CONTENT_TYPE_HEADER,
    ACCEPT_HEADER,
    AUTH_HEADER,
    X_CORRELATION_ID_HEADER)

  val InvalidHeaders: Map[String, String] = ValidHeaders - X_CORRELATION_ID_NAME

}

