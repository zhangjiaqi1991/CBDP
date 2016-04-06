package sparkcluster

import breeze.linalg.{DenseVector, max}
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}
import org.slf4j.{Logger, LoggerFactory}
import sparkcluster.constant._
import sparkcluster.sparktools._
/**
 * Created by hy on 15-12-14.
 */
object sparkmain {
    val logger: Logger = LoggerFactory.getLogger(sparkmain.getClass)//获取日志对象
    def main(args: Array[String]) {
        val conf = new SparkConf().setAppName("sparkcluster")
        conf.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
        val sc = new SparkContext (conf)
        var i=(-1)
        //坐标
        logger.info("************Read data into RDD************")
        println("************Read data into RDD************")
        val location = sc.textFile("hdfs://" + MASTERIP + ":9000" + "/data/" + args(2))
          .filter(str => str.contains("longitude") == false)
            .map(s => s.split(","))
          .map(s => Array(s(args(0).toInt), s(args(1).toInt)))
            .map(s => s.map(b => b.toDouble))
            .map(s => DenseVector(s))
        val indexlocation = location.zipWithIndex().map(a=>(a._2.toInt,a._1))
        //val indexlocation = sc.parallelize(location.map { s => i += 1; (i, s)}) //附上行号
        val result = mainfunction(indexlocation, sc, args(3).toInt) //.repartition(1).sortByKey()
        //val (result1,location1)=subclass(indexlocation,result,3,sc,args(3).toInt)//对某个类再次进行分类
//        val (result2,location2)=subclass(location1,result1,1,sc)
//        val (result3,location3)=subclass(location2,result2,1,sc)



//        //输出到hdfs
//        val output = new Path(master+resultfile);
//        val hdfs = FileSystem.get(
//            new URI(master), new Configuration())
//        // 删除输出目录
//        if (hdfs.exists(output)) hdfs.delete(output, true)
//        result.map(s=>s._2).saveAsTextFile(master+resultfile+ "/lable")
//        indexlocation.repartition(1).sortByKey().map(s=>(s._2(0),s._2(1)))
//            .saveAsTextFile(master+resultfile+ "/location")
    }

