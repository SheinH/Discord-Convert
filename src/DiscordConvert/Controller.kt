package DiscordConvert

import javafx.fxml.FXML
import javafx.scene.control.*
import javafx.stage.FileChooser
import javafx.stage.Stage
import java.io.BufferedReader
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.StringBuilder
import java.nio.file.Path

class Controller(val primaryStage : Stage){

    @FXML
    private lateinit var codecToggleGroup : ToggleGroup
    @FXML
    private lateinit var fileChooseButton : Button
    @FXML
    private lateinit var convertButton : Button
    @FXML
    private lateinit var fileTextField : TextField

    private val openFileChooser : FileChooser = FileChooser()
    private val saveFileChooser : FileChooser = FileChooser()
    private val mp4Filter : FileChooser.ExtensionFilter = FileChooser.ExtensionFilter(".mp4", "*.mp4")
    private val webmFilter : FileChooser.ExtensionFilter = FileChooser.ExtensionFilter(".webm", "*.webm")

    val selectedCodec : String
        get() = (codecToggleGroup.selectedToggle as RadioButton).text

    val selectedFile : Path
        get() = Path.of(fileTextField.text)

    init{
        openFileChooser.title = "Choose Video File to Convert"
        val extFilter = FileChooser.ExtensionFilter("Video files", "*.mp4", "*.webm",
            "*.mov", "*.flv", "*.mkv", "*.avi", "*.mov", "*.m4a", "*.wmv", "*.3gp")
        openFileChooser.extensionFilters.add(extFilter)
    }

    @FXML
    private fun initialize()
    {
        fileChooseButton.setOnAction{ handleFileChoose() }
        convertButton.setOnAction { handleConvertButton() }
        codecToggleGroup.selectedToggleProperty().addListener { observable, old, new ->
            val button  = new as RadioButton
            saveFileChooser.extensionFilters.clear()
            saveFileChooser.extensionFilters.add(
                when(button.text){
                    "webm"  -> webmFilter
                    else    -> mp4Filter
                }
            )
        }
    }

    fun handleFileChoose(){
        val output = openFileChooser.showOpenDialog(primaryStage)
        if(output != null) {
            fileTextField.text = output.absolutePath
            println("Duration: ${ffmpeg.readDuration(output)}s")
        }
    }

    fun handleConvertButton(){
        println(selectedFile)

        saveFileChooser.showSaveDialog(primaryStage)
    }

    object ffmpeg{
        private fun run(vararg command: String): String {
            val pb = ProcessBuilder()
            pb.command(*command)
            pb.environment().put("PATH", System.getenv("PATH"))
            val process = pb.start()
            val errCode = process.waitFor()
            val output = output(process.errorStream)
            println("Echo command executed, any errors? ${ if(errCode == 0) "No" else "Yes" }")
            println("Echo output: ${output}")
            return output;
        }

        fun readDuration(file : File) : Double {
            val output = run("ffprobe", file.absolutePath)
            println("OUTPUT FROM PROGRAM: ${output}")
            return 3.14
        }

        private fun output(inputStream : InputStream) : String {
            val stringBuilder = StringBuilder()
            val bufferedReader = BufferedReader(InputStreamReader(inputStream))
            var line : String? = bufferedReader.readLine()
            while(line != null){
                stringBuilder.append(line).append(System.lineSeparator())
                line = bufferedReader.readLine()
            }
            bufferedReader.close()
            return stringBuilder.toString()
        }
    }
}