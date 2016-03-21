package cluster

import breeze.linalg.{DenseMatrix, DenseVector}

import scala.io.Source


/**
 * Created by hy on 15-12-2.
 */

object readfile extends {
    /**
     *
     * @param file CSV文件
     * @return 数据坐标点，一列为一个点
     */
    def read(file: String): DenseMatrix[Double] = {
        val s = Source.fromFile(file, "UTF-8").getLines().toArray
        val filterdata = s//.filter(str => (str.split(",")(4) == "1"))
        //过滤数据，选取status为1或5的点
        val dataMat: DenseMatrix[Double] =
            new DenseMatrix[Double](2, filterdata.length)
        for (i <- 0 until filterdata.length) {
            val temp = filterdata(i).split(" ")
            dataMat(::, i) := DenseVector[Double](
            temp(0).toDouble,temp(1).toDouble)
        }
        dataMat
        //        dataMat:*=1.0/1000000


    }

}
