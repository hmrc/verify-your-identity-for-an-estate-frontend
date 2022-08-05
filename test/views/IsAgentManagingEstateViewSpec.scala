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

package views

import forms.IsAgentManagingEstateFormProvider
import models.NormalMode
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.YesNoViewBehaviours
import views.html.IsAgentManagingEstateView

class IsAgentManagingEstateViewSpec extends YesNoViewBehaviours {

  val messageKeyPrefix = "isAgentManagingEstate"
  val captionMessageKey = "utr.caption"
  val form = new IsAgentManagingEstateFormProvider()()

  val utr = "0987654321"

  "IsAgentManagingEstate view" must {

    val view = viewFor[IsAgentManagingEstateView](Some(emptyUserAnswers))

    def applyView(form: Form[_]): HtmlFormat.Appendable =
      view.apply(form, NormalMode, utr)(fakeRequest, messages)

    behave like normalPageTitleWithCaption(applyView(form),
      messageKeyPrefix,
      "utr",
      utr)

    behave like pageWithBackLink(applyView(form))

    behave like yesNoPage(
      form,
      applyView,
      messageKeyPrefix,
      captionMessageKey,
      utr,
      controllers.routes.IsAgentManagingEstateController.onSubmit(NormalMode).url
    )

  }
}
