/*
 * Copyright 2023 HM Revenue & Customs
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
import models.{NormalMode, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.when
import org.mockito.MockitoSugar.mock
import pages.UtrPage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import services.{FakeRelationshipEstablishmentService, RelationshipNotFound}

import scala.concurrent.Future

class SaveUTRControllerSpec extends SpecBase {

  val utr = "0987654321"

  val fakeEstablishmentServiceFailing = new FakeRelationshipEstablishmentService(RelationshipNotFound)

  "SaveUTRController" must {

    "send UTR to session repo" when {

      "user answers does not exist" in {

        val captor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])

        val mockSessionRepository = mock[SessionRepository]

        when(mockSessionRepository.set(captor.capture()))
          .thenReturn(Future.successful(true))

        val application = applicationBuilder(userAnswers = None, fakeEstablishmentServiceFailing)
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        val request = FakeRequest(GET, controllers.routes.SaveUTRController.save(utr).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustBe controllers.routes.IsAgentManagingEstateController.onPageLoad(NormalMode).url

        captor.getValue.get(UtrPage).value mustBe utr

      }
      "user answers exists" in {

        val captor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])

        val mockSessionRepository = mock[SessionRepository]

        when(mockSessionRepository.set(captor.capture()))
          .thenReturn(Future.successful(true))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), fakeEstablishmentServiceFailing)
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        val request = FakeRequest(GET, controllers.routes.SaveUTRController.save(utr).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustBe controllers.routes.IsAgentManagingEstateController.onPageLoad(NormalMode).url

        captor.getValue.get(UtrPage).value mustBe utr

      }
    }
  }

}
