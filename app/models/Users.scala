package models

import javax.inject.{Inject, Singleton}

import scala.concurrent.{ExecutionContext, Future}
import play.api.db.slick.{DatabaseConfigProvider => DBConfigProvider}

/**
  * task テーブルへの Accessor
  */
@Singleton
class Users @Inject()(dbcp: DBConfigProvider)(implicit ec: ExecutionContext) extends Dao(dbcp) {

  import profile.api._

  val table = "users"

  /**
    * ユーザーが存在するか確認
    * @return
    */
  def exists(name: String): Future[Boolean] =
    for {
      result <- db.run(sql"SELECT COUNT(*) FROM #$table WHERE name = $name".as[Int])
    } yield result.head > 0

  def add(user: User) =
    db.run(
      sqlu"INSERT INTO #$table (name, password, password_salt) VALUES (${user.name},${user.password},${user.passwordSalt})"
    )

  def save(user: User) =
    db.run(
      sqlu"UPDATE #$table SET name = ${user.name}, password = ${user.password}, password_salt = ${user.passwordSalt} WHERE id = ${user.id}"
    )

  def delete(id: Int) =
    db.run(
      sqlu"DELETE FROM #$table WHERE id = $id"
    )

  def getByName(name: String) =
    db.run(sql"SELECT * FROM #$table WHERE name = $name".as[User])
      .map(_.headOption)

  def getById(id: Int) =
    db.run(sql"SELECT * FROM #$table WHERE id = $id".as[User])
      .map(_.headOption)
}
