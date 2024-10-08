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

import config.FrontendAppConfig
import controllers.actions._
import javax.inject.Inject
import models.NormalMode
import pages.{IsAgentManagingEstatePage, UtrPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.{RelationshipEstablishment, RelationshipFound, RelationshipNotFound}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.{IvSuccessView, IvSuccessWithoutPlaybackView}

import scala.concurrent.{ExecutionContext, Future}

class IvSuccessController @Inject()(
                                     override val messagesApi: MessagesApi,
                                     identify: IdentifierAction,
                                     getData: DataRetrievalAction,
                                     requireData: DataRequiredAction,
                                     val controllerComponents: MessagesControllerComponents,
                                     relationshipEstablishment: RelationshipEstablishment,
                                     withPlaybackView: IvSuccessView,
                                     withoutPlaybackView: IvSuccessWithoutPlaybackView
                                   )(implicit ec: ExecutionContext, val config: FrontendAppConfig)
  extends FrontendBaseController with I18nSupport {

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      request.userAnswers.get(UtrPage).map { utr =>

        def onRelationshipFound = {

            val isAgentManagingEstate = request.userAnswers.get(IsAgentManagingEstatePage) match {
              case None => false
              case Some(value) => value
            }

            if (config.playbackEnabled) {
              Future.successful(Ok(withPlaybackView(isAgentManagingEstate, utr)))
            } else {
              Future.successful(Ok(withoutPlaybackView(utr)))
            }
        }

        lazy val onRelationshipNotFound = Future.successful(Redirect(controllers.routes.IsAgentManagingEstateController.onPageLoad(NormalMode)))

        relationshipEstablishment.check(request.internalId, utr) flatMap {
          case RelationshipFound =>
            onRelationshipFound
          case RelationshipNotFound =>
            onRelationshipNotFound
        }

      } getOrElse Future.successful(Redirect(controllers.routes.SessionExpiredController.onPageLoad))

  }

  def onSubmit: Action[AnyContent] = Action { _ =>
    Redirect(config.estatesContinueUrl)
  }

}
