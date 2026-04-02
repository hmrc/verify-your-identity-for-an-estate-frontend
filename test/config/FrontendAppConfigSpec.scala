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

package config

import base.SpecBase

class FrontendAppConfigSpec extends SpecBase {

  "FrontendAppConfig" must {

    "return the stubbed estatesIV URL when stubRelationshipEstablishment is true" in {
      val utr = "1234567890"

      val application = applicationBuilder()
        .configure("microservice.services.features.stubRelationshipEstablishment" -> true)
        .build()

      val config = application.injector.instanceOf[FrontendAppConfig]

      val result = config.relationshipEstablishmentFrontendUrl(utr)

      result must include(utr)
      result must include("test-only")

      application.stop()
    }

    "return the live estatesIV URL when stubRelationshipEstablishment is false" in {
      val utr = "1234567890"

      val application = applicationBuilder()
        .configure("microservice.services.features.stubRelationshipEstablishment" -> false)
        .build()

      val config = application.injector.instanceOf[FrontendAppConfig]

      val result = config.relationshipEstablishmentFrontendUrl(utr)

      result must include(utr)
      result must not include "test-only"

      application.stop()
    }
  }

}
