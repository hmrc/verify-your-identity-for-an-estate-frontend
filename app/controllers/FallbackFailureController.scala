/*
 * Copyright 2024 HM Revenue & Customs
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

package controllers

import handlers.ErrorHandler
import play.api.Logging
import play.api.i18n.I18nSupport
import utils.Session
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class FallbackFailureController @Inject()(
                                           val controllerComponents: MessagesControllerComponents,
                                           errorHandler: ErrorHandler
                                         )(implicit val ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Logging{

  def onPageLoad: Action[AnyContent] = Action.async { implicit request =>
    val errorMessage = s"[Verifying][Estates IV][Session ID: ${Session.id(hc)}] Estates IV encountered a problem that could not be recovered from"
    logger.error(errorMessage)
    errorHandler.internalServerErrorTemplate.map(html => (InternalServerError(html)))
    }
  }
