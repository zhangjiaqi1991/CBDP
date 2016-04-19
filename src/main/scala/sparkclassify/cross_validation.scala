package sparkclassify


import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.{SparkContext, SparkConf}
import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.tuning.{ParamGridBuilder, CrossValidator}
import org.apache.spark.ml.classification.RandomForestClassifier
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator

/**
  * Created by hy on 16-4-13.
  */
object cross_validation {
  //runtime error
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

    val rf = new RandomForestClassifier()
      .setLabelCol("label")
      .setFeaturesCol("features")
      .setNumTrees(100)

    val pipeline = new Pipeline().setStages(Array(rf))

    val paramGrid = new ParamGridBuilder().build() // No parameter search

    val evaluator = new MulticlassClassificationEvaluator()
      .setLabelCol("label")
      .setPredictionCol("prediction")
      // "f1", "precision", "recall", "weightedPrecision", "weightedRecall"
      .setMetricName("precision")

    val cv = new CrossValidator()
      // ml.Pipeline with ml.classification.RandomForestClassifier
      .setEstimator(pipeline)
      // ml.evaluation.MulticlassClassificationEvaluator
      .setEvaluator(evaluator)
      .setEstimatorParamMaps(paramGrid)
      .setNumFolds(5)

    val model = cv.fit(train) // trainingData: DataFrame
    val predictions = model.transform(test)
    // evaluate the model
    val predictionsAndLabels = predictions.select("prediction", "label")
      .map(row => (row.getDouble(0), row.getDouble(1)))
    predictionsAndLabels.foreach(println)
  }

}
