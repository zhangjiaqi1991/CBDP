package text

import scala.collection.mutable.ArrayBuffer

/**
 * Created by hy on 15-12-18.
 */
object rate {
    /**
     *
     * @param array
     * @param ay 案由
     * @param ah 案号
     * @return  原告胜率，被告胜率
     */
    def winrate(array: Array[String], ay: Array[String], ah: Array[String]): Array[String] = {
        val result = new ArrayBuffer[String]()

        val keyword1 = "\\S{0,}判决如下：$" //案件受理费这句话在判决如下以后出现
        val k1 = array.indexOf(array.filter(s => s.matches(keyword1)).mkString)
        val relative = array.filter(s => s.matches("\\S{0,}(本案)?案件受理费\\S{0,}") ==
            true).filter(s => array.indexOf(s) > k1).mkString
        //本案件案件受理费。。。。。。。。。。

        val regex = "\\d*，?\\d+\\.?\\d+元".r //匹配金额
        if (!regex.findFirstIn(relative).isEmpty && !regex.findFirstIn(relative.split("受理费").last).isEmpty && !regex.findFirstIn(relative.split("减半").last).isEmpty) {
            //若这句话中没有数字，受理费后面没数字，减半后面没数字则直接取空
            val jbslf = slfcount(array) //减半受理费，实际收取的受理费，算百分比时用到

            val slf = regex.findFirstIn(relative.split("受理费").last).mkString.filter(s => s != '，' && s != '元').toDouble //案件受理费，不考虑减半，判断纠纷是否需要计算用到
            //判断是否是民事判决书，案号中带民初字
            if (condition(array, ah)) {
                //若是，判断是否是需要计算的纠纷类型
                if (jf(ay, slf)) {

                    if (relative.contains("被告") && relative.contains("原告")) {
                        //取出t中原告承担费用
                        val f = relative.split("原告").last.mkString
                        val yslf = regex.findAllIn(f)

                        if (!yslf.isEmpty) {
                            val ma = yslf.toArray.map(s => s.filter(p => p != '，' && p != '元')
                                .toDouble).filter(s => s < jbslf) //找出这句话中所有比jbslf小的金额
                            if (!ma.isEmpty) {

                                val temp = ((jbslf - ma.head) * 100 / jbslf) //取数组中的第一个数作为原告金额
                                if (temp > 100) {
                                    result += "100.0%"
                                    result += "0.0%"
                                } else {
                                    result += temp.toString + "%"
                                    result += ((1 - ((jbslf - ma.head)
                                        / jbslf)) * 100).toString + "%"
                                }
                            } else {
                                result += "" //原告后面无符合条件的金额
                                result += ""
                                result.toArray
                            }

                        } else {
                            //案件受理费这句话中无金额
                            result += ""
                            result += ""
                            result.toArray
                        }
                    } else {
                        //如果不同时含有被告和原告信息
                        if (relative.contains("由原告")) {
                            //由原告承担
                            result += "0.0%"
                            result += "100.0%"
                        } else {
                            //由被告承担
                            result += "100.0%"
                            result += "0.0%"
                        }
                    }

                    result.toArray
                } else {
                    result += ""
                    result += ""
                    result.toArray //属于无需计算的纠纷类型，返回空
                }
            } else {
                result += ""
                result += ""
                result.toArray //不是民事判决书，返回空
            }
        } else {
            result += ""
            result += ""
            result.toArray //无案件受理费
        }
    }

    /**
     * 计算案件受理费
     * @param array
     * @return
     */
    def slfcount(array: Array[String]): Double = {
        //计算案件受理费
        var slf = 0.0
        val keyword1 = "\\S{0,}判决如下：$" //案件受理费这句话在判决如下以后出现
        val k1 = array.indexOf(array.filter(s => s.matches(keyword1)).mkString)
        val relative = array.filter(s => s.matches("\\S{0,}(本案)?案件受理费\\S{0,}") ==
            true).filter(s => array.indexOf(s) > k1).mkString
        //本案件案件受理费。。。。。。。。。。

        val regex = "\\d*，?\\d+\\.?\\d+元".r //匹配金额
        val t = regex.findAllIn(relative).toArray //匹配句子中的所有数字
        try {
            if (t.length == 1) {
                //若金额唯一则为受理费
                slf = t(0).filter(s => s != '，' && s != '元').toDouble
            } else {
                //若不唯一，判断是否有减半字样
                if (relative.contains("减半")) {
                    slf = regex.findFirstIn(relative.split("减半").last).mkString.filter(s => s != '，' && s != '元').toDouble
                } else {
                    //若无则直接取案件受理费后面第一个数字
                    slf = regex.findFirstIn(relative.split("受理费").last).mkString.filter(s => s != '，' && s != '元').toDouble
                }
            }
        } catch {
            case _: Exception => println("受理费读取异常")
        }
        slf
    }

