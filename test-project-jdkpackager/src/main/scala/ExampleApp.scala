import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.stage.Stage

object ExampleApp {
  def main(args: Array[String]): Unit = {
    Application.launch(classOf[ExampleApp], args: _*)
  }
}
/** Silly GUI app. */
class ExampleApp extends Application {
  def start(primaryStage: Stage): Unit = {
    val root = new Scene(new Button("Boo!"))
    primaryStage.setScene(root)
    primaryStage.show()
  }
}

