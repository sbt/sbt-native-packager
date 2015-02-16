import javafx.application.Application
import javafx.event.{ActionEvent, EventHandler}
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.{TextArea, Button}
import javafx.scene.effect.InnerShadow
import javafx.scene.layout.{FlowPane, HBox, BorderPane, Pane}
import javafx.scene.paint.Color
import javafx.scene.text.{Font, FontWeight, Text}
import javafx.stage.{Modality, StageStyle, Stage}


/** Silly GUI app launcher. */
object ExampleApp {
  def main(args: Array[String]): Unit = {
    Application.launch(classOf[ExampleApp], args: _*)
  }
}

/** Silly GUI app. */
class ExampleApp extends Application {

  def showProps(stage: Stage) = new EventHandler[ActionEvent] {
    def handle(event: ActionEvent): Unit = {
      val win = new Stage(StageStyle.UTILITY)
      win.initModality(Modality.APPLICATION_MODAL)
      win.initOwner(stage)
      val content = new TextArea(
        sys.props.toSeq.sortBy(_._1).map(p⇒s"${p._1}=${p._2}").mkString("\n")
      )
      content.setPrefHeight(400)
      win.setScene(new Scene(content))
      win.sizeToScene()
      win.showAndWait()
    }
  }

  def start(primaryStage: Stage): Unit = {
    primaryStage.setTitle("Scala on the Desktop!")
    val stuff = new Text("Hello World")
    stuff.setFont(Font.font(null, FontWeight.BOLD, 42))
    stuff.setFill(Color.PEACHPUFF)
    val is = new InnerShadow()
    is.setOffsetX(4.0f)
    is.setOffsetY(4.0f)
    stuff.setEffect(is)
    val root = new BorderPane(stuff)
    root.setPrefWidth(400)
    root.setPrefHeight(200)

    val b = new Button("Show me props!")
    b.setOnAction(showProps(primaryStage))
    root.setBottom(b)
    BorderPane.setAlignment(b, Pos.CENTER)

    primaryStage.setScene(new Scene(root))
    primaryStage.sizeToScene()
    primaryStage.show()
  }
}

