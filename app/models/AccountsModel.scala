package models

import java.security.SecureRandom

import anorm.SqlParser.scalar
import anorm._
import javax.inject.{Inject, Singleton}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AccountsModel @Inject()(dbApi: play.api.db.DBApi, events: EventsModel)(implicit ec: ExecutionContext) {

  private val db = dbApi database "default"

  private def eventId = events.getCurrentEventIdSync

  object KeyGenerator {
    private final val random = new SecureRandom()

    def generateKey = {
      (1 to 8).map(_ => random.nextInt(16).toHexString).mkString.toUpperCase()
    }
  }

  def createStaffAccount(desk: Int, name: String, author: Int) = Future(db.withConnection { implicit conn =>
    SQL("INSERT INTO staff_accounts(event_id, vip_desk_id, name, authored_by) VALUES ({event}, {desk}, {name}, {author})")
      .on("event" -> eventId, "desk" -> desk, "name" -> name, "author" -> author)
      .executeInsert(scalar[Int].singleOpt)
  })

  def createOneTimeKey(user: Int) = Future(db.withConnection { implicit conn =>
    val key = KeyGenerator.generateKey
    val result = SQL("INSERT INTO admin_one_time_delegations(admin_user_id, grant_key) VALUES ({adm}, {key})")
      .on("adm" -> user, "key" -> key)
      .executeUpdate()

    if (result == 1) Some(key) else None
  })

  val MaxDurationMinutes = 1

  def getOneTimeKey(key: String): Future[Option[Int]] = Future(db.withConnection { implicit conn =>
    /* SELECT * FROM admin_one_time_delegations WHERE ADDTIME(creation_time, MAKETIME(0, 1, 0)) > CURRENT_TIMESTAMP */
    val result = SQL("SELECT admin_user_id FROM admin_one_time_delegations WHERE ADDTIME(creation_time, MAKETIME(0, {t}, 0)) > CURRENT_TIMESTAMP AND grant_key = {k}")
        .on("k" -> key, "t" -> MaxDurationMinutes)
        .as(scalar[Int].singleOpt)

    SQL("DELETE FROM admin_one_time_delegations WHERE grant_key = {k}")
        .on("k" -> key)
        .executeUpdate()

    result
  })
}
