package text

import scala.collection.mutable.ArrayBuffer

/**
 * Created by hy on 15-12-16.
 */
object find {
    /**
     * 查找被告/原告信息
     * @param array
     * @param regex
     * @return
     */
    def findall(array: Array[String], regex: String): Tuple2[Array[String],
        Int] = {
        val result = new ArrayBuffer[String]()
        val temp = new ArrayBuffer[String]()
        val relative = array.filter(s => s.matches(regex) == true)

        val re = "\\S{0,}案\\S{0,}。$"
        val rel = array.filter(s => s.matches(re) == true)
        var startindex=0
        if(rel.length>0) {
            startindex = array.indexOf(rel(0)) //以 ”一案“为正文开头标志
        }else{
            startindex=1000//若无一案字样，则所有以原/被告开头的都为原/被告
        }

        if (relative.length > 0) {
            val r = relative(0)
            val index = array.indexOf(r)//第一次出现原/被告人信息的位置
            for (i <- 0 until relative.length) {
                if (array.indexOf(relative(i)) < startindex)
                    temp += relative(i) //在原文中在最后一个委托人之前出现的原/被告人信息
            }
            for (i <- temp) {
                //对于每条被告人信息分析，若有住所地信息则分组后第一个为原/被告人信息，若无则全部为被告人
                if (i.contains("住") == true ||i.contains("男") == true ||i
                    .contains("因") == true||i.contains("地") == true||i
                    .contains("女")==true ) {
                    val ra = i.split("，")
                    result += ra(0)
                } else {
                    result += i
                }
            }
            Tuple2(result.toArray, index)
        }else{
            Tuple2(result.toArray, -1)//-1表示未找到符合条件信息
        }


    }

    /**
     * 查找唯一的数据，比如时间，书记员等
     * @param array
     * @return
     */
    def findonly(array: Array[String], regex: String): Array[String] = {
        val result = new ArrayBuffer[String]()
        val relative = array.filter(s => s.matches(regex) == true)
        for (s <- relative) {
            result += s
        }
        result.toArray
    }
}
