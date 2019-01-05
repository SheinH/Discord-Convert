package DiscordConvert

import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.StringBuilder

fun main(){
    val pb = ProcessBuilder()
    pb.command("/usr/local/bin/ffmpeg", "-version")
    println("Run echo command")
    val process = pb.start()
    val errCode = process.waitFor()
    println("Echo command executed, any errors? ${ if(errCode == 0) "No" else "Yes" }")
    System.out.println("Echo output: ${output(process.inputStream)}")
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