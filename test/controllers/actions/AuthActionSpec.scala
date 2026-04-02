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

package controllers.actions

import base.SpecBase
import config.FrontendAppConfig
import controllers.routes
import play.api.mvc.{Action, AnyContent, BodyParsers, Results}
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.{Credentials, ~}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AuthActionSpec extends SpecBase {

  class Harness(authAction: IdentifierAction) {
    def onPageLoad: Action[AnyContent] = authAction(_ => Results.Ok)
  }

  private val appConfig: FrontendAppConfig = injector.instanceOf[FrontendAppConfig]

  private def authFunctions(exception: Throwable) =
    new AuthPartialFunctions(new FakeFailingAuthConnector(exception), appConfig)

  "Auth Action" when {

    "the user hasn't logged in" must {

      "redirect the user to log in " in {

        val application = applicationBuilder(userAnswers = None).build()

        val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
        val authAction  = new AuthenticatedIdentifierAction(authFunctions(new MissingBearerToken), bodyParsers)
        val controller  = new Harness(authAction)
        val result      = controller.onPageLoad(fakeRequest)

        status(result) mustBe SEE_OTHER

        redirectLocation(result).get must startWith(frontendAppConfig.loginUrl)
      }
    }

    "the user's session has expired" must {

      "redirect the user to log in " in {

        val application = applicationBuilder(userAnswers = None).build()

        val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
        val authAction  = new AuthenticatedIdentifierAction(authFunctions(new MissingBearerToken), bodyParsers)
        val controller  = new Harness(authAction)
        val result      = controller.onPageLoad(fakeRequest)

        status(result) mustBe SEE_OTHER

        redirectLocation(result).get must startWith(frontendAppConfig.loginUrl)
      }
    }

    "the user doesn't have sufficient enrolments" must {

      "redirect the user to the unauthorised page" in {

        val application = applicationBuilder(userAnswers = None).build()

        val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
        val authAction  = new AuthenticatedIdentifierAction(authFunctions(new InsufficientEnrolments), bodyParsers)
        val controller  = new Harness(authAction)
        val result      = controller.onPageLoad(fakeRequest)

        status(result) mustBe SEE_OTHER

        redirectLocation(result) mustBe Some(routes.UnauthorisedController.onPageLoad.url)
      }
    }

    "the user doesn't have sufficient confidence level" must {

      "redirect the user to the unauthorised page" in {

        val application = applicationBuilder(userAnswers = None).build()

        val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
        val authAction  = new AuthenticatedIdentifierAction(authFunctions(new InsufficientConfidenceLevel), bodyParsers)
        val controller  = new Harness(authAction)
        val result      = controller.onPageLoad(fakeRequest)

        status(result) mustBe SEE_OTHER

        redirectLocation(result) mustBe Some(routes.UnauthorisedController.onPageLoad.url)
      }
    }

    "the user used an unaccepted auth provider" must {

      "redirect the user to the unauthorised page" in {

        val application = applicationBuilder(userAnswers = None).build()

        val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
        val authAction  = new AuthenticatedIdentifierAction(authFunctions(new UnsupportedAuthProvider), bodyParsers)
        val controller  = new Harness(authAction)
        val result      = controller.onPageLoad(fakeRequest)

        status(result) mustBe SEE_OTHER

        redirectLocation(result) mustBe Some(routes.UnauthorisedController.onPageLoad.url)
      }
    }

    "the user has an unsupported affinity group" must {

      "redirect the user to the unauthorised page" in {

        val application = applicationBuilder(userAnswers = None).build()

        val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
        val authAction  = new AuthenticatedIdentifierAction(authFunctions(new UnsupportedAffinityGroup), bodyParsers)
        val controller  = new Harness(authAction)
        val result      = controller.onPageLoad(fakeRequest)

        status(result) mustBe SEE_OTHER

        redirectLocation(result) mustBe Some(routes.UnauthorisedController.onPageLoad.url)
      }
    }

    "the user has an unsupported credential role" must {

      "redirect the user to the unauthorised page" in {

        val application = applicationBuilder(userAnswers = None).build()

        val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
        val authAction  = new AuthenticatedIdentifierAction(authFunctions(new UnsupportedCredentialRole), bodyParsers)
        val controller  = new Harness(authAction)
        val result      = controller.onPageLoad(fakeRequest)

        status(result) mustBe SEE_OTHER

        redirectLocation(result) mustBe Some(routes.UnauthorisedController.onPageLoad.url)
      }
    }

    "the user is fully authenticated" must {

      "invoke the block and return OK" in {

        val application = applicationBuilder(userAnswers = None).build()

        val bodyParsers = application.injector.instanceOf[BodyParsers.Default]

        val retrievalResult: Future[Option[String] ~ Option[Credentials] ~ Option[AffinityGroup]] =
          Future.successful(
            new ~(
              new ~(Some("internalId"), Some(Credentials("providerId", "providerType"))),
              Some(AffinityGroup.Individual)
            )
          )

        val authAction = new AuthenticatedIdentifierAction(
          new AuthPartialFunctions(new FakeAuthConnector(retrievalResult), appConfig),
          bodyParsers
        )
        val controller = new Harness(authAction)
        val result     = controller.onPageLoad(fakeRequest)

        status(result) mustBe OK

        application.stop()
      }
    }

    "auth returns incomplete retrieval data" must {

      "throw UnauthorizedException" in {

        val application = applicationBuilder(userAnswers = None).build()

        val bodyParsers = application.injector.instanceOf[BodyParsers.Default]

        val retrievalResult: Future[Option[String] ~ Option[Credentials] ~ Option[AffinityGroup]] =
          Future.successful(new ~(new ~(None, None), None))

        val authAction = new AuthenticatedIdentifierAction(
          new AuthPartialFunctions(new FakeAuthConnector(retrievalResult), appConfig),
          bodyParsers
        )
        val controller = new Harness(authAction)

        intercept[Exception] {
          await(controller.onPageLoad(fakeRequest))
        }

        application.stop()
      }
    }
  }

}
