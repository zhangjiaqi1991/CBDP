package sparkstream

import org.apache.spark._
import org.apache.spark.streaming._

/**
  * Created by hy on 16-4-19.
  */
object mlstream {
  def main(args: Array[String]) {
    val conf = new SparkConf().setMaster("local[2]").setAppName("NetworkWordCount")
    val ssc = new StreamingContext(conf, Seconds(1))
    val lines = ssc.socketTextStream("localhost", 9999)
    //val lines=ssc.textFileStream("hdfs://192.168.1.51:9000/data/")
    //lines.print()
    val words = lines.flatMap(_.split(" "))
    val pairs = words.map(word => (word, 1))
    val wordCounts = pairs.reduceByKey(_ + _)
    wordCounts.print()
    ssc.start() // Start the computation
    ssc.awaitTermination() // Wait for the computation to terminate
  }
}
