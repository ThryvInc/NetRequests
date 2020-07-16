package co.lithobyte.networkrequests.network

import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.android.volley.VolleyError

interface NetworkErrorFunctionProvider {
    fun errorFunction(): (VolleyError?) -> Unit
}

class PrintingNetworkErrorFunctionProvider: NetworkErrorFunctionProvider {
    override fun errorFunction(): (VolleyError?) -> Unit {
        return { error: VolleyError? ->
            error?.printStackTrace()
            val networkResponse = error?.networkResponse
            if (networkResponse != null && networkResponse.statusCode > 299) {
                Log.e("Server Error", "Status code: ${networkResponse.statusCode}")
                if (error.message == null) {
                    Log.e("Server Error", "Headers: ${networkResponse.allHeaders}")
                } else {
                    Log.e("Server Error", "Message: ${error.message}")
                }
            }
        }
    }
}

class DebugNetworkErrorFunctionProvider(val context: Context?): NetworkErrorFunctionProvider {
    override fun errorFunction(): (VolleyError?) -> Unit {
        return { error: VolleyError? ->
            val title: String
            val text: String?
            val networkResponse = error?.networkResponse
            if (networkResponse != null && networkResponse.statusCode > 299) {
                title = "Server Error ${networkResponse.statusCode}"
                if (error.message == null) {
                    text = "Headers: ${networkResponse.headers}"
                } else {
                    text = "Message: ${error.message}"
                }
            } else {
                if (error?.message != null) {
                    title = "Error"
                    text = error.message
                } else {
                    title = "Offline"
                    text = "It looks like you're offline. Please check your internet connection."
                }
            }
            if (context != null) {
                AlertDialog.Builder(context)
                    .setTitle(title)
                    .setMessage(text)
                    .setPositiveButton("Ok",{ dialog, _ -> dialog.dismiss() })
                    .create()
                    .show()
            }
        }
    }
}

class ToastNetworkErrorFunctionProvider(val context: Context?): NetworkErrorFunctionProvider {
    override fun errorFunction(): (VolleyError?) -> Unit {
        return { error: VolleyError? ->
            val text: String?
            val networkResponse = error?.networkResponse
            if (networkResponse != null && networkResponse.statusCode > 299) {
                text = "Server Error ${networkResponse.statusCode}"
            } else {
                if (error?.message != null) {
                    text = error.message
                } else {
                    text = "It looks like you're offline. Please check your internet connection."
                }
            }
            if (context != null) {
                Toast.makeText(context, text, Toast.LENGTH_LONG).show()
            }
        }
    }
}