    /**
     * 计算机原告支持率
     * @param array 全文
     * @param ay 案由
     * @param ah 案号
     * @return Tuple2(支持率(原告，被告），诉请金额)
     */
    def supportrate(array: Array[String], ay: Array[String], ah:
    Array[String]): Tuple2[Array[String], String] = {
        val result = new ArrayBuffer[String]()

        val keyword1 = "\\S{0,}判决如下：$" //案件受理费这句话在判决如下以后出现
        val k1 = array.indexOf(array.filter(s => s.matches(keyword1)).mkString)
        val relative = array.filter(s => s.matches("\\S{0,}(本案)?案件受理费\\S{0,}") ==
            true).filter(s => array.indexOf(s) > k1).mkString
        //本案件案件受理费。。。。。。。。。。

        val regex = "\\d*，?\\d+\\.?\\d+元".r //匹配金额表达式
        if (!regex.findFirstIn(relative.split("受理费").last).isEmpty) {
            //若这句话中没有数字则直接取空
            val slf = regex.findFirstIn(relative.split("受理费").last).mkString.filter(s => s != '，' && s != '元').toDouble //案件受理费 不考虑减半
            val sq = sqcount(slf) //倒测诉请金额
            var zc = 0.0 //判决金额
            //判断民事判决书，民初字
            if (condition(array, ah)) {
                //若是，判断是否是需要计算的纠纷类型
                if (jf(ay, slf)) {
                    //计算判决的金额
                    val keyword1 = "\\S{0,}判决如下：$"
                    val keyword2 = "\\S{0,}未按\\S{0,}判决\\S{0,}。"
                    val k1 = array.indexOf(array.filter(s => s.matches(keyword1)).mkString)
                    //val v=array.filter(b => b.matches(keyword2)).head
                    val k2 = array.indexOf(array.filter(s => s.matches(keyword2)).mkString)
                    if (k1 != (-1) || k2 != (-1)) {
                        //k1 k2 为-1时 说明未找到关键字，直接取空
                        for (i <- (k1 + 1) to (k2 - 1)) {
                            //两个关键字之间的判决记录进行处理
                            if (array(i).contains("计")) {
                                //如果有“计”那么按计分组
                                // 取最后一个元素中的第一个数字
                                val m = regex.findFirstIn(array(i).split("计").last)
                                if (!(m.isEmpty)) {
                                    zc += m.mkString.filter(s => s != '，' && s != '元').toDouble
                                }
                            } else {
                                //如果没有“计”，那么把这条记录中的数字都加起来
                                val m = regex.findAllIn(array(i))
                                if (!m.isEmpty) {
                                    zc += m.toArray.map(s => s.filter(p => p != '，' && p != '元').toDouble).sum
                                }
                            }
                        }
                        //                        if(zc==0)
                        //                            throw  new RuntimeException
                        if (zc > sq){
                            zc = sq}
                        result += (zc * 100 / sq).toString + "%"
                        result += ((1 - zc / sq) * 100).toString + "%"
                        Tuple2(result.toArray, sq.toString)
                    } else {
                        result += ""
                        result += ""
                        Tuple2(result.toArray, sq.toString)
                    }
                } else {
                    result += ""
                    result += ""
                    Tuple2(result.toArray, sq.toString)
                }
            } else {
                result += ""
                result += ""
                Tuple2(result.toArray, sq.toString)
            }
        } else {
            result += ""
            result += ""
            Tuple2(result.toArray, "")
        }
    }

