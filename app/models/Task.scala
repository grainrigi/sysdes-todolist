package models

import java.sql.Timestamp

import play.api.mvc.Request

/**
  * Domain model of task
  * @param id          ID
  * @param userId      ID
  * @param title       タスク名
  * @param description タスクの説明
  * @param isDone      完了状態
  * @param createdAt   作成日時
  */
case class Task(id: Int, userId: Int, title: String, description: String, isDone: Boolean, createdAt: Timestamp) {
  def update(title: String, description: String, isDone: Boolean): Task =
    Task(id, userId, title, description, isDone, createdAt)
}

object Task extends DomainModel[Task] {
  import slick.jdbc.GetResult
  implicit def getResult: GetResult[Task] = GetResult(
    r => Task(r.nextInt, r.nextInt, r.nextString, r.nextString, r.nextBoolean, r.nextTimestamp)
  )

  def apply(userId: Int, title: String, description: String): Task =
    Task(0, userId, title, description, false, null)
}
