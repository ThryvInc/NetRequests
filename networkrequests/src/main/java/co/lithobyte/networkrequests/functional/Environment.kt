package co.lithobyte.networkrequests.functional


import co.lithobyte.networkrequests.network.ServerConfiguration
import com.google.gson.Gson

data class Environment(val serverConfiguration: ServerConfiguration, val gson:Gson = Gson())
