package sparkclassify


import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.{SparkContext, SparkConf}
import org.apache.spark.ml.classification.{OneVsRest, LogisticRegression}
import org.apache.spark.ml.util.MetadataUtils
import org.apache.spark.mllib.evaluation.MulticlassMetrics

/**
  * Created by hy on 16-4-12.
  */
object One_vs_Rest {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("sparkcluster").setMaster("local")
    conf.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
    val sc = new SparkContext(conf)
    val sqlContext = new org.apache.spark.sql.SQLContext(sc)
    import sqlContext.implicits._
    val MASTERIP = "192.168.1.51"
    //坐标
    val data = sc.textFile("hdfs://" + MASTERIP + ":9000" + "/data/shiptype.csv")
      .filter(str => str.contains("shiptype") == false)
      .map { s =>
        val array = s.split(",")
        (array(8).toDouble - 1.0, Vectors.dense(Array(array(2), array(3), array(4), array(5), array(6)).map(_.toDouble)))
      }.toDF("label", "features")

    val splits = data.randomSplit(Array(0.8, 0.2))
    val train = splits(0)
    val test = splits(1)

    val classifier = new LogisticRegression()
      .setMaxIter(100)
      .setTol(1E-6)
      .setFitIntercept(true)

    val ovr = new OneVsRest()
    ovr.setClassifier(classifier)
    // train the multiclass model.
    val ovrModel = ovr.fit(train)

    // score the model on test data.
    val predictions = ovrModel.transform(test)
    // evaluate the model
    val predictionsAndLabels = predictions.select("prediction", "label")
      .map(row => (row.getDouble(0), row.getDouble(1)))

    val metrics = new MulticlassMetrics(predictionsAndLabels)
    val precision = metrics.precision
    val confusionMatrix = metrics.confusionMatrix

    println(s" Confusion Matrix\n ${confusionMatrix.toString}\n")
    println(s"precision:${precision}")

  }
}
