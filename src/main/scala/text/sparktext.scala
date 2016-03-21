package text

import org.apache.spark.{SparkConf, SparkContext}
import org.jsoup._


/**
 * Created by hy on 16-3-7.
 */
object sparktext extends {
     def main (args: Array[String]){
         val conf = new SparkConf().setAppName("sparkcluster").setMaster ("local")
         conf.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
         val sc = new SparkContext(conf)
         val f = sc.wholeTextFiles("hdfs://192.168.1.51:9000/webpage")
         val t=f.collect()
         mainfunction(t(0))
    }
    def mainfunction(t:Tuple2[String,String]):Unit={
        val regex="t\\S+\\.".r
        val filename=regex.findFirstIn(t._1).mkString.filter(s=>s!='.')
        val doc = Jsoup.parse(t._2, "UTF-8")
        val ele = doc.getElementById("wenshu")
        val array = ele.text().split(" ")//解析后数组
        println(filename)
        array.foreach(println)
    }

}
