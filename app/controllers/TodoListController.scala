package controllers

import javax.inject.{Inject, Singleton}
import play.api.mvc.{AbstractController, Action, ControllerComponents}

import scala.concurrent.{ExecutionContext, Future}

/**
  * TodoListコントローラ(フロントエンド)
  */
@Singleton
class TodoListController @Inject()(cc: ControllerComponents)(implicit ec: ExecutionContext)
    extends AbstractController(cc) {

  /**
    * インデックスページを表示
    */
  def index =
    noauthorized(Action { implicit request =>
      // 200 OK ステータスで app/views/index.scala.html をレンダリングする
      Ok(views.html.index())
    })

  def register =
    noauthorized(Action { implicit request =>
      Ok(views.html.register())
    })

  def login =
    noauthorized(Action { implicit request =>
      Ok(views.html.login())
    })

  def home =
    authorized(Action { implicit request =>
      Ok(views.html.home())
    })

  def passwd =
    authorized(Action { implicit request =>
      Ok(views.html.passwd())
    })

  def deregister =
    authorized(Action { implicit request =>
      Ok(views.html.deregister())
    })

  def noauthorized[T](action: Action[T]) = Action.async(action.parser) { request =>
    request.session.get("user") match {
      case Some(_) => Future { Redirect("/home") }
      case None    => action(request)
    }
  }

  def authorized[T](action: Action[T]) = Action.async(action.parser) { request =>
    request.session.get("user") match {
      case Some(_) => action(request)
      case None    => Future { Redirect("/login") }
    }
  }

}
