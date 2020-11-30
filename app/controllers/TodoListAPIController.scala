package controllers

import javax.inject.{Inject, Singleton}
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents, Request}
import play.api.libs.json._

import scala.concurrent.{ExecutionContext, Future}
import models._

/**
  * TodoListコントローラ(API)
  */
@Singleton
class TodoListAPIController @Inject()(cc: ControllerComponents, users: Users, tasks: Tasks)(
    implicit ec: ExecutionContext
) extends AbstractController(cc) {
  case class RegisterPayload(name: String, pass: String)
  case class LoginPayload(name: String, pass: String)
  case class PasswdPayload(current: String, desired: String)
  case class DeregisterPayload(current: String)
  case class AddTaskPayload(title: String, description: String)
  case class UpdateTaskPayload(title: String, description: String, isDone: Boolean)

  implicit val taskJsonWrites = new Writes[Task] {
    def writes(task: Task) = Json.obj(
      "id"          -> task.id,
      "title"       -> task.title,
      "description" -> task.description,
      "isDone"      -> task.isDone,
      "createdAt"   -> task.createdAt
    )
  }

  def register =
    Action.async(parse.json) { implicit request =>
      implicit val json            = Json.reads[RegisterPayload]
      val payload: RegisterPayload = request.body.as
      users
        .exists(payload.name)
        .flatMap(_ match {
          case true => Future { Ok(JsString("このユーザー名は既に使われています")) }
          case false => {
            users
              .add(User(payload.name, payload.pass))
              .map(_ => Ok(JsBoolean(true)))
          }
        })
    }

  def login =
    Action.async(parse.json) { implicit request =>
      implicit val json         = Json.reads[LoginPayload]
      val payload: LoginPayload = request.body.as
      users
        .getByName(payload.name)
        .map(_ match {
          case None => Ok(JsString("ユーザー名かパスワードが間違っています"))
          case Some(user) =>
            user.comparePassword(payload.pass) match {
              case false => Ok(JsString("ユーザー名かパスワードが間違っています"))
              case true => {
                Ok(JsBoolean(true)).withNewSession.withSession("user" -> user.id.toString)
              }
            }
        })
    }

  def logout =
    Action { implicit request =>
      Ok(JsBoolean(true)).withNewSession
    }

  def userId[T](implicit request: Request[T]) = request.session("user").toInt

  def getUser[T](implicit request: Request[T]): Future[User] =
    users
      .getById(userId)
      .map(_.get)

  def passwd =
    authorized(Action.async(parse.json) { implicit request =>
      implicit val json          = Json.reads[PasswdPayload]
      val payload: PasswdPayload = request.body.as

      getUser.flatMap(user => {
        user.comparePassword(payload.current) match {
          case false => Future { Ok(JsString("パスワードが間違っています")) }
          case true =>
            users
              .save(user.withPassword(payload.desired))
              .map(_ => Ok(JsBoolean(true)))
        }
      })
    })

  def deregister =
    authorized(Action.async(parse.json) { implicit request =>
      implicit val json              = Json.reads[DeregisterPayload]
      val payload: DeregisterPayload = request.body.as

      getUser.flatMap(user => {
        user.comparePassword(payload.current) match {
          case false => Future { Ok(JsString("パスワードが間違っています")) }
          case true =>
            users
              .delete(user.id)
              .map(_ => Ok(JsBoolean(true)).withNewSession)
        }
      })
    })

  def getTasks(undone: Boolean) =
    authorized(Action.async { implicit request =>
      val list = if (undone) tasks.listUndone(userId) else tasks.list(userId)
      list.map(ts => Ok(JsArray(ts.map(t => Json.toJson(t)))))
    })

  def addTask =
    authorized(Action.async(parse.json) { implicit request =>
      implicit val json           = Json.reads[AddTaskPayload]
      val payload: AddTaskPayload = request.body.as

      tasks
        .add(Task(userId, payload.title, payload.description))
        .map(_ => Ok(JsBoolean(true)))
    })

  def deleteTask(id: Int) =
    authorized(Action.async { implicit request =>
      tasks
        .delete(userId, id)
        .map(_ => Ok(JsBoolean(true)))
    })

  def updateTask(id: Int) =
    authorized(Action.async(parse.json) { implicit request =>
      implicit val json              = Json.reads[UpdateTaskPayload]
      val payload: UpdateTaskPayload = request.body.as

      for {
        taskOpt <- tasks.getById(userId, id)
        fut     <- tasks.save(taskOpt.get.update(payload.title, payload.description, payload.isDone))
      } yield Ok(JsBoolean(true))
    })

  def authorized[T](action: Action[T]) = Action.async(action.parser) { request =>
    request.session.get("user") match {
      case Some(_) => action(request)
      case None    => Future { Unauthorized("unautorized") }
    }
  }
}
