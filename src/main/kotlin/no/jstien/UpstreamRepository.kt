package no.jstien

import com.github.kittinunf.fuel.*
import com.github.kittinunf.result.Result
import org.w3c.dom.Document
import org.w3c.dom.Node
import org.xml.sax.InputSource
import java.io.StringReader
import java.util.*
import javax.xml.parsers.DocumentBuilderFactory

class UpstreamRepository() {
    private val url = "http://podkast.nrk.no/program/radioresepsjonen.rss"

    fun getEpisodeIndex() : List<EpisodeManifest> {
        val index = ArrayList<EpisodeManifest>()

        val (_, _, result) = url.httpGet().responseString()
        when (result) {
            is Result.Failure -> {
                throw RuntimeException("RSS feed retrieval failed", result.getException())
            }
            is Result.Success -> {
                println("Success! :D")
                val data = result.get()
                parseResponseInto(index, data)
            }
        }

        return index
    }

    private fun parseResponseInto(list: ArrayList<EpisodeManifest>, result: String) {
        val doc = createXmlDocument(result)
        val items = doc.getElementsByTagName("item")

        for (i in 0 until items.length) {
            val item = items.item(i)
            try {
                val url = getChild(item, "enclosure").attributes.getNamedItem("url").textContent
                val pub = getChild(item, "pubDate").textContent
                list.add(EpisodeManifest(url, pub))
            } catch (e: RuntimeException) {
                println("FUCKD: ${e.message}")
            }
        }
    }

    private fun createXmlDocument(xml: String) : Document {
        val builder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        val input = InputSource(StringReader(xml))
        return builder.parse(input)
    }

    private fun getChild(node: Node, name: String) : Node {
        val children = node.childNodes
        for (i in 0..children.length) {
            if (children.item(i).nodeName.equals(name)) {
                return children.item(i)
            }
        }

        throw RuntimeException("Could not find child with name '${name}")
    }
}
