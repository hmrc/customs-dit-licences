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
import play.api.mvc._
import play.mvc.Http.Status.UNAUTHORIZED
import uk.gov.hmrc.customs.api.common.controllers.ErrorResponse
import uk.gov.hmrc.customs.api.common.controllers.ErrorResponse.UnauthorizedCode
import uk.gov.hmrc.customs.dit.licence.logging.LicencesLogger
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.HeaderCarrierConverter

import scala.concurrent.Future

@Singleton
class ValidateAndExtractHeadersAction @Inject()(validator: HeaderValidator,
                                                logger: LicencesLogger) extends ActionRefiner[Request, ValidatedRequest] {

  override def refine[A](inputRequest: Request[A]): Future[Either[Result, ValidatedRequest[A]]] = Future.successful {
    implicit val r = inputRequest
    implicit def hc(implicit rh: RequestHeader): HeaderCarrier = HeaderCarrierConverter.fromHeadersAndSession(rh.headers)

    val headers = inputRequest.headers.toSimpleMap
    validator.validateHeaders(headers) match {
      case Right(_) =>
        logger.debug("Passed header validation", headers.toSeq)
        Right(ValidatedRequest(headers, inputRequest))
      case Left(errorResponse) =>
        logger.error(s"Failed header validation: ${errorResponse.message}", headers.toSeq)
        Left(errorResponse.XmlResult)
    }
  }

}

@Singleton
class ValidateRequestPayloadAction @Inject()(logger: LicencesLogger) extends ActionFilter[ValidatedRequest] {

  private val actionName = this.getClass.getSimpleName
  private lazy val ErrorUnauthorized = ErrorResponse(UNAUTHORIZED, UnauthorizedCode, "Payload is not well-formed XML")

  //TODO probably not needed as framework catches malformed xml
  def filter[A](validatedRequest: ValidatedRequest[A]): Future[Option[Result]] = Future.successful {
    implicit val r = validatedRequest.asInstanceOf[ValidatedRequest[AnyContent]]
    implicit def hc(implicit rh: RequestHeader): HeaderCarrier = HeaderCarrierConverter.fromHeadersAndSession(rh.headers)

    logger.debug(s"Entered $actionName", validatedRequest.headers.headers, validatedRequest.body.toString)

    r.body.asXml match {
      case Some(_) =>
        logger.debug(s"passed request payload well-formedness check in $actionName")
        None
      case None =>
//        logger.error(s"Failed request payload well-formedness check in $actionName")
        Some(ErrorUnauthorized.XmlResult)
    }
  }

}

