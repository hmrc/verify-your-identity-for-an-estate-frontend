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

package utils.countryOptions

import base.SpecBase
import com.typesafe.config.ConfigException

class CountryOptionsSpec extends SpecBase {

  "Country Options" must {

    "build correctly the InputOptions with all country list and country code" in {

      val application = applicationBuilder()
        .configure(Map(
          "location.canonical.list.all" -> "countries-canonical-list-test.json"
        ))
        .build()

      val countryOption: CountryOptions = application.injector.instanceOf[AllCountryOptions]
      countryOption.options mustEqual Seq(InputOption("ES", "Spain"), InputOption("GB", "United Kingdom"))

      application.stop()
    }

    "throw the error if the country json does not exist" in {

      val application = applicationBuilder()
        .configure(Map(
          "location.canonical.list.all" -> "countries-canonical-test.json"
        ))
        .build()

      an[ConfigException.BadValue] shouldBe thrownBy {
        application.injector.instanceOf[AllCountryOptions].options
      }

      application.stop()
    }
  }
}
