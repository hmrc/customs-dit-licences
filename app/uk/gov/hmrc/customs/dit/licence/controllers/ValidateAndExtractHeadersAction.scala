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

import javax.inject.Inject
import play.api.mvc.{ActionRefiner, Request, Result}
import uk.gov.hmrc.customs.dit.licence.logging.LicencesLogger
import uk.gov.hmrc.customs.dit.licence.model.ValidatedRequest

import scala.concurrent.Future

class ValidateAndExtractHeadersAction @Inject()(validator: HeaderValidator,
                                                logger: LicencesLogger) extends ActionRefiner[Request, ValidatedRequest] {

  protected override def refine[A](inputRequest: Request[A]): Future[Either[Result, ValidatedRequest[A]]] = Future.successful {
    implicit val r: Request[A] = inputRequest

    validator.validateHeaders match {
      case Right(requestData) =>
        Right(ValidatedRequest(requestData, inputRequest))
      case Left(errorResponse) =>
        Left(errorResponse.XmlResult)
    }
  }
}
