package sparkcluster

import com.datastax.spark.connector._
import org.apache.spark.{SparkConf, SparkContext}
import sparkcluster.constant._

/**
 * Created by hy on 16-3-9.
 */
object hdfs2cass {
    def main(args: Array[String]) {
    val conf = new SparkConf(true)
    conf.set("spark.cassandra.connection.host", "192.168.1.141")
    val sc = new SparkContext("spark://192.168.1.121:7077", "cassandra", conf)
    val master="hdfs://"+MASTERIP+":9000"
    val regex="\\d+"
    val b_regex=sc.broadcast(regex)
    val location = sc.textFile(master+"/data/xiamen.csv")
        .map(s => s.split(","))
        .filter(str => str(STATUS) .matches(b_regex.value)==true)
        .map(s=>s.map(st=>st.toDouble))
        .map(s=>(s(0),s(1),s(2),s(3),s(4),s(5)))
    location.saveToCassandra("test", "xiamen", SomeColumns("unique_id",
        "acquisition_time","lng","lat","status","contain"))

    }
}
