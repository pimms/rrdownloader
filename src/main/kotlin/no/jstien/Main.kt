package no.jstien

import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.Semaphore

fun main(args: Array<String>) {
    val semaphore = Semaphore(10)

    println("Hello, World")
    val repo = UpstreamRepository()
    val episodes: List<EpisodeManifest> = repo.getEpisodeIndex()

    val dir = System.getProperty("user.home") + "/radioresepsjonen"
    println("Target dir: ${dir}")
    File(dir).mkdirs()

    var accountedFor = 0
    episodes.forEach{ manifest ->
        val filename = manifest.getFormattedName()
        val path = dir + "/" + filename
        val file = File(path)
        if (file.exists()) {
            println("Episode ${filename} already downloaded")
        } else {
            semaphore.acquire()
            manifest.url.httpGet().responseString { request, response, result ->
                when (result) {
                    is Result.Failure -> {
                        println("ERROR: Failed to download episode ${filename}");
                    }
                    is Result.Success -> {
                        val outStream = FileOutputStream(file)
                        try {
                            outStream.write(response.data)
                            println("Downloaded episode ${filename}")
                        } catch (e: RuntimeException) {
                            println("ERROR: Failed to flush ${filename} to disk")
                        } finally {
                            outStream.close()
                        }
                    }
                }

                accountedFor++
                semaphore.release()
            }
        }
    }

    while (accountedFor < episodes.size) {
        Thread.sleep(100)
    }
}

