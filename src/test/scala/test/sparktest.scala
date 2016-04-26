package test

import org.apache.spark.ml.PipelineModel
import org.apache.spark.{SparkConf, SparkContext}
import sparkcluster.constant._

/**
  * Created by hy on 15-12-30.
  */
object sparktest {
  def main(args: Array[String]) {
    val conf = new SparkConf(true).setAppName("test")
    conf.set("spark.cassandra.connection.host", "127.0.0.1").setMaster("local")
    val sc = new SparkContext(conf)
    val model = sc.objectFile[PipelineModel]("hdfs://192.168.1.51:9000/data/model.mdl")

  }
}
