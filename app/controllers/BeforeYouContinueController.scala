/*
 * Copyright 2026 HM Revenue & Customs
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
import connectors.EstatesStoreConnector
import controllers.actions._
import javax.inject.Inject
import models.EstatesStoreRequest
import pages.{IsAgentManagingEstatePage, UtrPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.{RelationshipEstablishment, RelationshipFound, RelationshipNotFound}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.BeforeYouContinueView

import scala.concurrent.{ExecutionContext, Future}

class BeforeYouContinueController @Inject()(
                                             override val messagesApi: MessagesApi,
                                             identify: IdentifierAction,
                                             relationship: RelationshipEstablishment,
                                             getData: DataRetrievalAction,
                                             requireData: DataRequiredAction,
                                             val controllerComponents: MessagesControllerComponents,
                                             view: BeforeYouContinueView,
                                             connector: EstatesStoreConnector
                                           )(implicit ec: ExecutionContext, config: FrontendAppConfig) extends FrontendBaseController with I18nSupport {

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

        request.userAnswers.get(UtrPage) map { utr =>

        relationship.check(request.internalId, utr) flatMap {
          case RelationshipFound =>
            Future.successful(Redirect(controllers.routes.IvSuccessController.onPageLoad))
          case RelationshipNotFound =>
            Future.successful(Ok(view(utr)))
        }

      } getOrElse Future.successful(Redirect(controllers.routes.SessionExpiredController.onPageLoad))
  }

  def onSubmit: Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      (for {
        utr <- request.userAnswers.get(UtrPage)
        isManagedByAgent <- request.userAnswers.get(IsAgentManagingEstatePage)
      } yield {

        def onRelationshipNotFound =  {

          val returningSuccessRedirect = config.relationshipEstablishmentSuccessUrl
          val returningFailureRedirect = config.relationshipEstablishmentFailureUrl

          val host = config.relationshipEstablishmentFrontendUrl(utr)

          val queryString: Map[String, Seq[String]] = Map(
            "successUrl" -> Seq(returningSuccessRedirect),
            "failureUrl" -> Seq(returningFailureRedirect)
          )

          connector.lock(EstatesStoreRequest(request.internalId, utr, isManagedByAgent, estateLocked = false)) map { _ =>
            Redirect(host, queryString)
          }

        }

        relationship.check(request.internalId, utr) flatMap {
          case RelationshipFound =>
            Future.successful(Redirect(controllers.routes.IvSuccessController.onPageLoad))
          case RelationshipNotFound =>
            onRelationshipNotFound
        }
      }) getOrElse Future.successful(Redirect(controllers.routes.SessionExpiredController.onPageLoad))
  }
}
