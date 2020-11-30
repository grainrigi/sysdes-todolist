package models

import javax.inject.{Inject, Singleton}

import scala.concurrent.{ExecutionContext, Future}
import play.api.db.slick.{DatabaseConfigProvider => DBConfigProvider}

/**
  * task テーブルへの Accessor
  */
@Singleton
class Tasks @Inject()(dbcp: DBConfigProvider)(implicit ec: ExecutionContext) extends Dao(dbcp) {

  import profile.api._
  import utility.Await

  val table = "tasks"

  /**
    * DB上に保存されている全てのタスクを取得する
    * @return
    */
  def list(userId: Int): Future[Seq[Task]] = db.run(
    sql"SELECT id, user_id, title, description, is_done, created_at FROM #$table WHERE user_id = $userId".as[Task]
  )

  def listUndone(userId: Int): Future[Seq[Task]] = db.run(
    sql"SELECT id, user_id, title, description, is_done, created_at FROM #$table WHERE user_id = $userId AND is_done = FALSE"
      .as[Task]
  )

  def add(task: Task) =
    db.run(
      sqlu"INSERT INTO #$table (user_id, title, description, is_done) VALUES (${task.userId},${task.title},${task.description},${task.isDone})"
    )

  def save(task: Task) =
    db.run(
      sqlu"UPDATE #$table SET title = ${task.title}, description = ${task.description}, is_done = ${task.isDone} WHERE id = ${task.id}"
    )

  def delete(userId: Int, id: Int) =
    db.run(
      sqlu"DELETE FROM #$table WHERE id = $id AND user_id = $userId"
    )

  def getById(userId: Int, id: Int): Future[Option[Task]] =
    db.run(
        sql"SELECT id, user_id, title, description, is_done, created_at FROM #$table WHERE user_id = $userId AND id = $id"
          .as[Task]
      )
      .map(_.headOption)
}
