package text
import text.find._

import scala.collection.mutable.ArrayBuffer
/**
 * Created by hy on 15-12-18.
 */
object tools {

        /**
         * 原告
         * @param array
         * @return 原告人信息，第一次出现原告信息在原文中的位置
         */
        def accuser(array: Array[String]): Tuple2[Array[String], Int] = {
//            if((findall(array, "^原告\\S{0,}。")_1).length==0)
//                throw new Exception
            findall(array, "^原告\\S{0,}。")

        }

        /**
         * 被告
         * @param array
         * @return
         */
        def accused(array: Array[String]): Tuple2[Array[String], Int] = {
            findall(array, "^被告\\S{0,}。")
        }


        /**
         * 原被告委托人和事务所信息
         * @param array
         * @param index 第一个被告人信息出现的位置
         * @return 原告委托人，被告委托人，事务所信息
         */

        def rep(array: Array[String], index: Int): Tuple3[Array[String], Array[String], Array[String]] = {
            var ind = 0
            if (index == (-1)) {
                //若无被告人信息则所有委托人为原告的，被告出现位置取较大值
                ind = 1000
            } else {
                ind = index
            }
            val errep = new ArrayBuffer[String]()
            val edrep = new ArrayBuffer[String]()
            val lawyer = new ArrayBuffer[String]()
            val regex = "\\S{0,4}委托代理人\\S{0,}"
            val relative = array.filter(s => s.matches(regex) == true)
            for (v <- relative) {
                if (array.indexOf(v) > ind) {
                    //被告委托人
                    if (v.contains("律师")) {
                        //判断是否是律师，提取律师事务所信息
                        val temp = v.split("，")
                        edrep += temp(0)
                      if(temp.length>1){
                        if (lawyer.contains(temp(1)) == false)
                            lawyer += temp(1)
                      }
                    } else {
                        //如果不是律师就直接放入结果中
                        edrep += v
                    }
                } else {
                    //原告委托人
                    if (v.contains("律师")) {
                        //判断是否是律师，提取律师事务所信息
                        val temp = v.split("，")
                        errep += temp(0)
                      if(temp.length>1){
                        if (lawyer.contains(temp(1)) == false)
                            lawyer += temp(1)
                      }
                    } else {
                        //如果不是律师就直接放入结果中
                        errep += v
                    }
                }
            }
            Tuple3(errep.toArray, edrep.toArray, lawyer.toArray)
        }

        /**
         *
         * @param array
         * @return 时间
         */
        def time(array: Array[String]): Array[String] = {
            findonly(array, "^二〇\\S{0,}")
        }

        /**
         *
         * @param array
         * @return 书记，审判长，审判员，代理审判员，案号，有些字间带空格，有些不带
         */
        def record(array: Array[String]): Array[String] = {
            findonly(array, "^书\\S{0,}记\\S{0,}员\\S{0,}")
        }

        def chiefjudge(array: Array[String]): Array[String] = {
            findonly(array, "^审\\S{0,}判\\S{0,}长\\S{0,}")

        }

        def judge(array: Array[String]): Array[String] = {
            findonly(array, "^审\\S{0,}判\\S{0,}员\\S{0,}")

        }

        def proxyjudge(array: Array[String]): Array[String] = {
            findonly(array, "^代理审判员\\S{0,}")
        }

        /**
         * 案号
         * @param array
         * @return
         */
        def casenum(array: Array[String]): Array[String] = {
            findonly(array, "^（\\d{4}）\\S{0,}")
        }

        /**
         * 案由
         * @param array
         * @return
         */
        def casecause(array: Array[String]): Array[String] = {
            val result = new ArrayBuffer[String]()
            if (findonly(array, "^\\S{0,}纠纷\\S{0,}书").length > 0) {
                //若标题带了案由，则用标题提取案由
                val temp = findonly(array, "^\\S{0,}纠纷\\S{0,}书")
                result += temp(0).split("纠纷")(0) + "纠纷"
            } else {
                //若标题不带案由，则由案号后，XXXX纠纷一案，XXXX即为案由
                val temp = findonly(array, "^\\S{0,}纠纷一案\\S{0,}")
                if (temp.length > 0) {
                    result += temp(0).split("纠纷")(0) + "纠纷"
                }
            }
            result.toArray
        }

        /**
         * 法院
         * @param array
         * @return
         */
        def court(array: Array[String]): Array[String] = {
            findonly(array, "\\S{0,}法院$")
        }

        /**
         * 审级
         * @param array  案号!
         * @return
         */
        def judgetime(array: Array[String]): Array[String] = {
            val result = new ArrayBuffer[String]()
            if(!array.isEmpty) {
                if (array(0).contains("初")) {
                    result += "一审"
                } else {
                    result += "二审"
                }
            }
            result.toArray
        }
}
