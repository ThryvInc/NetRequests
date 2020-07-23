package co.lithobyte.networkrequests.network

import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.android.volley.NoConnectionError
import com.android.volley.TimeoutError
import com.android.volley.VolleyError

interface NetworkErrorFunctionProvider {
    fun errorFunction(): (VolleyError?) -> Unit
}

fun noResponseErrorMessages(error: VolleyError?): Pair<String, String> {
    val title: String
    val text: String
    if (error?.message != null) {
        title = "Error"
        text = error.message ?: ""
    } else if (error is NoConnectionError) {
        title = "Offline"
        text = "It looks like you're offline. Please check your internet connection."
    } else if (error is TimeoutError) {
        title = "Timeout"
        text = "Request timed out. Please check your internet connection; if this keeps occurring, please contact us."
    } else {
        title = "Error"
        if (error != null) {
            text = "${error::class.java.simpleName} occurred."
        } else {
            text = "An unknown error occurred."
        }
    }
    return Pair(title, text)
}

class PrintingNetworkErrorFunctionProvider: NetworkErrorFunctionProvider {
    override fun errorFunction(): (VolleyError?) -> Unit {
        return { error: VolleyError? ->
            error?.printStackTrace()
            val networkResponse = error?.networkResponse
            if (networkResponse != null && networkResponse.statusCode > 299) {
                Log.e("Server Error", "Response: $networkResponse")
                Log.e("Server Error", "Status code: ${networkResponse.statusCode}")
                Log.e("Server Error", "Headers: ${networkResponse.allHeaders}")
                if (error.message != null) {
                    Log.e("Server Error", "Message: ${error.message}")
                }
            } else if (error != null) {
                Log.e("Server Error", error.toString())
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
                val pair = noResponseErrorMessages(error)
                title = pair.first
                text = pair.second
            }
            if (context != null) {
                AlertDialog.Builder(context)
                    .setTitle(title)
                    .setMessage(text)
                    .setPositiveButton("Ok") { dialog, _ -> dialog.dismiss() }
                    .create()
                    .show()
            }
        }
    }
}

class ToastDebugNetworkErrorFunctionProvider(val context: Context?): NetworkErrorFunctionProvider {
    override fun errorFunction(): (VolleyError?) -> Unit {
        return { error: VolleyError? ->
            val text: String?
            val networkResponse = error?.networkResponse
            if (networkResponse != null && networkResponse.statusCode > 299) {
                text = "Server Error ${networkResponse.statusCode}"
            } else {
                val pair = noResponseErrorMessages(error)
                text = pair.second
            }
            if (context != null) {
                Toast.makeText(context, text, Toast.LENGTH_LONG).show()
            }
        }
    }
}

enum class DisplayType {
    log, toast, alert
}

open class ServerCodeErrorFunctionProvider(val context: Context?,
                                           val displayType: DisplayType = DisplayType.toast):
    NetworkErrorFunctionProvider {

    var errorMap: HashMap<Int, String> = hashMapOf(
        400 to "Bad request",
        401 to "Unauthorized",
        402 to "Payment required",
        403 to "Forbidden",
        404 to "Not found",
        405 to "HTTP method not allowed",
        406 to "Content type in Accept header is unavailable",
        408 to "Request timed out",
        409 to "Conflict in requested resource",
        410 to "Resource is permanently unavailable",
        411 to "Length required",
        412 to "Precondition failed",
        413 to "Payload too large",
        414 to "URI too long",
        415 to "Unsupported media type",
        416 to "Range not satisfiable",
        417 to "Expectation failed",
        418 to "This server is, in fact, a teapot",
        420 to "Error 420, dank dude",
        421 to "Unauthorized",
        422 to "Unable to process payload",
        429 to "Too many requests",
        431 to "Headers too large",
        451 to "Unavailable for legal reasons",

        500 to "Internal server error",
        501 to "This functionality is not implemented",
        502 to "Bad internet gateway",
        503 to "Server currently unavailable",
        505 to "HTTP version not supported",
        511 to "Network authentication required"
    )

    open fun errorStrings(code: Int): Pair<String, String> {
        val title = "Error $code"
        val text = errorMap[code] ?: "Unknown error."
        return Pair(title, text)
    }

    open override fun errorFunction(): (VolleyError?) -> Unit {
        return {
            val title: String
            val text: String
            val networkResponse = it?.networkResponse
            if (networkResponse != null && networkResponse.statusCode > 299) {
                val strings = errorStrings(networkResponse.statusCode)
                title = strings.first
                text = strings.second
            } else {
                val pair = noResponseErrorMessages(it)
                title = pair.first
                text = pair.second
            }

            when (displayType) {
                DisplayType.log -> {
                    PrintingNetworkErrorFunctionProvider().errorFunction().invoke(it)
                }
                DisplayType.toast -> {
                    if (context != null) {
                        Toast.makeText(context, "$title: $text", Toast.LENGTH_LONG).show()
                    }
                }
                DisplayType.alert -> {
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
    }
}

open class GenericLoginNetworkErrorFunctionProvider(context: Context?,
                                                    displayType: DisplayType = DisplayType.toast):
    ServerCodeErrorFunctionProvider(context, displayType) {
    init {
        errorMap[401] = "Please check that your credentials are valid and try again."
        errorMap[403] = "Please check that your credentials are valid and try again."
    }
}

open class EmailLoginNetworkErrorFunctionProvider(context: Context?,
                                                  displayType: DisplayType = DisplayType.toast):
    ServerCodeErrorFunctionProvider(context, displayType) {
    init {
        errorMap[401] = "Invalid email/password combination. Please try again."
        errorMap[403] = "Invalid email/password combination. Please try again."
    }
}

open class UsernameLoginNetworkErrorFunctionProvider(context: Context?,
                                                     displayType: DisplayType = DisplayType.toast):
    ServerCodeErrorFunctionProvider(context, displayType) {
    init {
        errorMap[401] = "Invalid username/password combination. Please try again."
        errorMap[403] = "Invalid username/password combination. Please try again."
    }
}
