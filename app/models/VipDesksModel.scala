package models

import anorm._
import data._
import data.returnTypes.FullAccredType
import javax.inject.{Inject, Singleton}
import utils.SqlUtils

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class VipDesksModel @Inject()(dbApi: play.api.db.DBApi, events: EventsModel)(implicit ec: ExecutionContext) {
  private val db = dbApi database "default"
  private def currentEventId = events.getCurrentEventIdSync

  def getVipDesks(event: Option[Int] = None): Future[List[VipDesk]] = Future(db.withConnection { implicit conn =>
    SQL("SELECT * FROM vip_desks WHERE event_id = {event_id}")
      .on("event_id" -> event.getOrElse(currentEventId))
      .as(VipDeskRowParser.*)
  })

  def createVipDesk(tpe: VipDesk, event: Option[Int] = None): Future[Int] = Future(db.withConnection { implicit conn =>
    SqlUtils.insertOne("vip_desks", tpe.copy(eventId = event.orElse(Some(currentEventId))))
  })

}
