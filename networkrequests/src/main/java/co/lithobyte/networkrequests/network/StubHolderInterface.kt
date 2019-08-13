package co.lithobyte.networkrequests.network

import co.lithobyte.functionalkotlin.FunctionalAsyncTask
import co.lithobyte.functionalkotlin.into
import co.lithobyte.functionalkotlin.o
import java.util.*

interface StubHolderInterface {
    val statusCode: Int
    val stubFileName: String?
    val stubString: String?
}

open class StubHolder(override val statusCode: Int = 200,
                      override val stubFileName: String? = null,
                      override val stubString: String? = null) : StubHolderInterface

fun <T> stubRequest(request: FunctionalJsonRequest<T>, shouldUseSameThread: Boolean = false) {
    if (300 > request.stubHolder?.statusCode ?: Int.MAX_VALUE) {
        stubRequestSuccess(request, shouldUseSameThread = shouldUseSameThread)
    } else {

    }
}

fun <T> stubRequestSuccess(request: FunctionalJsonRequest<T>,
                           classLoader: ClassLoader? = FileUtil::class.java.classLoader,
                           shouldUseSameThread: Boolean = false) {
    val stubber = request.parseResponseString o request.listener
    val stubHolder = request.stubHolder ?: return

    val fileName = stubHolder.stubFileName
    val string = stubHolder.stubString
    if (string != null) {
        if (shouldUseSameThread) {
            string into stubber
        } else {
            FunctionalAsyncTask({ string into request.parseResponseString }, { it into request.listener }).execute()
        }
    } else if (fileName != null && classLoader != null) {
        val stubString = FileUtil.readStubStringFromFile(fileName, classLoader)
        if (shouldUseSameThread) {
            stubString into stubber
        } else {
            FunctionalAsyncTask({ stubString into request.parseResponseString }, { it into request.listener }).execute()
        }
    }
}

open class FileUtil {
    companion object {
        fun readStubStringFromFile(fileName: String,
                                   classLoader: ClassLoader? = FileUtil::class.java.classLoader): String {
            val raw = classLoader?.getResourceAsStream("stubfiles/$fileName")
            return Scanner(raw).useDelimiter("\\A").next()
        }
    }
}
