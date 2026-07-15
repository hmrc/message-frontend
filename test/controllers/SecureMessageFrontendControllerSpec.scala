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

package controllers

import binders.Encrypted
import org.mockito.Mockito.*
import org.mockito.ArgumentMatchers.any
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.ws.{ WSClient, WSRequest, WSResponse }
import play.api.test.Helpers.*
import play.api.test.{ FakeRequest, Injecting }
import uk.gov.hmrc.play.bootstrap.binders.RedirectUrl
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import scala.concurrent.Future

class SecureMessageFrontendControllerSpec
    extends PlaySpec with Injecting with Matchers with ScalaFutures with GuiceOneAppPerSuite with MockitoSugar {

  val mockServicesConfig: ServicesConfig = mock[ServicesConfig]
  val mockWsClient: WSClient = mock[WSClient]
  val mockWsRequest: WSRequest = mock[WSRequest]
  val mockWsResponse: WSResponse = mock[WSResponse]

  when(mockServicesConfig.baseUrl("secure-message-frontend")).thenReturn("http://secure-message-frontend")
  when(mockWsClient.url(any[String])).thenReturn(mockWsRequest)
  when(mockWsRequest.withHttpHeaders(any[Seq[(String, String)]]: _*)).thenReturn(mockWsRequest)
  when(mockWsRequest.withQueryStringParameters(any[Seq[(String, String)]]: _*)).thenReturn(mockWsRequest)
  when(mockWsRequest.get()).thenReturn(Future.successful(mockWsResponse))

  when(mockWsResponse.status).thenReturn(OK)
  when(mockWsResponse.body).thenReturn("""{"message":"sample response body"}""")
  when(mockWsResponse.contentType).thenReturn("application/json")
  when(mockWsResponse.headers).thenReturn(Map("Content-Type" -> Seq("application/json")))

  override def fakeApplication(): Application = new GuiceApplicationBuilder()
    .overrides(
      bind[ServicesConfig].toInstance(mockServicesConfig),
      bind[WSClient].toInstance(mockWsClient)
    )
    .build()

  lazy val controller: SecureMessageFrontendController = inject[SecureMessageFrontendController]

  "SecureMessageFrontendController" must {

    "return OK for list" in {
      val request = FakeRequest(GET, "/list?taxIdentifiers=nino&regimes=paye")
      val result = controller.list(List("nino"), List("paye")).apply(request)

      status(result) mustBe OK
    }

    "return OK for btaList" in {
      val request = FakeRequest(GET, "/btaList?taxIdentifiers=sautr")
      val result = controller.btaList(List("sautr")).apply(request)

      status(result) mustBe OK
    }

    "return OK inboxLink" in {
      val request = FakeRequest(GET, "/inboxLink?messagesInboxUrl=inbox&taxIdentifiers=nino&regimes=paye")
      val result = controller.inboxLink(RedirectUrl("/inbox"), List("nino"), List("paye")).apply(request)
      status(result) mustBe OK
    }

    "return OK for read" in {
      val request = FakeRequest(GET, "/read?encryptedUrl=mockEncryptedUrl")
      val encryptedUrl = Encrypted(binders.ParameterisedUrl("mockEncryptedUrl"))
      val result = controller.read(encryptedUrl).apply(request)

      status(result) mustBe OK
    }

    "return OK for count" in {
      val request = FakeRequest(GET, "/count?readPreference=No&taxIdentifiers=nino&regimes=paye")
      val result = controller.count(Some(model.ReadPreference.No), List("nino"), List("paye")).apply(request)

      status(result) mustBe OK
    }
  }
}
