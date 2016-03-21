package test

import org.apache.spark.{SparkConf, SparkContext}
import sparkcluster.constant._

/**
 * Created by hy on 15-12-30.
 */
object sparktest  {
     def main (args: Array[String]){
    val conf = new SparkConf(true).setAppName("test")
    conf.set("spark.cassandra.connection.host", "127.0.0.1").setMaster ("local")
    val sc = new SparkContext(conf)
    val master="hdfs://192.168.1.51:9000"
    val regex="\\d+"
    val b_regex=sc.broadcast(regex)
    val location = sc.textFile(master+"/data/xiamen.csv")
        .map(s => s.split(","))
        .filter(str => str(STATUS) .matches(b_regex.value)==true)
        .map(s=>s.map(st=>st.toDouble))
        .map(s=>(s(0),s(1),s(2),s(3),s(4),s(5)))
    location.saveAsTextFile("hdfs://192.168.1.51:9000/data/test00")
}  }
