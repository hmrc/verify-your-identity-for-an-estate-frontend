/*
 * Copyright 2020 HM Revenue & Customs
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

import connectors.{EstatesStoreConnector, RelationshipEstablishmentConnector}
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import javax.inject.Inject
import models.RelationshipEstablishmentStatus.{UnsupportedRelationshipStatus, UpstreamRelationshipError}
import models.{RelationshipEstablishmentStatus, EstatesStoreRequest}
import pages.{IsAgentManagingEstatePage, UtrPage}
import play.api.Logger
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import views.html.{EstateLocked, EstateNotFound, EstateStillProcessing}

import scala.concurrent.{ExecutionContext, Future}

class IvFailureController @Inject()(
                                     val controllerComponents: MessagesControllerComponents,
                                     lockedView: EstateLocked,
                                     stillProcessingView: EstateStillProcessing,
                                     notFoundView: EstateNotFound,
                                     identify: IdentifierAction,
                                     getData: DataRetrievalAction,
                                     requireData: DataRequiredAction,
                                     relationshipEstablishmentConnector: RelationshipEstablishmentConnector,
                                     connector: EstatesStoreConnector
                                   )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private def renderFailureReason(utr: String, journeyId: String)(implicit hc : HeaderCarrier) = {
    relationshipEstablishmentConnector.journeyId(journeyId) map {
      case RelationshipEstablishmentStatus.Locked =>
        Logger.info(s"[IvFailure][status] $utr is locked")
        Redirect(routes.IvFailureController.estateLocked())
      case RelationshipEstablishmentStatus.NotFound =>
        Logger.info(s"[IvFailure][status] $utr was not found")
        Redirect(routes.IvFailureController.estateNotFound())
      case RelationshipEstablishmentStatus.InProcessing =>
        Logger.info(s"[IvFailure][status] $utr is processing")
        Redirect(routes.IvFailureController.estateStillProcessing())
      case UnsupportedRelationshipStatus(reason) =>
        Logger.warn(s"[IvFailure][status] Unsupported IV failure reason: $reason")
        Redirect(controllers.routes.FallbackFailureController.onPageLoad())
      case UpstreamRelationshipError(response) =>
        Logger.warn(s"[IvFailure][status] HTTP response: $response")
        Redirect(controllers.routes.FallbackFailureController.onPageLoad())
    }
  }

  def onEstateIvFailure(): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      request.userAnswers.get(UtrPage) match {
        case Some(utr) =>
          val queryString = request.getQueryString("journeyId")

          queryString.fold{
            Logger.warn(s"[IVFailureController][onEstateIvFailure] unable to retrieve a journeyId to determine the reason")
            Future.successful(Redirect(controllers.routes.FallbackFailureController.onPageLoad()))
          }{
            journeyId =>
              renderFailureReason(utr, journeyId)
          }
        case None =>
          Logger.warn(s"[IVFailureController][onEstateIvFailure] unable to retrieve a UTR")
          Future.successful(Redirect(controllers.routes.FallbackFailureController.onPageLoad()))
      }
  }

  def estateLocked() : Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      (for {
        utr <- request.userAnswers.get(UtrPage)
        isManagedByAgent <- request.userAnswers.get(IsAgentManagingEstatePage)
      } yield {
        connector.lock(EstatesStoreRequest(request.internalId, utr, isManagedByAgent, estateLocked = true)) map { _ =>
          Ok(lockedView(utr))
        }
      }) getOrElse Future.successful(Redirect(controllers.routes.SessionExpiredController.onPageLoad()))
  }

  def estateNotFound() : Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      request.userAnswers.get(UtrPage) map {
        utr =>
          Future.successful(Ok(notFoundView(utr)))
      } getOrElse Future.successful(Redirect(controllers.routes.SessionExpiredController.onPageLoad()))
  }

  def estateStillProcessing() : Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      request.userAnswers.get(UtrPage) map {
        utr =>
          Future.successful(Ok(stillProcessingView(utr)))
      } getOrElse Future.successful(Redirect(controllers.routes.SessionExpiredController.onPageLoad()))
  }
}