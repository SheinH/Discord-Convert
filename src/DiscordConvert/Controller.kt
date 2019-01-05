package DiscordConvert

import javafx.fxml.FXML
import javafx.scene.control.*
import javafx.scene.layout.Pane
import javafx.scene.layout.VBox
import javafx.stage.FileChooser
import javafx.stage.Stage
import java.io.BufferedReader
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.StringBuilder
import java.util.concurrent.Executors

class Controller(val primaryStage : Stage){

    @FXML
    private lateinit var mainVBox : VBox
    @FXML
    private lateinit var codecToggleGroup : ToggleGroup
    @FXML
    private lateinit var fileChooseButton : Button
    @FXML
    private lateinit var convertButton : Button
    @FXML
    private lateinit var fileTextField : TextField
    @FXML
    private lateinit var progressIndicator: ProgressIndicator

    private val openFileChooser : FileChooser = FileChooser()
    private val saveFileChooser : FileChooser = FileChooser()
    private val mp4Filter : FileChooser.ExtensionFilter = FileChooser.ExtensionFilter(".mp4", "*.mp4")
    private val webmFilter : FileChooser.ExtensionFilter = FileChooser.ExtensionFilter(".webm", "*.webm")

    val selectedCodec : String
        get() = (codecToggleGroup.selectedToggle as RadioButton).text

    val selectedFile : File
        get() = File(fileTextField.text)
    var inputEnabled : Boolean = true
        set(value) {
            fun setEnabled(pane : Pane, value : Boolean){
                pane.children.forEach {
                    if(it is Pane)
                        setEnabled(it, value)
                    else
                        it.disableProperty().value = !value;
                }
            }
            setEnabled(mainVBox, value)
            progressIndicator.disableProperty().value = value;
            progressIndicator.visibleProperty().value = !value;
        }
    init{
        openFileChooser.title = "Choose Video File to Convert"
        val extFilter = FileChooser.ExtensionFilter("Video files", "*.mp4", "*.webm",
            "*.mov", "*.flv", "*.mkv", "*.avi", "*.mov", "*.m4a", "*.wmv", "*.3gp")
        openFileChooser.extensionFilters.add(extFilter)
        saveFileChooser.extensionFilters.add(mp4Filter)
    }

    @FXML
    private fun initialize()
    {
        fileChooseButton.setOnAction{ handleFileChoose() }
        convertButton.setOnAction { handleConvertButton() }
        codecToggleGroup.selectedToggleProperty().addListener { _, _, new ->
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
        val output : File? = openFileChooser.showOpenDialog(primaryStage)
        if(output != null) {
            fileTextField.text = output.absolutePath
        }
    }

    fun handleConvertButton(){
        var output : File? = saveFileChooser.showSaveDialog(primaryStage)
        if(output != null && output != selectedFile)
            ffmpeg.convertFile(selectedFile,selectedCodec,output,this)
    }

    fun disableInput(){
        fun disable(pane : Pane){
            pane.children.forEach {
                if(it is Pane)
                    disable(it)
                else
                    it.disableProperty().value = true;
            }
        }
        disable(mainVBox)
        progressIndicator.disableProperty().value = false;
        progressIndicator.visibleProperty().value = true;
    }

    object ffmpeg{
        const val targetSize = 64000;
        private fun run(vararg command: String): String {
            val pb = ProcessBuilder()
            pb.command(*command)
            pb.environment().put("PATH", System.getenv("PATH"))
            val process = pb.start()
            val errCode = process.waitFor()
            val output = output(process.inputStream)
            return output;
        }

        private fun runThread(controller : Controller, vararg command: String){
            fun doStuff(){
                controller.inputEnabled = false
                run(*command)
                controller.inputEnabled = true
            }
            val executor = Executors.newSingleThreadExecutor()
            executor.submit{doStuff()}
        }
        fun readDuration(file : File) : Double {
            val output = run("ffprobe", file.path, "-show_entries", "format=duration")
            val line2 = output.split(System.lineSeparator())[1]
            val dur = line2.substring(9,line2.length)
            return dur.toDouble()
        }

        fun convertFile(inputFile : File, codec : String, outputFile : File, controller : Controller){
            val duration = readDuration(inputFile)
            val bitrate = (targetSize / duration * .95).toInt()
            val bV = bitrate - 128
            runThread(controller,"ffmpeg", "-i", inputFile.path,
                                     "-b:v", "${bV}k",
                                     "-maxrate", "${bV}k",
                                     "-bufsize", "${bV}k",
                                     "-b:a", "128k",
                                     outputFile.path)
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