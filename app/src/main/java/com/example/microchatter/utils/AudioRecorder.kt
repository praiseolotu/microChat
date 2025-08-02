package com.example.microchatter.utils

import android.content.Context
import android.media.MediaRecorder
import android.os.CountDownTimer
import java.io.File

class AudioRecorder(private val context: Context) {

    private var recorder: MediaRecorder? = null
    private var outputFile: File? = null
    private var timer: CountDownTimer? = null
    var onRecordingFinished: ((File) -> Unit)? = null

    fun startRecording() {
        stopRecording()

        val file = File.createTempFile("audio_", ".aac", context.cacheDir)
        outputFile = file

        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(file.absolutePath)
            prepare()
            start()
        }

        timer = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {}
            override fun onFinish() {
                stopRecording()
            }
        }.start()
    }

    fun stopRecording() {
        try {
            recorder?.apply {
                stop()
                release()
            }
            recorder = null
            timer?.cancel()
            outputFile?.let { onRecordingFinished?.invoke(it) }
        } catch (_: Exception) {}
    }

    fun cancelRecording() {
        recorder?.apply {
            stop()
            release()
        }
        recorder = null
        timer?.cancel()
        outputFile?.delete()
    }
}