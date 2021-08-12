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

import play.api.data.{Field, Form, FormError}
import play.api.i18n.Messages
import viewmodels.RadioOption
import uk.gov.hmrc.govukfrontend.views.html.components.{RadioItem, Text}

object ViewUtils {

  def errorPrefix(form: Form[_])(implicit messages: Messages): String = {
    if (form.hasErrors || form.hasGlobalErrors) s"${messages("error.browser.title.prefix")} " else ""
  }

  def breadcrumbTitle(title: String)(implicit messages: Messages): String = {
    s"$title - ${messages("service.name")} - GOV.UK"
  }

  def errorHref(error: FormError, radioOptions: Seq[RadioOption] = Nil, isYesNo: Boolean = false): String = {
    error.args match {
      case x if x.contains("day") || x.contains("month") || x.contains("year") =>
        s"${error.key}.${error.args.head}"
      case _ if isYesNo =>
        s"${error.key}-yes"
      case _ if radioOptions.nonEmpty =>
        radioOptions.head.id
      case _ =>
          s"${error.key}"
    }
  }

  def mapRadioOptionsToRadioItems(field: Field, inputs: Seq[RadioOption])(implicit messages: Messages): Seq[RadioItem] =
    inputs.map(
      a => {
        RadioItem(
          id = Some(a.id),
          value = Some(a.value),
          checked = field.value.contains(a.value),
          content = Text(messages(a.messageKey)),
          attributes = Map.empty
        )
      }
    )
}
