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

package views

import org.scalatest.matchers.must.Matchers
import org.scalatestplus.play.PlaySpec
import play.api.data.FormError
import viewmodels.RadioOption

class ViewUtilsSpec extends PlaySpec with Matchers {

  "errorHref" must {

    "point at the individual date field when the error is on a date part" in {

      ViewUtils.errorHref(FormError("dateOfDeath", "error.required", Seq("month"))) mustBe "dateOfDeath.month"
    }

    "point at the yes option for a yes/no question" in {

      ViewUtils.errorHref(FormError("value", "error.required"), isYesNo = true) mustBe "value-yes"
    }

    "point at the first radio option when radio options are supplied" in {

      val options = Seq(RadioOption("prefix", "first"), RadioOption("prefix", "second"))

      ViewUtils.errorHref(FormError("value", "error.required"), radioOptions = options) mustBe "prefix.first"
    }

    "fall back to the error key" in {

      ViewUtils.errorHref(FormError("utr", "error.required")) mustBe "utr"
    }
  }

}
