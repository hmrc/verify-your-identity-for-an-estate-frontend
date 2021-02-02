/*
 * Copyright 2021 HM Revenue & Customs
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
import views.html.IvSuccessWithoutPlaybackView

class IvSuccessWithoutPlaybackViewSpec extends ViewBehaviours {

  val utr = "0987654321"

  "Returning IvSuccess view" must {

    "display the register link when config.playbackEnabled is false" when {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .configure("microservice.services.features.playback.enabled" -> false)
        .build()

      val view = application.injector.instanceOf[IvSuccessWithoutPlaybackView]

      val applyView = view.apply(utr)(fakeRequest, messages)

      behave like normalPage(applyView, "ivSuccess.withoutplayback", "paragraph1", "paragraph2", "paragraph3",
        "ifYouNeedHelp", "contactLink")
    }

    "display the correct subheading" in {

      val view = viewFor[IvSuccessWithoutPlaybackView](Some(emptyUserAnswers))

      val applyView = view.apply(utr)(fakeRequest, messages)

      val doc = asDocument(applyView)
      assertContainsText(doc, messages("ivSuccess.subheading", utr))
    }
  }
}
