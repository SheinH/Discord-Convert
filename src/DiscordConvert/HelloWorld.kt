package DiscordConvert

import javafx.application.Application
import javafx.scene.Scene
import javafx.stage.Stage
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.util.concurrent.Executors
import java.util.function.Consumer
import javafx.fxml.FXMLLoader
import javafx.scene.Parent


val isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");

class HelloWorld : Application() {
    private lateinit var controller : Controller

    override fun start(primaryStage: Stage) {
        controller = Controller(primaryStage)
        val loader = FXMLLoader(javaClass.getResource("Layout.fxml"))
        loader.setController(controller)
        val root : Parent = loader.load()
        primaryStage.title = "Discord Convert"
        primaryStage.scene = Scene(root)
        primaryStage.isResizable = false
        primaryStage.show()
    }
}

fun main(args: Array<String>) {
    Application.launch(HelloWorld::class.java,*args)
}