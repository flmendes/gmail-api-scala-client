/*
 * Copyright © 2014 Nemanja Stanarevic <nemanja@alum.mit.edu>
 *
 * Made with ❤ in NYC at Hacker School <http://hackerschool.com>
 *
 * Licensed under the GNU Affero General Public License, Version 3
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at:
 *
 * <http://www.gnu.org/licenses/agpl-3.0.html>
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gmailapi.methods

import akka.actor.Actor
import gmailapi.oauth2.OAuth2Identity
import gmailapi.resources.{ Draft, DraftList, Message, MessageFormat, GmailSerializer }
import org.json4s.jackson.Serialization.{ read, write }
import org.json4s.jackson.JsonMethods.parse
import scala.collection.immutable.Map
import scala.language.postfixOps
import spray.http.{ HttpCredentials, HttpEntity, HttpMethods, ContentTypes, Uri }

object Drafts {
  import GmailSerializer._

  case class Create(draft: Draft, userId: String = "me")
    (implicit val token: OAuth2Identity)
    extends GmailRestRequest {
    require(draft.message != None)
    require(draft.message.get.id == None)
    require(draft.message.get.labelIds == Nil)
    require(draft.message.get.snippet == None)
    require(draft.message.get.historyId == None)
    require(draft.message.get.payload == None)
    require(draft.message.get.sizeEstimate == None)
    require(draft.message.get.raw != None)

    val uri = s"$baseUri/users/$userId/drafts"
    val method = HttpMethods.POST
    val credentials: Option[HttpCredentials] = token
    val entity: HttpEntity = HttpEntity(
      ContentTypes.`application/json`,
      write(draft))
    val unmarshaller = Some(read[Draft](_: String))
    val quotaUnits = 10
  }

  case class Delete(id: String, userId: String = "me")
    (implicit val token: OAuth2Identity)
    extends GmailRestRequest {

    val uri = s"$baseUri/users/$userId/drafts/$id"
    val method = HttpMethods.DELETE
    val credentials: Option[HttpCredentials] = token
    val entity = HttpEntity.Empty
    val unmarshaller = None
    val quotaUnits = 10
  }

  case class List(
    maxResults: Option[Int] = None,
    pageToken: Option[String] = None,
    userId: String = "me")(implicit val token: OAuth2Identity)
    extends GmailRestRequest {

    val uri = {
      val queryBuilder = Uri.Query.newBuilder
      maxResults foreach {
        maxResults => queryBuilder += ("maxResults" -> maxResults.toString)
      }
      pageToken foreach {
        pageToken => queryBuilder += ("pageToken" -> pageToken)
      }
      Uri(s"$baseUri/users/$userId/drafts") withQuery (
        queryBuilder.result()) toString
    }
    val method = HttpMethods.GET
    val credentials: Option[HttpCredentials] = token
    val entity = HttpEntity.Empty
    val unmarshaller = Some(read[DraftList](_: String))
    val quotaUnits = 5
  }

  case class Update(id: String, draft: Draft, userId: String = "me")
    (implicit val token: OAuth2Identity) extends GmailRestRequest {
    require(draft.message != None)
    require(draft.message.get.id == None)
    require(draft.message.get.labelIds == Nil)
    require(draft.message.get.snippet == None)
    require(draft.message.get.historyId == None)
    require(draft.message.get.payload == None)
    require(draft.message.get.sizeEstimate == None)
    require(draft.message.get.raw != None)

    val uri = s"$baseUri/users/$userId/drafts/$id"
    val method = HttpMethods.PUT
    val credentials: Option[HttpCredentials] = token
    val entity = HttpEntity(ContentTypes.`application/json`, write(draft))
    val unmarshaller = Some(read[Draft](_: String))
    val quotaUnits = 15
  }

  case class Get(
    id: String,
    format: MessageFormat.Value = MessageFormat.Full,
    userId: String = "me")(implicit val token: OAuth2Identity)
    extends GmailRestRequest {

    val uri = s"$baseUri/users/$userId/drafts/$id"
    val method = HttpMethods.GET
    val credentials: Option[HttpCredentials] = token
    val entity = HttpEntity.Empty
    val unmarshaller = Some(read[Draft](_: String))
    val quotaUnits = 5
  }

  case class Send(draft: Draft, userId: String = "me")
    (implicit val token: OAuth2Identity) extends GmailRestRequest {
    require(draft.message == None)
    require(draft.id != None)

    val uri = s"$baseUri/users/$userId/drafts/send"
    val method = HttpMethods.POST
    val credentials: Option[HttpCredentials] = token
    val entity = HttpEntity(ContentTypes.`application/json`, write(draft))
    val unmarshaller = Some(read[Message](_: String))
    val quotaUnits = 25
  }
}