    /**
     *
     * @param l        行号，坐标
     * @param sc
      * @param percent 百分比入参
     * @return
     */
    def mainfunction(l: RDD[Tuple2[Int, DenseVector[Double]]], sc: SparkContext, percent: Int):
    RDD[Tuple2[Int, Int]] = {
        logger.info("************Begin clustering************")
        println("************Begin clustering************")
        val master="hdfs://"+MASTERIP+":9000"
        val resultfile=RESULT+PREFIX
        val location = l.map((s) => s._2).collect() //坐标
        val blocation = sc.broadcast(location)
        //距离矩阵(行号，距离向量）
        logger.info("************Compute distance matrix************")
        println("************Compute distance matrix************")
        val distmat = l.map(s => distmatrix(s, blocation.value))
        logger.info("************Compute successfully ************")
        //最大距离
        val biggestdist = distmat.map((s) =>max(s._2)).max()
        val bc_biggestdist = sc.broadcast(biggestdist)
        //阈值
        val thr = threshlod(distmat, percent)
        val bc_thr = sc.broadcast(thr)
        //局部密度（行号，密度）
        logger.info("************Compute local density************")
        println("************Compute local density************")
        val pointdensity = distmat
            .map(s => localdensity(s, bc_thr.value))
        val biggestdensity = pointdensity.reduce((a, b) => if (a._2 > b._2) a else b)._2 //密度最大值
        val bc_biggestdensity=sc.broadcast(biggestdensity)
        val normpointdensity = pointdensity.map(s => (s._1, s._2 /
            bc_biggestdensity.value)) //归一化
        val bc_normpointdensity = sc.broadcast(normpointdensity.collect())
        logger.info("************Compute successfully ************")
        //最近距离和最近点行号   （行号，(密度高的最近点行号，离密度高的点的最近距离)）
        logger.info("************Compute min distance************")
        println("************Compute min distance************")
        val mdist = normpointdensity.join(distmat).map(s => mindist(s, bc_normpointdensity.value, bc_biggestdist.value))
        logger.info("************Compute successfully ************")
        //最近距离×局部密度        （行号，密度高的最近点行号，乘积）
        logger.info("************Compute local density × min distance************")
        println("***********Compute local density × min distance************")
        val md = mdist.join(normpointdensity)//与点密度rdd相连
            .map(s=>(s._1,s._2._1._1,s._2._1._2/biggestdist*s._2._2))//距离归一化再相乘
            .sortBy(s=>(-(s._3)))
        val descion = md.map(s => s._3).distinct().sortBy(s => (-s)) //决策属性,估计类数
        logger.info("************Compute successfully ************")
        logger.info("************Compute class number ************")
        println("*********************Compute class number **********************")
        val CN=classnum(descion,sc)//类数
        logger.info("************Compute successfully ************")
        logger.info("************Compute class center point ************")
        println("******************Compute class center point*********************")
        //根据值，给每个中心点类标签
        val centervalue = md
                .map(s => s._3)
                .distinct()
                .sortBy(s => (-s))
                .take(CN) //取类中心点的属性乘积
        val bc_centervalue = sc.broadcast(centervalue)
        val centerlable = md
            .map(s => clable(s, bc_centervalue.value))
            .sortBy(s => s._1)//根据取的乘机值给类中心点标签
        logger.info("************Compute successfully ************")
        val bc_centerlable = sc.broadcast(centerlable.collect())
        //给每个点类标签
        logger.info("************Lable the other point ************")
        println("****************Lable the other point ***********************")
        val pointlable = centerlable.map(s => plable(s, bc_centerlable.value))
        val lable = pointlable.map(s => (s._1, s._3))
        logger.info("************Lable successfully ************")
        logger.info("************Compute class radius ************")
        println("***************Compute class radius ************************")
        //计算类中心点坐标，类中点到中心点的距离平均数作为半径
        //提取类中心点的类标签和坐标
        val center=centerlable
                .filter(s=>s._3>0)
                .map(s=>(s._1,s._3))
                .join(l)
                .map(s=>(s._2._1,s._2._2))
                .groupBy(s=>s._1)
                .map(s=>(s._1,s._2.head._2))
        //（类标签，类中元素与中心点的距离）
        val labledist=lable.join(l).map(s=>s._2)
            .leftOuterJoin(center).map(s=>(s._1,dist(s._2._1,s._2._2.get)))
        //类标签，类中元素与中心点的距离和
        val sumdist=labledist.reduceByKey((a,b)=>a+b)
        //类标签，每个类的元素个数
        val elenum=labledist.map(s=>(s._1,1)).reduceByKey((a,b)=>a+b)
        //半径，类中心点坐标
        val radius=sumdist.join(elenum).map(s=>(s._1,(s._2._1)/(s._2._2)))
            .join(center).map(s=>(s._1,s._2._1,s._2._2(0),s._2._2(1)))
        logger.info("************Compute successfully ************")
        //结果输出到hdfs
        logger.info("************Save to RDD ************")
        println("*****************Save to RDD **************************")
        saveresult(master,resultfile,lable,radius,l)
        logger.info("************Save successfully ************")
        logger.info("************End clustering************")
        println("*****************End clustering*************************")
        lable
    }

    /**
     *
     * @param location 坐标
     * @param r     上一次的聚类结果
     * @param C   需要再次聚类的类号
     * @param sc
     * @return 新的聚类结果
     */
    def subclass(
                  location:RDD[(Int,DenseVector[Double])],
                  r: RDD[(Int, Int)],
                  C:Int,
                  sc: SparkContext,
                  percent: Int): Tuple2[RDD[(Int, Int)], RDD[(Int, DenseVector[Double])]] = {
        var j,k = (-1)
//        val firstclassnum=r.map(s=>s._2).max()//粗聚类的类数
        //根据类标签，提取出某一类的坐标
        val clocation = location //行号，（坐标，类标签）
                .join(r).sortByKey().filter(s => s._2._2 == C)
//        //保留原有行号
//        val index = sc.parallelize(clocation
//            .map(s => s._1)
//            .collect().map { s => j+= 1; (j, s) })
        //生成行号，坐标的形式
     val indexlocation = clocation.map(s => s._2._1).zipWithIndex().map(a=>(a
    ._2.toInt,a._1))
//        val indexlocation = sc.parallelize(clocation
//           .map(s => s._2._1)
//            .collect()
//            .map { s => k += 1; (k, s) })
        logger.info("************Cluster the point in  class "+C+"************")
        println("**************Cluster the point in  class " + C + "*********")
        val result = mainfunction(indexlocation, sc, percent)
            //.join(index).map(s => (s._2._2, s._2._1))//将第二次的聚类结果与保留的原有行号进行匹配
//        //将两次的聚类结果进行合并
//        val result3 = r.leftOuterJoin(result2)
//            .map { s =>
//                if (s._2._2 != None) {
//                    (s._1, s._2._2.get + firstclassnum)
//                } else {
//                    (s._1, s._2._1)
//                }
//            }.sortByKey()
        (result,indexlocation)
    }

}
