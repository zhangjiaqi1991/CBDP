package test

import java.io.{PrintStream, ByteArrayOutputStream, Writer, File}
import java.util.Scanner
import org.json4s.Reader

import scala.sys.process._
/**
  * Created by hy on 15-12-14.
  */
object test_text extends App {
  //  val regex = "t\\S+\\.".r
  //  val f1 = new File("/home/hy/t20013_231231.htm")
  //  println(f1.getName)
  //  val filename = regex.findFirstIn(f1.getName).mkString.filter(s => s != '.')
  //  print(filename)

  val baoStream = new ByteArrayOutputStream(1024);
  // cache stream
  val cacheStream = new PrintStream(baoStream);
  val oldStream = System.out;
  System.setOut(cacheStream);
  println("echo wtf")
  val message = baoStream.toString();
  System.setOut(oldStream);
  println(message)
  //  val t=new Thread(new Runnable(){
  //    val cin=new Scanner(System.in)
  //    def  run(){
  //      while(cin.hasNextLine){
  //        println("***********")
  //        a+:cin.nextLine()
  //      }
  //    }
  //  }).start()
  //val ls= "sh /home/hy/run.sh" !;




}
