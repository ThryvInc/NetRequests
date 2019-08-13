package co.lithobyte.networkrequests.network

open class ServerConfiguration(val scheme: String = "https",
                               val host: String,
                               val apiBaseRoute: String = "api/v1") {

    fun urlForEndpoint(endpoint: String): String {
        return "$scheme://$host/$apiBaseRoute/$endpoint"
    }
}
