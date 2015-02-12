import javafx.application.Application
import javafx.scene.control.Label
import javafx.scene.effect.InnerShadow
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import javafx.scene.text.{Text, Font, FontWeight}
import javafx.scene.{Group, Scene}
import javafx.stage.Stage

object ExampleApp {
  def main(args: Array[String]): Unit = {
    Application.launch(classOf[ExampleApp], args: _*)
  }
}
/** Silly GUI app. */
class ExampleApp extends Application {
  def start(primaryStage: Stage): Unit = {
    primaryStage.setTitle("Scala on the Desktop!")
    val stuff = new Text(90, 100, "Hello World")
    stuff.setFont(Font.font(null, FontWeight.BOLD, 36))
    stuff.setFill(Color.PEACHPUFF)
    val is = new InnerShadow()
    is.setOffsetX(4.0f)
    is.setOffsetY(4.0f)
    stuff.setEffect(is)
    val root = new Pane(stuff)
    root.setPrefWidth(400)
    root.setPrefHeight(200)
    primaryStage.setScene(new Scene(root))
    primaryStage.sizeToScene()
    primaryStage.show()
  }
}

