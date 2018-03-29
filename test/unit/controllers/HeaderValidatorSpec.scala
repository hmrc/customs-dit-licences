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

package unit.controllers

import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar
import org.scalatest.prop.TableDrivenPropertyChecks
import play.api.http.HeaderNames
import play.api.http.HeaderNames._
import play.api.test.Helpers.CONTENT_TYPE
import uk.gov.hmrc.customs.api.common.controllers.ErrorResponse.{ErrorAcceptHeaderInvalid, ErrorContentTypeHeaderInvalid}
import uk.gov.hmrc.customs.dit.licence.controllers.HeaderValidator
import uk.gov.hmrc.customs.dit.licence.logging.LicencesLogger
import uk.gov.hmrc.customs.dit.licence.services.ConfigService
import uk.gov.hmrc.play.test.UnitSpec
import util.RequestHeaders._
import util.TestData.{ErrorUnauthorizedBasicToken, ErrorXCorrelationIdMissingOrInvalid}

class HeaderValidatorSpec extends UnitSpec with TableDrivenPropertyChecks with MockitoSugar {

  private val mockLogger = mock[LicencesLogger]
  private val mockConfigService = mock[ConfigService]
  private val validator = new HeaderValidator (mockConfigService, mockLogger)

  val headersTable =
    Table(
      ("description", "Headers", "Expected response"),
      ("Valid Headers", ValidHeaders, Right(())),
      ("Valid content type XML with no space header", ValidHeaders + (CONTENT_TYPE -> "application/xml;charset=utf-8"), Right(())),
      ("Missing accept header", ValidHeaders - ACCEPT, Left(ErrorAcceptHeaderInvalid)),
      ("Missing content type header", ValidHeaders - CONTENT_TYPE, Left(ErrorContentTypeHeaderInvalid)),
      ("Missing X-Correlation-ID header", ValidHeaders - X_CORRELATION_ID_NAME, Left(ErrorXCorrelationIdMissingOrInvalid)),
      ("Missing auth header", ValidHeaders - HeaderNames.AUTHORIZATION, Left(ErrorUnauthorizedBasicToken)),
      ("Invalid accept header", ValidHeaders + ACCEPT_HEADER_INVALID, Left(ErrorAcceptHeaderInvalid)),
      ("Invalid content type header", ValidHeaders + CONTENT_TYPE_HEADER_INVALID, Left(ErrorContentTypeHeaderInvalid)),
      ("Invalid content type XML without UTF-8 header", ValidHeaders + (CONTENT_TYPE -> "application/xml"), Left(ErrorContentTypeHeaderInvalid)),
      ("Invalid X-Correlation-ID header", ValidHeaders + X_CORRELATION_ID_HEADER_INVALID, Left(ErrorXCorrelationIdMissingOrInvalid)),
      ("Invalid auth header", ValidHeaders + AUTH_HEADER_INTERNAL_INVALID, Left(ErrorUnauthorizedBasicToken))
    )

  "HeaderValidatorAction" should {
    forAll(headersTable) { (description, headers, response) =>
      s"$description" in {
        when(mockConfigService.basicAuthTokenInternal).thenReturn(AUTH_HEADER_TOKEN_INTERNAL)

        validator.validateHeaders(headers) shouldBe response
      }
    }
  }
}
