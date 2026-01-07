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

package views.resources

import views.behaviours.ViewBehaviours
import views.html.resources.Stylesheets

class StylesheetsViewSpec extends ViewBehaviours {
  "noraml page view for stylesheet" should {

    "render the link tag and check for the attributes" in {
      val application = applicationBuilder().build()

      val view = application.injector.instanceOf[Stylesheets]

      val applyView = view.apply()(fakeRequest)

      val doc = asDocument(applyView)

      val linkTag = doc.select("link")
      linkTag.attr("href") must include("stylesheets/application.css")
      linkTag.attr("media") mustBe "all"
      linkTag.attr("rel") mustBe "stylesheet"
      linkTag.attr("type") mustBe "text/css"

    }
  }
}
