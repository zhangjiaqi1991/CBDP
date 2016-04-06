package sparkcluster

import breeze.linalg.DenseVector
import com.datastax.spark.connector._
import org.apache.spark.{SparkConf, SparkContext}
import sparkcluster.constant._
import sparkcluster.sparkmain._
/**
 * Created by hy on 16-3-7.
 */
object cass2spark {
    def main(args: Array[String]) {
        val conf = new SparkConf(true)
        conf.set("spark.cassandra.connection.host", "192.168.1.141")
        val sc = new SparkContext("spark://192.168.1.121:7077", "cassandra", conf)
        //var i=(-1)
        val rdd = sc.cassandraTable("test", "xiamen")
        val location = rdd.map(s => Array(s.getDouble("unique_id"),
            s.getDouble("acquisition_time"),
            s.getDouble("lng"),
            s.getDouble("lat"),
            s.getDouble("status"),
            s.getDouble("contain")))
            .filter(s => s(STATUS) == 1.0)
            .map(s => Array(s(XROW), s(YROW)))
           .map(s => DenseVector(s))
        //附上行号
        val indexlocation = location.zipWithIndex().map(a => (a._2.toInt, a ._1))
       //val indexlocation = sc.parallelize(location.collect().map { s => i += 1; (i, s)}) //附上行号
       val result = mainfunction(indexlocation, sc, PERCENT).repartition(1).sortByKey()
        val (result1, location1) = subclass(indexlocation, result, 3, sc, PERCENT) //对某个类再次进行分类
    }

}
