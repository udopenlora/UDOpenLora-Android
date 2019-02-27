package com.sdc.udn.openlora.activity.activity

import ai.api.AIServiceException
import ai.api.android.AIConfiguration
import ai.api.android.AIService
import ai.api.model.AIRequest
import ai.api.model.AIResponse
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.AsyncTask
import android.os.Bundle
import android.speech.RecognizerIntent
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.sdc.udn.openlora.R
import com.sdc.udn.openlora.activity.utils.Constant

open class BaseActivity : AppCompatActivity() {
    var aiService: AIService? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // SetUp DialogFlow.
        val config = AIConfiguration(
            Constant.KEY_DIALOGFLOW,
            ai.api.AIConfiguration.SupportedLanguages.English,
            AIConfiguration.RecognitionEngine.System
        )

        aiService = AIService.getService(this, config)
    }

    /**
     * Speech to talk.
     */
    public fun speechTalk() {
        if (requestPermission(arrayOf<String>(Manifest.permission.RECORD_AUDIO))) {
            showAlert(R.string.need_permission_micro)

            return
        }

        openSpeech()
    }

    /**
     * Open Speech.
     */
    private fun openSpeech() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Constant.VietNam)
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.bot_message_need_help))
        startActivityForResult(intent, Constant.REQUEST_CODE_SPEECH_TO_TEXT)
    }

    /**
     * Check Permission was Granted.
     */
    private fun requestPermission(permissions: Array<String>): Boolean {
        val noGrantPermission = mutableListOf<String>()

        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                noGrantPermission.add(permission)
            }
        }

        if (noGrantPermission.size > 0) {
            ActivityCompat.requestPermissions(this, noGrantPermission.toTypedArray(), Constant.REQUEST_CODE_PERMISSION)
            return true
        }

        return false
    }

    /**
     * Confirm Permissions Result.
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (permissions.isNotEmpty() && grantResults.isNotEmpty()) {
            if (permissions[0] == Manifest.permission.RECORD_AUDIO)
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    showAlert(R.string.need_permission_micro)
                } else {
                    openSpeech()
                }
        }
    }


    /**
     * Receive result from speech.
     */
    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == Constant.REQUEST_CODE_SPEECH_TO_TEXT) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                endListenerSpeak(result)
            }
        }
    }

    /**
     * Call request AI to do action.
     */
    private fun endListenerSpeak(result: ArrayList<String>) {
        if (result.size == 0) {
            showAlert(R.string.bot_message_error_speech_to_text)
            return
        }

        sendTextRequest(result[0])
    }

    /**
     * Send Text To AI.
     */
    private fun sendTextRequest(message: String) {
        TextRequestAsyncTask().execute(message)
    }

    public fun showAlert(message: String) {
        Toast.makeText(this@BaseActivity, message, Toast.LENGTH_LONG).show()
    }

    public fun showAlert(message: Int) {
        showAlert(getString(message))
    }

    @SuppressLint("StaticFieldLeak")
    internal inner class TextRequestAsyncTask : AsyncTask<String, Void, AIResponse>() {

        override fun onPreExecute() {

        }

        override fun doInBackground(vararg strings: String): AIResponse? {
            try {
                val aiRequest = AIRequest()
                aiRequest.setQuery(strings[0])

                return aiService?.textRequest(aiRequest)
            } catch (e: AIServiceException) {
                e.printStackTrace()
            }

            return null
        }

        override fun onPostExecute(aiResponse: AIResponse?) {
            if (isCancelled) {
                return
            }

            if (aiResponse == null) {
                showAlert(R.string.bot_message_error_speech_to_text)
                return
            }

            val speech = aiResponse.result.fulfillment.speech

            if (speech.isEmpty()) {
                showAlert(R.string.bot_message_error_speech_to_text)
                return
            }

            //val mss = speech.replace("</br>".toRegex(), "\n")
            showAlert(speech)
        }
    }
}