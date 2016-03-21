package text

import java.io.File
import org.jsoup.Jsoup

/**
 * Created by hy on 15-12-14.
 */
object readtext {
    def read(file: File) :Array[String]={
        val doc = Jsoup.parse(file, "UTF-8")
        val ele = doc.getElementById("wenshu")
        val array = ele.text().split(" ")
        array
    }

}
