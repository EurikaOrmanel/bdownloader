package com.lucrativeworm.amd.utils

import android.content.Context
import android.util.Log
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader

class Aria2Util(private val context: Context) {

    fun prepareAria2(): File {
        val inputStream = context.assets.open("aria2c")
        val outFile = File(context.filesDir, "aria2c")
        if (!outFile.exists()) {
            inputStream.use { input ->
                FileOutputStream(outFile).use { output ->
                    input.copyTo(output)
                }
            }
        }
        if (outFile.setExecutable(true)) {
            Log.d(this.javaClass.canonicalName, "File is now executable")
        }

        try {
            Runtime.getRuntime().exec("chmod 755 ${outFile.absolutePath}").waitFor()
        } catch (e: Exception) {
            Log.e("Aria2", "chmod failed", e)
        }

        val file = File(context.filesDir, "aria2c")
        if (file.canExecute()) {
            Log.d("Aria2", "Executable!")
        } else {
            Log.e("Aria2", "Not executable.")
        }
        val process = ProcessBuilder(outFile.absolutePath, "--version")
            .redirectErrorStream(true)
            .start()

        Log.d(this.javaClass.canonicalName, process.outputStream.toString())
        return outFile
    }

    fun runAria2() {
        val aria2Binary = prepareAria2()

        val command = listOf(
            aria2Binary.absolutePath,
            "--dir=${context.filesDir.absolutePath}",
            "https://images.pexels.com/photos/31495834/pexels-photo-31495834/free-photo-of-artistic-reflection-of-face-in-water-and-glass.jpeg?auto=compress&cs=tinysrgb&w=600&lazy=load"
        )

        val process = ProcessBuilder(command)
            .redirectErrorStream(true)
            .start()

        // Read output (optional)
        val reader = BufferedReader(InputStreamReader(process.inputStream))
        Thread {
            reader.lineSequence().forEach {
                Log.d("Aria2", it)
            }
        }.start()
    }

}