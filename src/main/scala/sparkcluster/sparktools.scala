package sparkcluster

import java.net.URI

import breeze.linalg.{DenseVector, norm, sum}
import breeze.numerics.exp
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{Path, FileSystem}
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import sparkcluster.constant._

import scala.util.control.Breaks._

/**
 * Created by hy on 15-12-28.
 */
object sparktools {
    /**
     *
     * @param location 行号，单点坐标
     * @param all 所有点坐标
     * @return 行号，单点到所有点的距离向量
     */
    def distmatrix(location: Tuple2[Int, DenseVector[Double]], all:
    Array[DenseVector[Double]])
    : Tuple2[Int, DenseVector[Double]] = {
        val temp = for {
            s <- all
            t = dist(location._2, s)
        } yield t
        (location._1, DenseVector(temp))
    }

    /**
     * 两个点之间的距离
      *
      * @param v1
     * @param v2
     * @return
     */
    def dist(v1: DenseVector[Double], v2: DenseVector[Double]): Double = {
        norm(v1 - v2)
    }

    /**
     * 局部密度
      *
      * @param distvector 行号，距离向量
     * @return 行号，点的局部密度
     */
    def localdensity(distvector: Tuple2[Int, DenseVector[Double]], threshlod:
    Double):
    Tuple2[Int, Double] = {
        //val ths=0.5 //阈值
        distvector._2 :*= 1.0 / threshlod
        val ss = distvector._2 :* distvector._2
        val ess = exp(-ss)
        Tuple2(distvector._1, sum(ess) - 1.0)
    }

  /**
    *
    * @param distmat 距离矩阵
    * @param percent 阈值百分比
    * @return 阈值
    */

  def threshlod(distmat: RDD[(Int, DenseVector[Double])], percent: Int): Double = {
    val position = percent * distmat.count() / 100
        val p = (position << 1).toInt
        val t = distmat.map((v) => v._2).flatMap((v) => v.toArray).filter(s => s != 0.0)
            .takeOrdered(p).last //过滤0或者distinct
        t
    }

    /**
     * 最近点和最近距离的计算
      *
      * @param density_distvector （行号，（密度，距离向量））
     * @param alldensity 所有点的密度  ,    （行号，密度）
     * @param biggestdist  距离中的最大值
     * @return  （点的行号，(密度高的最近点行号，离密度高的点的最近距离）)
     */
    def mindist(density_distvector: (Int, (Double, DenseVector[Double])),
                alldensity: Array[(Int, Double)], biggestdist: Double)
    : Tuple2[Int, (Int, Double)] = {
        var result: Tuple2[Int, (Int, Double)] = null
        val r = density_distvector._2._1 //点的密度
        val bigger = alldensity.filter((s) => s._2 > r) //密度高的点
        if (bigger.length == 0) {
            result = (density_distvector._1, (-1, biggestdist))
            //密度最大的点，距离为所有距离的最大值,密度高的最近点无需考虑
        } else {
            //根据行号取距离
            val biggerdist = bigger.map(s => (density_distvector._1, (s._1, density_distvector._2._2(s._1))))
            result = biggerdist.reduce((a, b) => if (a._2._2 < b._2._2) a else b)
        }
        result
    }

    /**
     * 给类中心点赋类标签
      *
      * @param md（行号，最近点行号，乘机）
     * @param center 乘机最大的几个点的乘机值
     * @return （行号，最近点行号，类标签）
     */
    def clable(md: Tuple3[Int, Int, Double], center: Array[Double]): Tuple3[Int, Int, Int] = {
        var t = 0
        if (center.contains(md._3)) {
            t = center.indexOf(md._3) + 1
        } else {
            t = (-1)
        }
        (md._1, md._2, t)
    }

    /**
     * 给每个点赋类标签
      *
      * @param p （行号，最近点的行号，类标签）   -1为未赋值的点
     * @param c   跟P相同，用来迭代的计算类标签
     * @return （行号，最近点的行号，类标签）
     */
    def plable(p: Tuple3[Int, Int, Int], c: Array[Tuple3[Int, Int, Int]])
    : Tuple3[Int, Int, Int] = {
        var t = 0
        if (p._3 == (-1)) {
            t = neighbor(p._2)
        } else {
            t = p._3
        }

        def neighbor(i: Int): Int = {
            if (c(i)._3 != (-1)) {
                c(i)._3
            }
            else {
                val nearest = c(i)._2
                neighbor(nearest)
            }
        }
        Tuple3(p._1, p._2, t)
    }

    /**
     * 估计类的个数
      *
      * @param d  决策属性RDD
     * @param sc
     * @return
     */
    def classnum(d: RDD[Double], sc: SparkContext): Int = {
        val bc_d = sc.broadcast(d.collect())
        var i = (-1)
        val cd = sc.parallelize(d.collect().map { s => i += 1; (i, s) })
        val s1 = cd.map(s => sub(s, bc_d.value)) //相邻相减
        val bc_s1 = sc.broadcast(s1.collect())
        val s2 = s1.map(s => div(s, bc_s1.value)) //相邻相除
        val sa = s2.collect()
        var n = 0
        breakable {
            for (i <- 1 until sa.length - 1) {
                if (sa(i)._2 - sa(i - 1)._2 > 0 && sa(i)._2 - sa(i + 1)._2 > 0) {
                    n = i
                    break()
                }
            }
        }
        n + 2
    }

    def sub(t: Tuple2[Int, Double], d: Array[Double]): Tuple2[Int, Double] = {
        if (t._1 == d.length - 1) {//判断是否是最后一个元素
            (t._1, 0.0)
        } else {
            (t._1, t._2 - d(t._1 + 1))
        }
    }

    def div(t: Tuple2[Int, Double], d: Array[(Int, Double)]): Tuple2[Int, Double]
    = {
        if (t._1 == d.length - 1) {//判断是否是最后一个元素
            (t._1, 0.0)
        } else {
            if (d(t._1 + 1)._2 != 0.0) {
                (t._1, t._2 / d(t._1 + 1)._2)
            } else {
                (t._1, 0.0)
            }
        }
    }

    /**
     *
     * @param master master ip
     * @param resultfile   存储文件的位置
     * @param lable  标签
     * @param radius  聚类中心 半径
     * @param l 坐标点
     */
    def saveresult(master: String,
                   resultfile: String,
                   lable: RDD[(Int, Int)],
                   radius: RDD[(Int, Double, Double, Double)],
                   l: RDD[(Int, DenseVector[Double])]): Unit = {
        var k = 1
        val hdfs = FileSystem.get(
            new URI(master), new Configuration())
        save(k)//存储结果
        def save(k: Int): Unit = {
            var j = k
            val output = new Path(master + resultfile + j.toString)
            if (!hdfs.exists(output)) {//查询文件名是否被占用，若没被占用
                lable.repartition(1).sortByKey().map(s => s._2)
                    .saveAsTextFile(master + resultfile + j.toString + "/lable")
                radius.repartition(1).saveAsTextFile(master + resultfile + j
                    .toString + "/radius")
              l.repartition(1).sortByKey().map((s) => (s._2(0), s._2(1))).map(s => s._1.toString() +","+s._2.toString)
                    .saveAsTextFile(master + resultfile + j
                    .toString + "/location")
            } else {//如果被占用了那么就将用户名的后缀+1，递归执行
                j += 1
                save(j)
            }
        }
    }


}
