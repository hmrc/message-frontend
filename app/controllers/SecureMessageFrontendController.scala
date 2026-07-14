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

package controllers

import binders.{ Encrypted, ParameterisedUrl }
import play.api.Logging
import play.api.libs.ws.WSClient
import play.api.mvc.*
import uk.gov.hmrc.play.bootstrap.binders.RedirectUrl
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class SecureMessageFrontendController @Inject (
  servicesConfig: ServicesConfig,
  ws: WSClient,
  cc: MessagesControllerComponents
)(implicit ec: ExecutionContext)
    extends FrontendController(cc) with Logging {

  private val serviceName = "secure-message-frontend"
  private val secureMessageUrl: String = servicesConfig.baseUrl(serviceName)

  val secueMessageFE_Host =
    servicesConfig.getConfString(
      s"$serviceName.host",
      throw new RuntimeException(s"Could not find config key $serviceName.host")
    )

  def list(taxIdentifiers: List[String], regimes: List[String] = List()): Action[AnyContent] = forwardRequest()

  def btaList(taxIdentifiers: List[String], regimes: List[String] = List()): Action[AnyContent] = forwardRequest()

  def inboxLink(
    messagesInboxUrl: RedirectUrl,
    taxIdentifiers: List[String],
    regimes: List[String] = List()
  ): Action[AnyContent] = forwardRequest()

  def read(encryptedUrl: Encrypted[ParameterisedUrl]): Action[AnyContent] = forwardRequest()

  def count(
    readPreference: Option[model.ReadPreference.Value],
    taxIdentifiers: List[String],
    regimes: List[String] = List()
  ): Action[AnyContent] = forwardRequest()

  def forwardRequest(): Action[AnyContent] = Action.async { implicit request =>
    val queryStringParams = request.queryString.toSeq.flatMap { case (key, values) =>
      values.map(value => (key, value))
    }

    val url = s"$secureMessageUrl${request.uri}"
    val headers = (request.headers.toSimpleMap + ("Host" -> secueMessageFE_Host)).toSeq

    logger.debug(s"Forward the request ${request.uri} to $serviceName")

    ws.url(url)
      .withHttpHeaders(headers: _*)
      .withQueryStringParameters(queryStringParams: _*)
      .get()
      .map(buildResult)
  }

  def buildResult(response: play.api.libs.ws.WSResponse): Result = {
    logger.debug(s"Response from $serviceName : ${response.body} ${response.status}")
    Status(response.status)(response.body)
      .as(response.contentType)
      .withHeaders(response.headers.view.mapValues(_.head).toSeq: _*)
  }
}
