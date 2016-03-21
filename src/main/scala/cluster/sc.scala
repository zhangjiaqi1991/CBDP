package cluster

import breeze.linalg._
import breeze.numerics.sqrt

import scala.io.Source

/**
 * Created by hy on 16-1-19.
 */
object sc extends App {
    val f = "/home/hy/Spiral.txt"
    val l = Source.fromFile(f).getLines().toArray.map(s => DenseVector(s.split
    ("\t").map(c => c.toDouble)))
    val distmat: DenseMatrix[Double] =
        new DenseMatrix[Double](l.length, l.length)
    for (i <- 0 until l.length) {
        for (j <- 0 until l.length) {
            distmat(i, j) = norm(l(i) - l(j))
        }
    }
    val ones = DenseVector.ones[Double](distmat.cols)
    val distsum = sum(distmat, Axis._0).toDenseVector
    //每一行（列）的和
    val d = ones :/ sqrt(distsum)
    val sumdiag = diag(d) //对角矩阵
    val s: DenseMatrix[Double] = sumdiag * distmat
    val c = s * sumdiag
    val ev=eig(c).eigenvectors
    val ea=eig(c).eigenvalues
    println(ev(::,2))
    println(ea)









}
