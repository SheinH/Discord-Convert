package helloworld

import javafx.application.Application
import javafx.event.EventHandler
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.layout.StackPane
import javafx.stage.Stage
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.util.concurrent.Executors
import java.util.function.Consumer


class HelloWorld : Application() {

    override fun start(primaryStage: Stage) {
        primaryStage.title = "Hello World!"
        val btn = Button()
        btn.text = "Say 'Hello World'"
        btn.onAction = EventHandler { println("Hello World!") }

        val root = StackPane()
        root.children.add(btn)
        primaryStage.scene = Scene(root, 300.0, 250.0)
        primaryStage.show()
    }
}

private class StreamGobbler(private val inputStream: InputStream, private val consumer: Consumer<String>) : Runnable {

    override fun run() {
        BufferedReader(InputStreamReader(inputStream)).lines()
            .forEach(consumer)
    }
}

val isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");

fun something(command: String): String {
    val builder = ProcessBuilder()
    if (isWindows) {
        builder.command("cmd.exe", "/c", command)
    } else {
        builder.command("sh", "-c", command)
    }
    //builder.directory(File(System.getProperty("user.home")))
    val process = builder.start()
    var stringBuilder = StringBuilder()
    val streamGobbler = StreamGobbler(process.inputStream, Consumer { stringBuilder.append(it) })
    val executor = Executors.newSingleThreadExecutor()
    executor.submit(streamGobbler)
    val exitCode = process.waitFor()
    assert(exitCode == 0)
    executor.shutdown()
    return stringBuilder.toString()
}

fun main(args: Array<String>) {
    println(something("pwd"))
}