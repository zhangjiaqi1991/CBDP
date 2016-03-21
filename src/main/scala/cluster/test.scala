package cluster

import java.io.{File, FileWriter}

import breeze.linalg._
import cluster.pointtools._

/**
 * Created by hy on 15-12-3.
 */
object test extends App {
  val content: DenseMatrix[Double] = readfile.read("/home/hy/jain.txt")
  val b: DenseMatrix[Double] = dist(content)
  val c: DenseMatrix[Double] = localdensity(b)

  val tup = mindist(c, b)
  val d = tup._3
  c :*= 1.0 / max(c)
  d :*= 1.0 / max(d)
  val x = c :* d
  val ind = findcenter(x, 2)
  val classlab = classlabel(ind, tup)



//  val c0 = (0 until d.cols).filter(i => classlab(i) == 2)
//  val data: DenseMatrix[Double] =
//    new DenseMatrix[Double](2, c0.length)
//  for (i <- 0 until c0.length) {
//    data(::, i) := content(::, c0(i))
//  }
//
//
//  val b1: DenseMatrix[Double] = dist(data)
//  CENTERNUM=3
//  val c1: DenseMatrix[Double] = localdensity(b1)
//  val tup1 = mindist(c1, b1)
//  val d1 = tup1._3
//  c1 :*= 1.0 / max(c1)
//  d1 :*= 1.0 / max(d1)
//  val x1 = c1 :* d1
//  val ind1 = findcenter(x1, CENTERNUM)
//  val classlab1 = classlabel(ind1, tup1)

//val ss= (0 until x.cols).toArray.map(s=>s.toDouble)
//val x2=x.toArray.sortWith(_>_)
//  val f = Figure()
//  val p = f.subplot(0)
//  p += plot(ss,x2,'-',"red")
//  p.xlabel = "x axis"
//  p.ylabel = "y axis"
//  f.saveas("descion1.png") //

//
  val f1=new File("/home/hy/class.txt")
  val fwriter1 = new FileWriter(f1)
  for(i<- 0 until classlab.length){
    fwriter1.write(classlab(i)+"\n")
  }
  fwriter1.flush()
  fwriter1.close()



}

