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

import com.google.inject.Inject
import javax.inject.Singleton
import uk.gov.hmrc.customs.api.common.logging.CdsLogger
import uk.gov.hmrc.customs.dit.licence.logging.LoggingHelper._
import uk.gov.hmrc.customs.dit.licence.logging.model.SeqOfHeader
import uk.gov.hmrc.http.HeaderCarrier

@Singleton
class LicencesLogger @Inject()(logger: CdsLogger) {

  def debug(msg: => String): Unit = logger.debug(formatDebug(msg))
  def debug(msg: => String, headers: => SeqOfHeader): Unit = logger.debug(formatDebug(msg, headers))
  def debug(msg: => String, headers: => SeqOfHeader, payload: => String): Unit =  logger.debug(formatDebug(msg, headers, Some(payload)))
  def info(msg: => String, headers: => SeqOfHeader): Unit = logger.info(formatInfo(msg, headers))
  def error(msg: => String, headers: => SeqOfHeader, e: => Throwable): Unit = logger.error(formatError(msg, headers), e)
  def error(msg: => String, headers: => SeqOfHeader): Unit = logger.error(formatError(msg, headers))
  def errorWithoutHeaderCarrier(msg: => String): Unit = logger.error(msg)
}
