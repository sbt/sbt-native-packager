package controllers

import com.google.inject.Inject
import play.api.mvc._

class Application @Inject() (cc: ControllerComponents) extends AbstractController(cc) {

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

}
