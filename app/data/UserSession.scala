package data

import ch.japanimpact.auth.api.AppTicketResponse
import play.api.libs.json._

sealed trait UserSession

case class AdminUser(userId: Int, groups: Set[String], firstName: String) extends UserSession

object AdminUser {
  def apply(rep: AppTicketResponse): AdminUser =
    AdminUser(rep.userId, rep.groups, rep.user.details.firstName)
}

case class StaffUser(staffUserId: Int) extends UserSession

object UserSession {
  private implicit val adminFormat: Format[AdminUser] = Json.format[AdminUser]
  private implicit val staffFormat: Format[StaffUser] = Json.format[StaffUser]

  def unapply(session: UserSession): Option[(String, JsValue)] = {
    val (prod: Product, sub) = session match {
      case b: AdminUser => (b, Json.toJson(b)(adminFormat))
      case b: StaffUser => (b, Json.toJson(b)(staffFormat))
    }
    Some(prod.productPrefix -> sub)
  }

  def apply(sessionType: String, session: JsValue): UserSession = {
    (sessionType match {
      case "AdminUser" => Json.fromJson(session)(adminFormat)
      case "StaffUser" => Json.fromJson(session)(staffFormat)
      case other =>
        throw new IllegalArgumentException("No case for " + other)
    }) match {
      case JsSuccess(res, _) => res
      case JsError(errors) =>
        errors.foreach(println)
        throw new IllegalArgumentException("A JS error occurred")
    }
  }

  implicit val format: Format[UserSession] = Json.format[UserSession]

}