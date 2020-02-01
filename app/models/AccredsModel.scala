package models

import anorm.SqlParser._
import anorm._
import data._
import data.returnTypes.{FullAccred, FullAccredType}
import javax.inject.{Inject, Singleton}
import utils.SqlUtils

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AccredsModel @Inject()(dbApi: play.api.db.DBApi, events: EventsModel)(implicit ec: ExecutionContext) {

  private val db = dbApi database "default"

  private def eventId = events.getCurrentEventIdSync

  def getAccreds: Future[List[Accred]] = Future(db.withConnection { implicit conn =>
    SQL("SELECT * FROM accreds WHERE deleted = 0 AND event_id = {eventId}")
      .on("eventId" -> eventId)
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

  def getAuthors: Future[Set[Int]] = Future(db.withConnection { implicit conn =>
    SQL("SELECT authored_by FROM accreds WHERE deleted = 0 AND event_id = {id}")
      .on("id" -> eventId)
      .as(int("authored_by").*)
      .toSet
  })

  def createAccred(accred: Accred) = Future(db.withConnection { implicit conn =>
    SqlUtils.insertOne("accreds", accred.copy(eventId = eventId))
  })

  def createAccreds(author: Int, accreds: List[Accred]) = Future(db.withConnection { implicit conn =>
    SqlUtils.insertMultiple("accreds", accreds.map(accred => accred.copy(None, authoredBy = author, status = AccredStatus.Granted, eventId = eventId)))
  })

  def updateAccred(id: Int, accred: Accred): Future[Int] = Future(db.withConnection { implicit conn =>
    val params: Seq[NamedParameter] = AccredParameterList(accred)

    SQL("UPDATE accreds SET lastname = {lastname}, firstname = {firstname}, body_name = {bodyName}, stage_name = {stageName}, must_contact_admin = {mustContactAdmin}, details = {details}, accred_type_id = {accredTypeId}, prefered_vip_desk = {preferedVipDesk}, require_real_name_on_delivery = {requireRealNameOnDelivery} WHERE accred_id = {accredId}")
      .on(params: _*)
      .executeUpdate()
  })

  def updateAccredStatus(id: Int, target: data.AccredStatus.Value, staffId: Option[Int], adminId: Option[Int], remarks: String,
                         firstName: Option[String], lastName: Option[String], number: Option[String]) = Future(db.withConnection { implicit conn =>
    val nameUpdate = if (firstName.nonEmpty) "firstname = {firstName}, lastname = {lastName}, " else ""

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

}
