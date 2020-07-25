package models

import anorm.SqlParser._
import anorm._
import data._
import data.returnTypes.{FullAccred, FullAccredLog, FullAccredType, FullStaffAccount}
import javax.inject.{Inject, Singleton}
import utils.SqlUtils

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AccredsModel @Inject()(dbApi: play.api.db.DBApi, events: EventsModel)(implicit ec: ExecutionContext) {

  private val db = dbApi database "default"

  private def currentEventId = events.getCurrentEventIdSync

  def getAccreds(event: Option[Int] = None): Future[List[Accred]] = Future(db.withConnection { implicit conn =>
    SQL("SELECT * FROM accreds WHERE deleted = 0 AND event_id = {eventId}")
      .on("eventId" -> event.getOrElse(currentEventId))
      .as(AccredRowParser.*)
  })

  def getAccred(id: Int): Future[Option[Accred]] = Future(db.withConnection { implicit conn =>
    SQL("SELECT * FROM accreds WHERE deleted = 0 AND accred_id = {id}")
      .on("id" -> id)
      .as(AccredRowParser.singleOpt)
  })

  def getFullAccred(id: Int): Future[Option[(Accred, FullAccredType)]] = Future(db.withConnection { implicit conn =>
    SQL(
      """SELECT accreds.*, accred_types.*, physical_accred_types.*
        |FROM accreds
        |LEFT JOIN accred_types ON accred_types.accred_type_id = accreds.accred_type_id
        |LEFT JOIN accred_type_mappings atm on accred_types.accred_type_id = atm.accred_type_id AND atm.event_id = accreds.event_id
        |LEFT JOIN physical_accred_types on atm.physical_accred_type_id = physical_accred_types.physical_accred_type_id
        |WHERE accreds.deleted = 0 AND accreds.accred_id = {id}""".stripMargin)
      .on("id" -> id)
      .as((AccredRowParser ~ FullAccredTypeRowParser).singleOpt)
      .map { case accred ~ tpe => (accred, tpe) }
  })

  def getAuthors(event: Option[Int] = None): Future[Set[Int]] = Future(db.withConnection { implicit conn =>
    SQL("SELECT authored_by FROM accreds WHERE deleted = 0 AND event_id = {id} UNION SELECT authored_by_admin AS authored_by FROM accred_logs WHERE authored_by_admin IS NOT NULL")
      .on("id" -> event.getOrElse(currentEventId))
      .as(int(1).*)
      .toSet
  })

  def createAccred(accred: Accred, event: Option[Int] = None) = Future(db.withConnection { implicit conn =>
    SqlUtils.insertOne("accreds", accred.copy(eventId = event.getOrElse(this.currentEventId)))
  })

  def createAccreds(author: Int, accreds: List[Accred], event: Option[Int] = None) = Future(db.withConnection { implicit conn =>
    SqlUtils.insertMultiple("accreds", accreds.map(accred => accred.copy(None, authoredBy = author, status = AccredStatus.Granted, eventId = event.getOrElse(this.currentEventId))))
  })

  def updateAccred(id: Int, accred: Accred): Future[Int] = Future(db.withConnection { implicit conn =>
    val params: Seq[NamedParameter] = AccredParameterList(accred)

    SQL("UPDATE accreds SET lastname = {lastname}, firstname = {firstname}, body_name = {bodyName}, stage_name = {stageName}, must_contact_admin = {mustContactAdmin}, details = {details}, accred_type_id = {accredTypeId}, prefered_vip_desk = {preferedVipDesk}, require_real_name_on_delivery = {requireRealNameOnDelivery} WHERE accred_id = {accredId}")
      .on(params: _*)
      .executeUpdate()
  })

  def updateAccredStatus(id: Int, target: data.AccredStatus.Value, staffId: Option[Int], adminId: Option[Int], remarks: String,
                         firstName: Option[String], lastName: Option[String], number: Option[String], event: Option[Int] = None) = Future(db.withConnection { implicit conn =>
    val nameUpdate = if (firstName.nonEmpty) "firstname = {firstName}, lastname = {lastName}, " else ""

    val eventId = event.getOrElse(currentEventId)

    SQL("INSERT INTO accred_logs(accred_id, authored_by_admin, authored_by_staff, source_state, target_state, remarks, accred_number) (SELECT {accredId}, {admin}, {staff}, status, {targetState}, {remarks}, {accredNumber} FROM accreds WHERE accred_id = {accredId} AND deleted = 0 AND event_id = {eventId}) ")
      .on("accredId" -> id, "eventId" -> eventId, "admin" -> adminId, "staff" -> staffId, "targetState" -> target, "remarks" -> remarks, "accredNumber" -> number)
      .executeUpdate() == 1 && SQL("UPDATE accreds SET " + nameUpdate + "status = {targetState} WHERE accred_id = {accredId} AND deleted = 0 AND event_id = {eventId}")
      .on("accredId" -> id, "eventId" -> eventId, "targetState" -> target, "firstName" -> firstName, "lastName" -> lastName).executeUpdate() == 1
  })

  def deleteAccred(id: Int): Future[Int] = Future(db.withConnection { implicit conn =>
    SQL("UPDATE accreds SET deleted = 1 WHERE accred_id = {accredId}")
      .on("accredId" -> id)
      .executeUpdate()
  })

  def getLogs(id: Int): Future[List[FullAccredLog]] = Future(db.withConnection { implicit conn =>
    SQL("SELECT * FROM accred_logs LEFT JOIN staff_accounts on accred_logs.authored_by_staff = staff_accounts.staff_account_id LEFT JOIN vip_desks on staff_accounts.vip_desk_id = vip_desks.vip_desk_id WHERE accred_id = {id} ORDER BY accred_log_time DESC")
      .on("id" -> id)
      .as((AccredLogRowParser ~ StaffAccountRowParser.? ~ VipDeskRowParser.?).map {
        case log ~ Some(staff) ~ Some(desk) => FullAccredLog(log, Some(FullStaffAccount(staff, desk)))
        case log ~ r1 ~ r2 =>
          println(r1 + " - " + r2)
          FullAccredLog(log, None)
      }.*)
  })

}
