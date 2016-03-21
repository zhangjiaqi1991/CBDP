package test

import java.io.File

/**
  * Created by hy on 15-12-14.
  */
object test_text extends App {
  val regex = "t\\S+\\.".r
  val f1 = new File("/home/hy/t20013_231231.htm")
  println(f1.getName)
  val filename = regex.findFirstIn(f1.getName).mkString.filter(s => s != '.')
  print(filename)



  //  val array = readtext.read("/home/hy/data2/t20140504_967842.htm")
  //
  //  val yg=accuser(array)//原告
  //  val bg=accused(array)//被告
  //  val wt=rep(array,bg._2)//原被告委托人，事务所
  //  val fg=chiefjudge(array)//主审法官。审判长
  //  val sp=judge(array)//审判员
  //  val sjy=record(array)//书记员
  //  val dsp=proxyjudge(array)//代理审判员
  //  val ah=casenum(array)//案号
  //  val ay=casecause(array)//案由
  //  val t=time(array)//日期
  //  val sj=judgetime(ah)//审级
  //  val ft=court(array)//法院
  //  val wr=winrate(array,ay,ah)//原被告胜率
  //  val sr=supportrate(array,ay,ah)//原被告诉请支持率
  //
  //  val sym1=","
  //  val sym2=";"
  //  val result=Array(
  //    yg._1.mkString(sym1) ,
  //    bg._1.mkString(sym1) ,
  //    wt._1.mkString(sym1) ,
  //    wt._2.mkString(sym1) ,
  //    fg.mkString ,
  //    sp.mkString ,
  //    dsp.mkString ,
  //    sjy.mkString ,
  //    wt._3.mkString(sym1) ,
  //    t.mkString,
  //    ay.mkString,
  //    ah.mkString,
  //    ft.mkString,
  //    sj.mkString,
  //    wr.mkString(sym2),
  //    sr._1.mkString(sym2),
  //    sr._2.toString).mkString(sym2)
  //  println(result)
  //


}
