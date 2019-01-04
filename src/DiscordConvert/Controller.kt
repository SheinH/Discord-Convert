package DiscordConvert

import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.RadioButton
import javafx.scene.control.TextField
import javafx.scene.control.ToggleGroup
import javafx.stage.FileChooser
import javafx.stage.Stage
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.file.Path
import java.util.concurrent.Executors
import java.util.function.Consumer

class Controller(val primaryStage : Stage){

    @FXML
    private lateinit var toggleGroup : ToggleGroup
    @FXML
    private lateinit var fileChooseButton : Button
    @FXML
    private lateinit var convertButton : Button
    @FXML
    private lateinit var fileTextField : TextField

    private val fc : FileChooser = FileChooser()

    val selectedCodec : String
        get() = (toggleGroup.selectedToggle as RadioButton).text

    val selectedFile : Path
        get() = Path.of(fileTextField.text)

    init{
        fc.title = "Choose Video File to Convert"
        val extFilter = FileChooser.ExtensionFilter("Video files", "*.mp4", "*.webm",
            "*.mov", "*.flv", "*.mkv", "*.avi", "*.mov", "*.m4a", "*.wmv", "*.3gp")
        fc.extensionFilters.add(extFilter)
    }

    @FXML
    private fun initialize()
    {
        fileChooseButton.setOnAction{ handleFileChoose() }
        convertButton.setOnAction {  }
    }

    fun handleFileChoose(){
        val output = fc.showOpenDialog(primaryStage)
        if(output != null)
            fileTextField.text = output.absolutePath
    }

    fun handleConvertButton(){
        println(selectedFile)
    }

    fun run(command: String): String {
        class StreamGobbler(private val inputStream: InputStream, private val consumer: Consumer<String>) : Runnable {
            override fun run() {
                BufferedReader(InputStreamReader(inputStream)).lines()
                    .forEach(consumer)
            }
        }
        val builder = ProcessBuilder()
        if (isWindows) {
            builder.command("cmd.exe", "/c", command)
        } else {
            builder.command("sh", "-c", command)
        }
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
}