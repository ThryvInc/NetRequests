package co.lithobyte.networkrequests.network

import android.content.Context
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley

open class VolleyManager {
    companion object {
        private var queue: RequestQueue? = null
        var shouldStub: (FunctionalJsonRequest<*>) -> Boolean = { false }
        var shouldUseSameThread = false

        fun init(context: Context, shouldForceNewQueue: Boolean = false) {
            if (queue == null || shouldForceNewQueue) queue = Volley.newRequestQueue(context.applicationContext)
        }

        fun <T> addToQueue(request: Request<T>, shouldCache: Boolean = false) {
            if (request is FunctionalJsonRequest<*>) {
                if (shouldStub(request)) {
                    stubRequest(request, shouldUseSameThread)
                    return
                }
            }
            request.setShouldCache(shouldCache)
            if (!shouldCache) {
                queue?.cache?.clear()
            }
            queue?.add(request)
        }
    }
}
