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

package controllers.testOnlyDoNotUseInAppConf

import com.google.inject.Inject
import config.FrontendAppConfig
import controllers.actions.IdentifierAction
import models.requests.IdentifierRequest
import play.api.Logger
import play.api.i18n.MessagesApi
import play.api.libs.json.Json
import play.api.mvc.{AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.concurrent.{ExecutionContext, Future}

case class BusinessKey(name: String,value: String)

object BusinessKey {
  implicit val format = Json.format[BusinessKey]
}

case class Relationship(relationshipName: String, businessKeys: Set[BusinessKey], credId: String)

object Relationship {
  implicit val format = Json.format[Relationship]
}

case class RelationshipJson(relationship: Relationship, ttlSeconds:Int =1440)

object RelationshipJson {
  implicit val format = Json.format[RelationshipJson]
}

class RelationshipEstablishmentConnector @Inject()(val httpClient: HttpClient,config: FrontendAppConfig)
                                                  (implicit val ec : ExecutionContext) {

  private val relationshipEstablishmentPostUrl: String = s"${config.relationshipEstablishmentBaseUrl}/relationship-establishment/relationship/"

  private def relationshipEstablishmentGetUrl(credId :String): String = s"${config.relationshipEstablishmentBaseUrl}/relationship-establishment/relationship/$credId"

  private def relationshipEstablishmentDeleteUrl(credId: String): String = s"${config.relationshipEstablishmentBaseUrl}/test/relationship/$credId"

  private def newRelationship(credId: String, utr: String): Relationship =
    Relationship(config.relationshipName, Set(BusinessKey(config.relationshipIdentifier, utr)), credId)

  def createRelationship(credId: String, utr: String)(implicit headerCarrier: HeaderCarrier) =
    httpClient.POST[RelationshipJson,HttpResponse](relationshipEstablishmentPostUrl,RelationshipJson(newRelationship(credId, utr)))

  def getRelationship(credId: String)(implicit headerCarrier: HeaderCarrier) =
    httpClient.GET(relationshipEstablishmentGetUrl(credId))

  def deleteRelationship(credId: String)(implicit headerCarrier: HeaderCarrier) =
    httpClient.DELETE(relationshipEstablishmentDeleteUrl(credId))

}

/**
 * Test controller and connector to relationship-establishment to set a relationship for a given UTR.
 * This will then enable the service to "succeed" and "fail" an IV check without having to go into EstateIV.
 */
class TestRelationshipEstablishmentController @Inject()(
                                                         override val messagesApi: MessagesApi,
                                                         val controllerComponents: MessagesControllerComponents,
                                                         relationshipEstablishmentConnector: RelationshipEstablishmentConnector,
                                                         identify: IdentifierAction
                                                       )
                                                       (implicit ec : ExecutionContext)
  extends FrontendBaseController {

  def check(utr: String) = identify.async {
    implicit request =>

      Logger.warn("[TestRelationshipEstablishmentController] EstateIV is using a test route, you don't want this in production.")

      val succeedRegex = "(1\\d{9})".r
      val failRegex = "(2\\d{9})".r

      utr match {
        case "4381028111" | "5000000000" =>
          establishRelationshipForUtr(request, utr)
        case succeedRegex(_) =>
          establishRelationshipForUtr(request, utr)
        case failRegex(_) =>
          Future.successful(Redirect(controllers.routes.IvFailureController.onEstateIvFailure()))
        case _ =>
          Future.successful(Redirect(controllers.routes.IvFailureController.onEstateIvFailure()))
      }
  }

  private def establishRelationshipForUtr(request: IdentifierRequest[AnyContent], utr: String)(implicit hc: HeaderCarrier) = {
    relationshipEstablishmentConnector.createRelationship(request.credentials.providerId, utr) map {
      _ =>
        Redirect(controllers.routes.IvSuccessController.onPageLoad())
    }
  }
}
