package sparkstream

import org.apache.spark._
import org.apache.spark.ml.{PipelineModel, Pipeline}
import org.apache.spark.ml.classification.DecisionTreeClassifier
import org.apache.spark.ml.feature.{IndexToString, VectorIndexer, StringIndexer}
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.sql.SQLContext
import org.apache.spark.streaming._

/**
  * Created by hy on 16-4-19.
  */
object mlstream {
  def main(args: Array[String]) {
    val conf = new SparkConf().setMaster("local[2]").setAppName("classify")
    val sc = new SparkContext(conf)
    val model = sc.objectFile[PipelineModel]("hdfs://192.168.1.51:9000/data/model.mdl").take(1)(0)
    val stc = new StreamingContext(sc, Seconds(1))
    val sqc = new SQLContext(sc)
    import sqc.implicits._

    val lines = stc.socketTextStream("192.168.1.51", 9998)
    //val lines=ssc.textFileStream("hdfs://192.168.1.51:9000/data/")

    val words = lines.foreachRDD { rdd =>
      val temp = rdd.map { s =>
        val t = s.split("\\s+").map(_.toDouble)
        Tuple1(Vectors.dense(t))
      }.toDF("features")
      val result = model.transform(temp)
      val predition = result.select("prediction")
      predition.show()
    }

    stc.start() // Start the computation
    stc.awaitTermination() // Wait for the computation to terminate
  }
}
