/*
 * Copyright 2022 HM Revenue & Customs
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

import base.SpecBase
import connectors.TaxEnrolmentsConnector
import models.UserAnswers
import org.mockito.ArgumentMatchers.{eq => eqTo, _}
import org.mockito.Mockito.{verify => verifyMock, _}
import org.scalatest.BeforeAndAfterAll
import org.mockito.MockitoSugar.mock
import pages.{IsAgentManagingEstatePage, UtrPage}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.{RelationshipEstablishment, RelationshipFound}
import views.html.{IvSuccessView, IvSuccessWithoutPlaybackView}

import scala.concurrent.Future

class IvSuccessControllerSpec extends SpecBase with BeforeAndAfterAll {

  private val utr = "0987654321"

  private val connector = mock[TaxEnrolmentsConnector]
  private val mockRelationshipEstablishment = mock[RelationshipEstablishment]

  "Returning IvSuccess Controller" must {

    "return OK and the correct view for a GET with no Agent" in {

      val userAnswers = UserAnswers(userAnswersId)
        .set(IsAgentManagingEstatePage, false).success.value
        .set(UtrPage, utr).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers), relationshipEstablishment = mockRelationshipEstablishment)
        .overrides(
          bind(classOf[TaxEnrolmentsConnector]).toInstance(connector)
        ).configure("microservice.services.features.playback.enabled" -> true)
        .build()

      val request = FakeRequest(GET, controllers.routes.IvSuccessController.onPageLoad.url)

      val view = application.injector.instanceOf[IvSuccessView]

      val viewAsString = view(isAgent = false, utr)(request, messages).toString

      when(mockRelationshipEstablishment.check(eqTo("id"), eqTo(utr))(any()))
        .thenReturn(Future.successful(RelationshipFound))

      val result = route(application, request).value

      status(result) mustEqual OK

      contentAsString(result) mustEqual viewAsString

      verifyMock(mockRelationshipEstablishment).check(eqTo("id"), eqTo(utr))(any())

      reset(connector)
      reset(mockRelationshipEstablishment)

      application.stop()

    }

    "return OK and the correct view for a GET when playback is disabled" in {

      val userAnswers = UserAnswers(userAnswersId)
        .set(IsAgentManagingEstatePage, false).success.value
        .set(UtrPage, utr).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers), relationshipEstablishment = mockRelationshipEstablishment)
        .overrides(
          bind(classOf[TaxEnrolmentsConnector]).toInstance(connector)
        ).configure("microservice.services.features.playback.enabled" -> false)
        .build()

      val request = FakeRequest(GET, controllers.routes.IvSuccessController.onPageLoad.url)

      val view = application.injector.instanceOf[IvSuccessWithoutPlaybackView]

      val viewAsString = view(utr)(request, messages).toString

      when(mockRelationshipEstablishment.check(eqTo("id"), eqTo(utr))(any()))
        .thenReturn(Future.successful(RelationshipFound))

      val result = route(application, request).value

      status(result) mustEqual OK

      contentAsString(result) mustEqual viewAsString

      verifyMock(mockRelationshipEstablishment).check(eqTo("id"), eqTo(utr))(any())

      reset(connector)
      reset(mockRelationshipEstablishment)

      application.stop()

    }

    "return OK and the correct view for a GET with Agent" in {

      val userAnswers = UserAnswers(userAnswersId)
        .set(IsAgentManagingEstatePage, true).success.value
        .set(UtrPage, utr).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers), relationshipEstablishment = mockRelationshipEstablishment)
        .overrides(
          bind(classOf[TaxEnrolmentsConnector]).toInstance(connector)
        ).configure("microservice.services.features.playback.enabled" -> true)
        .build()

      val request = FakeRequest(GET, controllers.routes.IvSuccessController.onPageLoad.url)

      val view = application.injector.instanceOf[IvSuccessView]

      val viewAsString = view(isAgent = true, utr)(request, messages).toString

      when(mockRelationshipEstablishment.check(eqTo("id"), eqTo(utr))(any()))
        .thenReturn(Future.successful(RelationshipFound))

      val result = route(application, request).value

      status(result) mustEqual OK

      contentAsString(result) mustEqual viewAsString

      verifyMock(mockRelationshipEstablishment).check(eqTo("id"), eqTo(utr))(any())

      reset(connector)
      reset(mockRelationshipEstablishment)

      application.stop()

    }

    "redirect to next page" when {

      "clicking continue" in {

        lazy val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        lazy val request = FakeRequest(POST, controllers.routes.IvSuccessController.onSubmit.url)

        lazy val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual "http://localhost:8828/maintain-an-estate/status"

        application.stop()

      }

    }

    "redirect to Session Expired" when {

      "no existing data is found" in {

        lazy val application = applicationBuilder(userAnswers = None).build()

        lazy val request = FakeRequest(GET, controllers.routes.IvSuccessController.onPageLoad.url)

        lazy val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad.url

        application.stop()

      }

    }

  }
}
