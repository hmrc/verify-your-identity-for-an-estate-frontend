/*
 * Copyright 2025 HM Revenue & Customs
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

package handlers

import base.SpecBase
import play.api.i18n.MessagesApi
import views.html.ErrorTemplate

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
class ErrorHandlerSpec extends SpecBase {

  private val messageApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  private val errorTemplate: ErrorTemplate = app.injector.instanceOf[ErrorTemplate]
  private val errorHandler: ErrorHandler = new ErrorHandler(messageApi, errorTemplate)

  "ErrorHandler" must {

    "return an error page" in {
      val result = Await.result(errorHandler.standardErrorTemplate(
        pageTitle = "pageTitle",
        heading = "heading",
        message = "message"
      )(fakeRequest), 1.seconds)

      result.body must include("pageTitle")
      result.body must include("message")
    }


  }
}
