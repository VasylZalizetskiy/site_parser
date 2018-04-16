package models

import java.util.Date

import play.api.libs.functional.syntax.unlift
import play.api.libs.json.{JsPath, Json, OFormat}
import play.api.libs.functional.syntax._

case class LinkInfo(acceptor: String,
                    donor: String,
                    anchor: String,
                    anchorRelevance: Option[Boolean] = None,
                    created: Option[Date] = None,
                    removed: Option[Date] = None,
                    checked: Option[Date] = None,
                    id: String
                   )

object LinkInfo {

  implicit val mongoFormat: OFormat[LinkInfo] = (
      (JsPath \ "acceptor").format[String] and
      (JsPath \ "donor").format[String] and
      (JsPath \ "anchor").format[String] and
      (JsPath \ "anchorRelevance").formatNullable[Boolean] and
      (JsPath \ "created").formatNullable[Date] and
      (JsPath \ "removed").formatNullable[Date] and
      (JsPath \ "checked").formatNullable[Date] and
      (JsPath \ "_id").format[String]
    ) (LinkInfo.apply, unlift(LinkInfo.unapply))

}