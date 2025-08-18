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

package config

import com.google.inject.{Inject, Singleton}
import play.api.Configuration
import play.api.i18n.Lang
import uk.gov.hmrc.hmrcfrontend.config.ContactFrontendConfig
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

@Singleton
class FrontendAppConfig @Inject() (configuration: Configuration,
                                   contactFrontendConfig: ContactFrontendConfig,
                                   servicesConfig : ServicesConfig) {

  lazy val serviceName: String = configuration.get[String]("serviceName")

  val betaFeedbackUrl = s"${contactFrontendConfig.baseUrl.get}/contact/beta-feedback?service=${contactFrontendConfig.serviceId.get}"

  lazy val locationCanonicalList: String = configuration.get[String]("location.canonical.list.all")
  lazy val locationCanonicalListNonUK: String = configuration.get[String]("location.canonical.list.nonUK")

  lazy val estatesRegistration: String = configuration.get[String]("urls.estatesRegistration")

  lazy val authUrl: String = servicesConfig.baseUrl("auth")
  lazy val loginUrl: String = configuration.get[String]("urls.login")
  lazy val loginContinueUrl: String = configuration.get[String]("urls.loginContinue")
  lazy val logoutUrl: String = configuration.get[String]("urls.logout")

  lazy val logoutAudit: Boolean =
    configuration.get[Boolean]("microservice.services.features.auditing.logout")

  lazy val countdownLength: Int = configuration.get[Int]("timeout.countdown")
  lazy val timeoutLength: Int = configuration.get[Int]("timeout.length")

  lazy val estatesContinueUrl: String = configuration.get[String]("urls.maintainContinue")

  lazy val playbackEnabled: Boolean = configuration.get[Boolean]("microservice.services.features.playback.enabled")

  lazy val estatesStoreUrl: String = servicesConfig.baseUrl("estates-store") + "/estates-store"
  lazy val taxEnrolmentsUrl: String = servicesConfig.baseUrl("tax-enrolments") + "/tax-enrolments"

  lazy val relationshipEstablishmentUrl : String = servicesConfig.baseUrl("relationship-establishment")

  lazy val relationshipName : String =
    configuration.get[String]("microservice.services.self.relationship-establishment.name")

  lazy val relationshipIdentifier : String =
    configuration.get[String]("microservice.services.self.relationship-establishment.identifier")

  lazy val relationshipEstablishmentStubbed: Boolean =
    configuration.get[Boolean]("microservice.services.features.stubRelationshipEstablishment")

  def relationshipEstablishmentFrontendUrl(utr: String) : String = {
    if(relationshipEstablishmentStubbed) {
      s"${configuration.get[String]("urls.testOnly.estatesIV")}/$utr"
    } else {
      s"${configuration.get[String]("urls.estatesIV")}/$utr"
    }
  }

  lazy val relationshipEstablishmentSuccessUrl : String =
    configuration.get[String]("urls.successUrl")

  lazy val relationshipEstablishmentFailureUrl : String =
    configuration.get[String]("urls.failureUrl")

  lazy val languageTranslationEnabled: Boolean =
    configuration.get[Boolean]("microservice.services.features.welsh-translation")

  def languageMap: Map[String, Lang] = Map(
    "english" -> Lang("en"),
    "cymraeg" -> Lang("cy")
  )

  val cacheTtlSeconds: Long = configuration.get[Long]("mongodb.timeToLiveInSeconds")
  val dropIndexes: Boolean = configuration.getOptional[Boolean]("microservice.services.features.mongo.dropIndexes").getOrElse(false)

}