    /**
     * 判断是否是需要计算纠纷的类型
     * @param a  案由
     * @param m 受理费
     * @return
     */
    def jf(a: Array[String], m: Double): Boolean = {
        val jf1 = Array("婚约财产纠纷", "离婚纠纷", "离婚后财产纠纷", "离婚后损害责任纠纷", "婚姻无效纠纷", "撤销婚姻纠纷", "夫妻财产约定纠纷", "同居关系纠纷", "抚养纠纷", "扶养纠纷", "赡养纠纷", "收养关系纠纷", "监护权纠纷", "探望权纠纷", "分家析产纠纷")
        val jf2 = Array("生命权", "健康权", "身体权纠纷", "姓名权纠纷", "肖像权纠纷", "名誉权纠纷", "荣誉权纠纷", "隐私权纠纷", "婚姻自主权纠纷", "人身自由权纠纷", "一般人格权纠纷")
        val jf3 = Array("劳动合同纠纷", "福利待遇纠纷", "社会保险纠纷", "人事争议")
        val jf4 = Array("著作权合同纠纷", "商标合同纠纷", "专利合同纠纷", "植物新品种合同纠纷", "集成电路布图设计合同纠纷", "商业秘密合同纠纷", "技术合同纠纷", "特许经营合同纠纷", "企业名称（商号）合同纠纷", "特殊标志合同纠纷", "网络域名合同纠纷", "知识产权质押合同纠纷", "著作权权属、侵权纠纷", "商标权权属、侵权纠纷", "专利权权属、侵权纠纷", "植物新品种权权属、侵权纠纷", "集成电路布图设计专有权权属、侵权纠纷", "侵害企业名称（商号）权纠纷", "侵害特殊标志专有权纠纷", "网络域名权属、侵权纠纷", "发现权纠纷", "发明权纠纷", "其他科技成果权纠纷", "确认不侵害知识产权纠纷", "因申请知识产权临时措施损害责任纠纷", "因恶意提起知识产权诉讼损害责任纠纷", "专利权宣告无效后返还费用纠纷")
        if (a.length > 0) {
            //分别判断是否含有4组纠纷，若有判断受理费是否大于一定值
            for (j <- jf1) {
                if (a(0).contains(j)) {
                    if (m >= 2000.0) {
                        return true
                    }
                    else {
                        return false
                    }
                }
            }
            for (i <- jf2) {
                if (a(0).contains(i)) {
                    if (m >= 1000.0) {
                        return true
                    }
                    else {
                        return false
                    }
                }
            }
            for (k <- jf3) {
                if (a(0).contains(k)) {
                    return false
                }
            }
            for (l <- jf4) {
                if (a(0).contains(l)) {
                    if (m >= 2500.0)
                        return true
                    else
                        return false
                }
            }
            true
        } else {
            true
        }
    }

    /**
     * 判断是否是民事判决书，案号有民初字
     * @param array
     * @param ah
     * @return
     */
    def condition(array: Array[String], ah: Array[String])
    : Boolean = {
        // 标题民事判决书，案号中带民初字
        val ms = array.filter(s => s.matches("\\S{0,}一审民事判决书$") == true).length
        val mcz = ah(0).matches("\\S{0,}（?民）?\\S初字\\S{0,}")
        ms > 0 && mcz
    }

    /**
     * 根据案件受理费倒测诉请金额
     * @param slf
     * @return
     */
    def sqcount(slf: Double): Double = {
        if (slf < 2250 || slf == 2250) {
            slf / 0.025 + 10000
        } else {
            if (slf - 2250 < 2000 || slf - 2250 == 2000) {
                (slf - 2250) / 0.02 + 100000
            } else {
                if (slf - 4250 < 4500 || slf - 4250 == 4500) {
                    (slf - 4250) / 0.015 + 200000
                } else {
                    if (slf - 8750 < 5000 || slf - 8750 == 5000) {
                        (slf - 8750) / 0.01 + 500000
                    } else {
                        if (slf - 13750 < 9000 || slf - 13750 == 9000) {
                            (slf - 13750) / 0.009 + 1000000
                        } else {
                            if (slf - 22750 < 24000 || slf - 22750 == 24000) {
                                (slf - 22750) / 0.008 + 2000000
                            } else {
                                if (slf - 46750 < 35000 || slf - 46750 == 35000) {
                                    (slf - 46750) / 0.007 + 5000000
                                } else {
                                    if (slf - 81750 < 60000 || slf - 81750 == 60000) {
                                        (slf - 81750) / 0.006 + 10000000
                                    } else {
                                        (slf - 141750) / 0.005 + 20000000
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }


}
