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

package controllers.testOnlyDoNotUseInAppConf

import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, delete, get, post, postRequestedFor, urlEqualTo}
import controllers.actions.{FakeIdentifierAction, IdentifierAction}
import org.scalatest.OptionValues
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import utils.WireMockHelper

class TestRelationshipEstablishmentControllerSpec
    extends AnyWordSpec with Matchers with OptionValues with ScalaFutures with WireMockHelper {

  implicit lazy val hc: HeaderCarrier = HeaderCarrier()

  private lazy val app: Application = new GuiceApplicationBuilder()
    .overrides(bind[IdentifierAction].to[FakeIdentifierAction])
    .configure(
      "microservice.services.relationship-establishment.port" -> server.port(),
      "auditing.enabled"                                      -> false,
      "metrics.enabled"                                       -> false
    )
    .build()

  private lazy val controller: TestRelationshipEstablishmentController =
    app.injector.instanceOf[TestRelationshipEstablishmentController]

  private lazy val connector: RelationshipEstablishmentConnector =
    app.injector.instanceOf[RelationshipEstablishmentConnector]

  private val credId = "providerId"
  private val utr    = "1234567890"

  private val createUrl = "/relationship-establishment/relationship/"
  private val getUrl    = s"/relationship-establishment/relationship/$credId"
  private val deleteUrl = s"/test/relationship/$credId"

  "TestRelationshipEstablishmentController" must {

    "establish a relationship and redirect to IV success for a well formed UTR" in {

      server.stubFor(post(urlEqualTo(createUrl)).willReturn(aResponse().withStatus(CREATED)))

      val result = controller.check(utr)(FakeRequest())

      status(result)                 mustBe SEE_OTHER
      redirectLocation(result).value mustBe controllers.routes.IvSuccessController.onPageLoad.url

      server.verify(postRequestedFor(urlEqualTo(createUrl)))
    }

    "redirect to IV failure when the UTR is not ten digits" in {

      val result = controller.check("not-a-utr")(FakeRequest())

      status(result)                 mustBe SEE_OTHER
      redirectLocation(result).value mustBe controllers.routes.IvFailureController.onEstateIvFailure.url
    }
  }

  "RelationshipEstablishmentConnector" must {

    "GET a relationship for a credential" in {

      server.stubFor(get(urlEqualTo(getUrl)).willReturn(aResponse().withStatus(OK)))

      connector.getRelationship(credId).futureValue.status mustBe OK
    }

    "DELETE a relationship for a credential" in {

      server.stubFor(delete(urlEqualTo(deleteUrl)).willReturn(aResponse().withStatus(NO_CONTENT)))

      connector.deleteRelationship(credId).futureValue.status mustBe NO_CONTENT
    }
  }

}
