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

package unit.controllers

import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar
import org.scalatest.prop.TableDrivenPropertyChecks
import play.api.http.HeaderNames
import play.api.http.HeaderNames._
import play.api.mvc.{AnyContent, Headers, Request}
import play.api.test.Helpers.CONTENT_TYPE
import uk.gov.hmrc.customs.api.common.controllers.ErrorResponse.{ErrorAcceptHeaderInvalid, ErrorContentTypeHeaderInvalid}
import uk.gov.hmrc.customs.api.common.logging.CdsLogger
import uk.gov.hmrc.customs.dit.licence.controllers.HeaderValidator
import uk.gov.hmrc.customs.dit.licence.services.LicenceConfigService
import uk.gov.hmrc.play.test.UnitSpec
import util.RequestHeaders._
import util.TestData.{ErrorUnauthorizedBasicToken, ErrorXCorrelationIdMissingOrInvalid, TestRequestData}

class HeaderValidatorSpec extends UnitSpec with TableDrivenPropertyChecks with MockitoSugar {

  trait Setup {
    val mockLogger = mock[CdsLogger]
    implicit val request = mock[Request[AnyContent]]
    val mockConfigService = mock[LicenceConfigService]
    val validator = new HeaderValidator (mockConfigService, mockLogger)

    when(mockConfigService.basicAuthTokenInternal).thenReturn(AuthHeaderTokenInternal)
  }

  val headersTable =
    Table(
      ("description", "Headers", "Expected response"),
      ("Valid Headers", ValidHeaders, Right(TestRequestData)),
      ("Valid content type XML with no space header", ValidHeaders + (CONTENT_TYPE -> "application/xml;charset=utf-8"), Right(TestRequestData)),
      ("Missing accept header", ValidHeaders - ACCEPT, Left(ErrorAcceptHeaderInvalid)),
      ("Missing content type header", ValidHeaders - CONTENT_TYPE, Left(ErrorContentTypeHeaderInvalid)),
      ("Missing X-Correlation-ID header", ValidHeaders - XCorrelationIdHeaderName, Left(ErrorXCorrelationIdMissingOrInvalid)),
      ("Missing auth header", ValidHeaders - HeaderNames.AUTHORIZATION, Left(ErrorUnauthorizedBasicToken)),
      ("Invalid accept header", ValidHeaders + AcceptHeaderInvalid, Left(ErrorAcceptHeaderInvalid)),
      ("Invalid content type header", ValidHeaders + ContentTypeHeaderInvalid, Left(ErrorContentTypeHeaderInvalid)),
      ("Invalid content type XML without UTF-8 header", ValidHeaders + (CONTENT_TYPE -> "application/xml"), Left(ErrorContentTypeHeaderInvalid)),
      ("Invalid X-Correlation-ID header", ValidHeaders + XCorrelationIdHeaderInvalid, Left(ErrorXCorrelationIdMissingOrInvalid)),
      ("Invalid auth header", ValidHeaders + AuthHeaderInternalInvalid, Left(ErrorUnauthorizedBasicToken))
    )

  "HeaderValidatorAction" should {
    forAll(headersTable) { (description, headers, response) =>
      s"$description" in new Setup {
        when(request.headers).thenReturn(new Headers(headers.toSeq))

        validator.validateHeaders(request) shouldBe response
      }
    }
  }
}
