package models

import anorm.SqlParser.scalar
import anorm._
import javax.inject.{Inject, Singleton}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class StaffAccountsModel @Inject()(dbApi: play.api.db.DBApi, events: EventsModel)(implicit ec: ExecutionContext) {

  private val db = dbApi database "default"

  private def eventId = events.getCurrentEventIdSync

  def createStaffAccount(desk: Int, name: String, author: Int) = Future(db.withConnection { implicit conn =>
    SQL("INSERT INTO staff_accounts(event_id, vip_desk_id, name, autored_by) VALUES ({event}, {desk}, {name}, {author})")
      .on("event" -> eventId, "desk" -> desk, "name" -> name, "author" -> author)
      .executeInsert(scalar[Int].singleOpt)
  })
}
