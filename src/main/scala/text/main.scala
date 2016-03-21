package text

import java.io.{File, FileWriter}

import text.rate._
import text.readtext._
import text.tools._

/**
 * Created by hy on 15-12-18.
 */
object main {
    def main(args: Array[String]) {

        val f = "/home/hy/data/cmxrmfy/cmxrmfy/ms"
        val fs = listfiles(f)
        val f1 = new File("/home/hy/三级法院cmxrmfy.txt")
        val fwriter = new FileWriter(f1)
        fwriter.write("文件名;原告;被告;委托代理人（原告方）;委托代理人（被告方）;主审法官;审判员;代理审判员;书记员;" +
            "律师事务所;" +
            "判决日期;案由;案号;审理法院;审级;原告胜率/原告委托代理人胜率;被告胜率/被告委托代理人胜率;" +
            "原告/原告委托代理人主要诉请获支持率;被告/被告委托代理人主要抗辩论成功率;诉讼标的／案值" + "\n") //表头
        val regex="t\\S+\\.".r
        for (n <- fs) {
            try {
                val context = read(n)
               val filename=regex.findFirstIn(n.getName).mkString.filter(s=>s!='.')
                val str = mainfunction(context,filename)
                fwriter.write(str + "\n")
            } catch {
                case e: Exception => println(n)
            }
        }

        fwriter.flush()
        fwriter.close()

    }

    def mainfunction(array: Array[String],filename:String): String =
    {//一个网页生成一条记录
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


        val sym1 = ","//栏内分隔符
        val sym2 = ";"//栏间分隔符
        val result = Array(
            filename,//文件名
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
        result.filter(s => s != '。')
    }

    def listfiles(f: String): Array[File] = {//获取文件中的所有网页
        val file = new File(f)
        val fs = file.listFiles().toIterator.toArray
        fs
    }


}
