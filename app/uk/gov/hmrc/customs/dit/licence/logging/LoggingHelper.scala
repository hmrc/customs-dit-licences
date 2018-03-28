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

package uk.gov.hmrc.customs.dit.licence.logging

import play.api.http.HeaderNames.AUTHORIZATION
import uk.gov.hmrc.customs.dit.licence.logging.model.SeqOfHeader
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.customs.dit.licence.controllers.CustomHeaderNames.X_CORRELATION_ID_HEADER_NAME

object LoggingHelper {

  private val SIGNIFICANT_HEADERS = Map(
    X_CORRELATION_ID_HEADER_NAME -> "correlationId"
  )

  private val headerOverwriteValue = "value-not-logged"
  private val headersToOverwrite = Set(AUTHORIZATION)

  def formatError(msg: String, headers: SeqOfHeader): String = {
    formatInfo(msg, headers)
  }

  def formatInfo(msg: String, headers: SeqOfHeader): String = {
    s"${formatSignificantHeaders(headers)} $msg"
  }

  def formatDebug(msg: String, headers: SeqOfHeader = Seq.empty, maybePayload: Option[String] = None): String = {
    val payloadPart = maybePayload.fold("")(payload => s"\npayload=\n$payload")
    s"${formatSignificantHeaders(headers)} $msg \nheaders=${overwriteHeaderValues(headers, headersToOverwrite)}$payloadPart"
  }

  private def formatSingleHeader(headers: SeqOfHeader, headerName: String, headerHumanReadableName: String): String = {
    val maybeHeaderValue = findHeaderValue(headerName, headers)

    maybeHeaderValue.fold("")(value => s"[$headerHumanReadableName=$value]")
  }

  private def formatSignificantHeaders(headers: SeqOfHeader): String = {
    SIGNIFICANT_HEADERS.map { case (k, v) => formatSingleHeader(headers, k, v) }.mkString
  }

  private def findHeaderValue(headerName: String, headers: SeqOfHeader): Option[String] = {
    headers.collectFirst {
      case (`headerName`, headerValue) => headerValue
    }
  }

  private def overwriteHeaderValues(headers: SeqOfHeader, overwrittenHeaderNames: Set[String]): SeqOfHeader = {
    headers map {
      case (rewriteHeader, _) if overwrittenHeaderNames.contains(rewriteHeader) => rewriteHeader -> headerOverwriteValue
      case header => header
    }
  }
}
