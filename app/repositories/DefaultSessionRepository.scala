/*
 * Copyright 2023 HM Revenue & Customs
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

package repositories

import config.FrontendAppConfig
import models.UserAnswers
import org.mongodb.scala.model.Indexes.ascending
import org.mongodb.scala.model.{Filters, IndexModel, IndexOptions, ReplaceOptions}
import play.api.libs.json._
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository

import java.time.LocalDateTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DefaultSessionRepository @Inject()(
                                          val mongo: MongoComponent,
                                          val config: FrontendAppConfig
                                        )(implicit val ec: ExecutionContext)
  extends PlayMongoRepository[UserAnswers](
    collectionName = "user-answers",
    mongoComponent = mongo,
    domainFormat = Format(UserAnswers.reads, UserAnswers.writes),
    indexes = Seq(
      IndexModel(
        ascending("updatedAt"),
        IndexOptions()
          .unique(false)
          .name("user-answers-updated-at-index")
          .expireAfter(config.cacheTtlSeconds, TimeUnit.SECONDS))),
    replaceIndexes = config.dropIndexes
  )
    with SessionRepository {

  override def get(id: String): Future[Option[UserAnswers]] =
    collection.find(Filters.eq("_id", id)).headOption()

  override def set(userAnswers: UserAnswers): Future[Boolean] = {
    val selector = Filters.eq("_id", userAnswers.id)
    val options = new ReplaceOptions()
      .upsert(true)
    val updatedObject = userAnswers.copy(lastUpdated = LocalDateTime.now)

    collection.replaceOne(selector, updatedObject, options).headOption().map(_.exists(_.wasAcknowledged()))
  }
}
