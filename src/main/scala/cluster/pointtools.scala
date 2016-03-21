package cluster

import breeze.linalg._
import breeze.numerics._
import cluster.constant._

import scala.collection.mutable.ArrayBuffer

/**
 * Created by hy on 15-12-4.
 */

object pointtools {
    /**
     *
     * @param m 坐标矩阵
     * @return 距离矩阵
     */
    def dist(m: DenseMatrix[Double]): DenseMatrix[Double] = {

        val aa = sum(m :* m, Axis._0)
        val bb = sum(m :* m, Axis._0)
        val ab: DenseMatrix[Double] = m.t * m
        ab :*= 2.0
        sqrt(matrixRep(aa.t, bb.cols, "hor") + matrixRep(bb, aa.cols, "ver") - ab)
    }

    /**
     *
     * @param m 需要复制的矩阵
     * @param n 复制次数
     * @param direct 复制方向
     * @return 矩阵复制
     */
    def matrixRep(m: DenseMatrix[Double], n: Int, direct: String):
    DenseMatrix[Double] = {
        def repmat(t: DenseMatrix[Double], k: Int): DenseMatrix[Double] = {
            if (k <= 1) t
            else {
                val y = direct match {
                    case "ver" => DenseMatrix.vertcat(t, m)
                    case "hor" => DenseMatrix.horzcat(t, m)
                    case _ => DenseMatrix.vertcat(t, m)
                }
                repmat(y, k - 1)
            }
        }
        repmat(m, n)
    }

    /**
     *
     * @param m
     * @return 点的局部密度
     */
    def localdensity(m: DenseMatrix[Double]): DenseMatrix[Double] = {
        val s = m.copy
        val dc = threshlod(m)
        s :*= 1.0 / dc
        //s :*= 1.0 / 0.5
        val ss = s :* s
        //        Gaussian kernel
        val t = exp(-ss)
        val res = sum(t, Axis._0)
        res :+= -1.0 // t（i，i）=1

    }

    /**
     *
     * @param m 距离矩阵
     * @return 阈值
     */
    def threshlod(m: DenseMatrix[Double]): Double = {
        val s = m
        val b = m.toArray.filter(s => s != 0.0).sorted
        //val b=m.toArray.distinct.sorted
        val position = PERCENT * m.cols / 100
        val r = position << 1
        println("threshlod is " + b(r))
        b(r)
    }

    /**
     *
     * @param m 局部密度
     * @param d  距离矩阵
     * @return  [密度高的点序号，密度高的点距离，密度高的点距离中最小的]
     *
     */
    def mindist(m: DenseMatrix[Double], d: DenseMatrix[Double]):
    Tuple3[Array[Array[Int]], Array[Array[Double]], DenseMatrix[Double]] = {
        val biggest = max(d)
        val rho = m.toArray

        val bigger = for {//密度高的索引号
            s <- rho
            temp = (0 until m.cols).filter(i => rho(i) > s).toArray
        } yield temp

        val biggerdist = for {//到密度高的点的距离
            s <- 0 until bigger.length
            t = for {
                i <- 0 until bigger(s).length
                temp = d(s, bigger(s)(i))
            } yield temp
            tt = t.toArray
        } yield tt
        val res = new Array[Double](rho.length)
        for (i <- 0 until biggerdist.length)//到密度高的点的最小距离 ，密度最大的点取距离矩阵的最大值
            if (biggerdist(i).length == 0)
                res(i) = biggest
            else
                res(i) = min(biggerdist(i))
        val a = new DenseMatrix[Double](1, res.length, res)
        Tuple3(bigger, biggerdist.toArray, a)
    }

    /**
     *
     * @param m  局部密度和最小距离的乘积
     * @param n 聚类中心个数
     * @return  返回每个类的代表点
     */
    def findcenter(m: DenseMatrix[Double], n: Int): Array[Array[Int]] = {
        val num = n
        val arr = m.toArray
        val sortarr = arr.sorted
        val revarr = sortarr.reverse //转成数组 排序 反转
        //        revarr.foreach(println)
        val result = new ArrayBuffer[Double]()
        for (s <- revarr if result.contains(s) == false)
            result += s
        val value = for (i <- 0 until n) yield result(i)//取数组中前n个大的不重复值
        val ind=for {//根据value值取每个类的代表点，
            v <- value
            temp = (0 until m.cols).filter(s => arr(s) == v)
            t=temp.toArray
        }yield t
        val index=ind.toArray
//        val index = for (v <- value) yield arr.indexOf(v)
//        index.foreach(s=>println(s.toVector))
        println("聚类中心为：")
        for(v<-index){
            v.foreach(println)
            println(" ")
        }
        index
    }

    /**
     *
     * @param center 每个类的代表点
     * @param tuple  密度比自己高的点序号，密度比自己高的点距离，密度比自己高的点中距离最近的点
     * @return
     */
    def classlabel(center: Array[Array[Int]], tuple: Tuple3[Array[Array[Int]],
        Array[Array[Double]], DenseMatrix[Double]]): Array[Int] = {
        val bigger = tuple._1
        val biggerdist = tuple._2
        val result = new Array[Int](bigger.length)

        val nearestindex = new Array[Int](bigger.length)
        for (i <- 0 until biggerdist.length) {//找到每个比自己密度高的点中距离最近的那个点
            if (biggerdist(i).length == 0)//密度最大的点必为类代表点 可取任意值
            nearestindex(i) = (-1)
            else
            nearestindex(i) = bigger(i)(biggerdist(i).indexOf(min(biggerdist(i))))

        }
        for (i <- 0 until bigger.length) {
            result(i) = (-1)//所有点的类标签赋初值
        }
        for (i <- 0 until center.length){//类代表点的类标签赋值
            for (c<-center(i)){
            result(c) = i}
        }
        for (j <- 0 until bigger.length)
            result(j) = neighbor(j)

        def neighbor(i: Int): Int = {
            if (result(i) != (-1)) {
                result(i)
            }
            else {
                val nearest = nearestindex(i)
                neighbor(nearest)
            }
        }
        result
    }

}



