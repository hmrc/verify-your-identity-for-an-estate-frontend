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

package views

import views.behaviours.ViewBehaviours
import views.html.IvSuccessView

class IvSuccessViewSpec extends ViewBehaviours {

  val utr = "0987654321"

  "Returning IvSuccess view with Agent" must {

    "display the register link when config.playbackEnabled is true" when {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .configure("microservice.services.features.playback.enabled" -> true)
        .build()

      val view = application.injector.instanceOf[IvSuccessView]

      val applyView = view.apply(isAgent = true, utr)(fakeRequest, messages)

      behave like normalPageTitleWithCaption(applyView,
        "ivSuccess.agent",
        "utr",
        utr,
        "paragraph1",
        "paragraph2",
        "continueLink",
        "paragraph3",
        "paragraph4")
    }
  }

  "IvSuccess view with no Agent" must {

    val view = viewFor[IvSuccessView](Some(emptyUserAnswers))

    val applyView = view.apply(isAgent = false, utr)(fakeRequest, messages)

    behave like normalPageTitleWithCaption(applyView,
      "ivSuccess.no.agent",
      "utr",
      utr,
      "paragraph1",
      "paragraph2",
      "contactLink")

    "show the form" in {
      val doc = asDocument(applyView)
      assertRenderedByCssSelector(doc, "form")
    }

    "show the continue button" in {
      val doc = asDocument(applyView)
      assertRenderedByCssSelector(doc, ".govuk-button")
      assertContainsText(doc, doc.select(".govuk-button").text())
    }
  }
}
