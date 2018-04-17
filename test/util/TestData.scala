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

import play.api.http.HeaderNames
import play.api.http.HeaderNames.{ACCEPT, CONTENT_TYPE}
import play.api.http.MimeTypes.XML
import play.api.mvc.{AnyContent, AnyContentAsText, AnyContentAsXml}
import play.api.test.FakeRequest
import play.mvc.Http.Status.UNAUTHORIZED
import uk.gov.hmrc.customs.api.common.controllers.ErrorResponse
import uk.gov.hmrc.customs.api.common.controllers.ErrorResponse.{UnauthorizedCode, errorBadRequest}
import uk.gov.hmrc.customs.dit.licence.model.{RequestData, ValidatedRequest}
import util.CustomsDitLiteExternalServicesConfig.{DitLiteEntryUsageServiceContext, DitLiteLateUsageServiceContext}
import util.RequestHeaders._
import util.TestData._

import scala.xml.NodeSeq

object TestData {

  val CorrelationId = "e61f8eee-812c-4b8f-b193-06aedc60dca2"
  lazy val TestValidatedRequest: ValidatedRequest[AnyContent] = ValidatedRequest[AnyContent](TestRequestData, ValidRequest)

  type EmulatedServiceFailure = UnsupportedOperationException
  val emulatedServiceFailure = new EmulatedServiceFailure("Emulated service failure.")

  val ValidXML: NodeSeq = <some>xml</some>

  lazy val ValidRequest: FakeRequest[AnyContentAsXml] = FakeRequest()
    .withHeaders(ValidHeaders.toSeq: _*)
    .withXmlBody(ValidXML)

  lazy val InvalidRequestWithoutXCorrelationId: FakeRequest[AnyContentAsXml] =
    ValidRequest.copyFakeRequest(headers = ValidRequest.headers.remove(XCorrelationIdHeaderName))

  lazy val MalformedXmlRequest: FakeRequest[AnyContentAsText] = ValidRequest.withTextBody("<xml><non_well_formed><xml>")

  lazy val ErrorXCorrelationIdMissingOrInvalid = errorBadRequest("X-Correlation-ID is missing or invalid")
  lazy val ErrorUnauthorizedBasicToken = ErrorResponse(UNAUTHORIZED, UnauthorizedCode, "Basic token is missing or not authorized")

  val TestRequestData = RequestData(CorrelationId)
  private val protocol = "http"

  val conf: Map[String, Any] = Map(
    "microservice.services.public-notification.host" -> ExternalServicesConfig.Host,
    "microservice.services.public-notification.port" -> ExternalServicesConfig.Port,
    "microservice.services.public-notification.context" -> CustomsDitLiteExternalServicesConfig.PublicNotificationServiceContext,

    "microservice.services.dit-lite-entry-usage.protocol" -> protocol,
    "microservice.services.dit-lite-entry-usage.host" -> ExternalServicesConfig.Host,
    "microservice.services.dit-lite-entry-usage.port" -> ExternalServicesConfig.Port,
    "microservice.services.dit-lite-entry-usage.context" -> DitLiteEntryUsageServiceContext,
    "microservice.services.dit-lite-entry-usage.bearer-token" -> ExternalServicesConfig.AuthToken,
    "microservice.services.dit-lite-late-usage.protocol" -> protocol,
    "microservice.services.dit-lite-late-usage.host" -> ExternalServicesConfig.Host,
    "microservice.services.dit-lite-late-usage.port" -> ExternalServicesConfig.Port,
    "microservice.services.dit-lite-late-usage.context" -> DitLiteLateUsageServiceContext,
    "microservice.services.dit-lite-late-usage.bearer-token" -> ExternalServicesConfig.AuthToken,
    "auditing.enabled" -> false
  )
}

object RequestHeaders {

  val ContentTypeHeader: (String, String) = CONTENT_TYPE -> s"$XML; charset=UTF-8"
  val ContentTypeHeaderInvalid: (String, String) = CONTENT_TYPE -> "somethinginvalid"

  val AcceptHeader: (String, String) = ACCEPT -> "application/xml"
  val AcceptHeaderInvalid: (String, String) = ACCEPT -> "invalid"

  val AuthHeaderTokenInternal = "dummy-token-internal"
  val AuthHeaderValueInternal: String = s"Basic $AuthHeaderTokenInternal"
  val AuthHeaderInternal: (String, String) = HeaderNames.AUTHORIZATION -> AuthHeaderValueInternal
  val AuthHeaderInternalInvalid: (String, String) = HeaderNames.AUTHORIZATION -> "some-invalid-auth-internal"

  val XCorrelationIdHeaderName = "X-Correlation-ID"
  val XCorrelationIdHeader: (String, String) = XCorrelationIdHeaderName -> CorrelationId
  val XCorrelationIdHeaderInvalid: (String, String) = XCorrelationIdHeaderName -> "invalid-uuid"

  val ValidHeaders = Map(
    ContentTypeHeader,
    AcceptHeader,
    AuthHeaderInternal,
    XCorrelationIdHeader)

  val InvalidHeaders: Map[String, String] = ValidHeaders - XCorrelationIdHeaderName

}
