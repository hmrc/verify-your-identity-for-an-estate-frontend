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

package models

import base.SpecBase
import pages.QuestionPage
import play.api.libs.json.{JsPath, Json}

class UserAnswersSpec extends SpecBase {

  object TestPage extends QuestionPage[String] {
    override def path: JsPath     = JsPath \ "testValue"
    override def toString: String = "testValue"
  }

  object NestedPage extends QuestionPage[String] {
    override def path: JsPath     = JsPath \ "existing" \ "nested"
    override def toString: String = "existing"
  }

  "UserAnswers" must {

    "get" must {

      "return None when a value is not present" in {
        val ua = UserAnswers("id")
        ua.get(TestPage) mustBe None
      }

      "return Some when a value is present" in {
        val ua = UserAnswers("id").set(TestPage, "foo").success.value
        ua.get(TestPage) mustBe Some("foo")
      }

      "return None when the stored value cannot be read as the expected type" in {
        val ua = UserAnswers("id", Json.obj("testValue" -> 42))
        ua.get(TestPage) mustBe None
      }
    }

    "set" must {

      "set a value successfully" in {
        val ua = UserAnswers("id").set(TestPage, "bar").success.value
        ua.get(TestPage) mustBe Some("bar")
      }

      "fail when the path conflicts with an existing non-object value" in {
        val ua = UserAnswers("id", Json.obj("existing" -> "aString"))
        ua.set(NestedPage, "value").isFailure mustBe true
      }
    }

    "remove" must {

      "remove a value that exists" in {
        val ua      = UserAnswers("id").set(TestPage, "foo").success.value
        val removed = ua.remove(TestPage).success.value
        removed.get(TestPage) mustBe None
      }

      "succeed even when the value does not exist" in {
        val ua     = UserAnswers("id")
        val result = ua.remove(TestPage).success.value
        result.get(TestPage) mustBe None
      }

      "return existing data when the path conflicts with a non-object value" in {
        val ua     = UserAnswers("id", Json.obj("existing" -> "aString"))
        val result = ua.remove(NestedPage).success.value
        result.data mustEqual ua.data
      }
    }

    "reads/writes" must {

      "serialise and deserialise correctly" in {
        val ua   = UserAnswers("test-id")
        val json = Json.toJson(ua)(UserAnswers.writes)
        json.as[UserAnswers](UserAnswers.reads).id mustEqual "test-id"
      }
    }
  }

}
