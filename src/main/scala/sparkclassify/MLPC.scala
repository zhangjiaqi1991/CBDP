package sparkclassify


import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.{SparkContext, SparkConf}
import org.apache.spark.ml.classification.MultilayerPerceptronClassifier
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator

/**
  * Created by hy on 16-4-12.
  */
object MLPC {
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

    val splits = data.randomSplit(Array(0.8, 0.2), seed = 1567L)
    val train = splits(0)
    val test = splits(1)
    // specify layers for the neural network:
    // input layer of size 4 (features), two intermediate of size 5 and 4
    // and output of size 3 (classes)
    val layers = Array[Int](5, 5, 10, 3)
    // create the trainer and set its parameters
    val trainer = new MultilayerPerceptronClassifier()
      .setLayers(layers)
      .setBlockSize(128)
      .setSeed(1567L)
      .setMaxIter(1000)
    // train the model
    val model = trainer.fit(train)
    // compute precision on the test set
    val result = model.transform(test)
    val predictionAndLabels = result.select("prediction", "label")
    val evaluator = new MulticlassClassificationEvaluator()
      .setMetricName("precision")
    println("Precision:" + evaluator.evaluate(predictionAndLabels))

  }
}
