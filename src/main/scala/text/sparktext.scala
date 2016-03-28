package text

import org.apache.spark.{SparkConf, SparkContext}
import org.jsoup._
import text.rate._
import text.tools._


/**
  * Created by hy on 16-3-7.
  */
object sparktext extends {
  def main(args: Array[String]) {
    val conf = new SparkConf().setAppName("sparktext")
    conf.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
    val sc = new SparkContext(conf)
    val f = sc.wholeTextFiles("hdfs://192.168.1.121:9000/webpage/ms")
    val arr = f.map(s => text2arr(s))
    val result = arr.map(s => arr2lable(s))
    sc.parallelize(Array[String]("文件名;原告;被告;委托代理人（原告方）;委托代理人（被告方）;主审法官;审判员;代理审判员;书记员;" +
      "律师事务所;" + "判决日期;案由;案号;审理法院;审级;原告胜率/原告委托代理人胜率;被告胜率/被告委托代理人胜率;" +
      "原告/原告委托代理人主要诉请获支持率;被告/被告委托代理人主要抗辩论成功率;诉讼标的／案值")).union(result).repartition(1)
      .saveAsTextFile("hdfs://192.168.1.121:9000/data/webresult")

  }

  /**
    *
    * @param t (文件路径名，网页内容）
    * @return （文件名，文书内容数组）
    */
  def text2arr(t: Tuple2[String, String]): Tuple2[String, Array[String]] = {
    val regex = "t\\S+\\.".r
    val filename = regex.findFirstIn(t._1).mkString.filter(s => s != '.')
    val doc = Jsoup.parse(t._2, "UTF-8")
    val ele = doc.getElementById("wenshu")
    val arr = ele.text().split(" ") //解析后数组
    (filename, arr)
  }

  /**
    *
    * @param arr (文件名，文书内容数组）
    * @return 一条标签
    */
  def arr2lable(arr: Tuple2[String, Array[String]]): String = {
    //一个网页生成一条记录
    var result: String = null
    try {
      val array = arr._2
      val yg = accuser(array) //原告
      val bg = accused(array) //被告
      val wt = rep(array, bg._2) //原被告委托人，事务所
      val fg = chiefjudge(array) //主审法官。审判长
      val sp = judge(array) //审判员
      val sjy = record(array) //书记员
      val dsp = proxyjudge(array) //代理审判员
      val ah = casenum(array) //案号
      val ay = casecause(array) //案由
      val t = time(array) //日期
      val sj = judgetime(ah) //审级
      val ft = court(array) //法院
      val wr = winrate(array, ay, ah) //原被告胜率
      val sr = supportrate(array, ay, ah) //原被告诉请支持率
      val sq = sr._2 //原告诉请金额
      val sym1 = "," //栏内分隔符
      val sym2 = ";" //栏间分隔符
      result = Array(
        arr._1, //文件名
        yg._1.mkString(sym1),
        bg._1.mkString(sym1),
        wt._1.mkString(sym1),
        wt._2.mkString(sym1),
        fg.mkString,
        sp.mkString,
        dsp.mkString,
        sjy.mkString,
        wt._3.mkString(sym1),
        t.mkString,
        ay.mkString,
        ah.mkString,
        ft.mkString,
        sj.mkString,
        wr.mkString(sym2),
        sr._1.mkString(sym2),
        sq).mkString(sym2)
    } catch {
      case e =>
        println(arr._1 + " " + e.printStackTrace())
    }
    result.filter(s => s != '。')
  }

}
