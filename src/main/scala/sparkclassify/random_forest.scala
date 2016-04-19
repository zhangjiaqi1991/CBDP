package sparkclassify

import org.apache.spark.mllib.classification.LogisticRegressionWithLBFGS
import org.apache.spark.mllib.evaluation.MulticlassMetrics
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.tree.RandomForest
import org.apache.spark.{SparkContext, SparkConf}

/**
  * Created by hy on 16-4-12.
  */
//90+%
object random_forest {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("sparkcluster").setMaster("local")
    conf.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
    val sc = new SparkContext(conf)
    val MASTERIP = "192.168.1.51"
    //坐标
    val ship = sc.textFile("hdfs://" + MASTERIP + ":9000" + "/data/shiptype.csv")
      .filter(str => str.contains("shiptype") == false)
      .map { s =>
        val array = s.split(",")
        LabeledPoint(array(8).toInt - 1, Vectors.dense(Array(array(2), array(3), array(4), array(5), array(6)).map(_.toDouble)))
      }.cache()

    val splits = ship.randomSplit(Array(0.8, 0.2))
    val training = splits(0).cache()
    val test = splits(1).cache()
    val numTraining = training.count()
    val numTest = test.count()
    println(s"Training: $numTraining, test: $numTest.")

    // Train a RandomForest model.
    // Empty categoricalFeaturesInfo indicates all features are continuous.
    val numClasses = 3
    val categoricalFeaturesInfo = Map[Int, Int]()
    val numTrees = 100 // Use more in practice.
    val featureSubsetStrategy = "auto" // Let the algorithm choose.
    val impurity = "gini"
    val maxDepth = 6 //越高效果也好，但会过拟合
    val maxBins = 32

    val model = RandomForest.trainClassifier(training, numClasses, categoricalFeaturesInfo,
      numTrees, featureSubsetStrategy, impurity, maxDepth, maxBins)


    val valuesAndPreds = test.map { point =>
      val prediction = model.predict(point.features)
      (point.label, prediction)
    }
    valuesAndPreds.foreach(println)
    val metrics = new MulticlassMetrics(valuesAndPreds)
    val precision = metrics.precision
    println("Precision = " + precision)
  }
}
