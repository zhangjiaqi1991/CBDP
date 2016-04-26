package test
/**
  * Created by hy on 15-12-14.
  */
object test_text extends App {
  //  val regex = "t\\S+\\.".r
  //  val f1 = new File("/home/hy/t20013_231231.htm")
  //  println(f1.getName)
  //  val filename = regex.findFirstIn(f1.getName).mkString.filter(s => s != '.')
  //  print(filename)
  class A {
    var str: String = null
  }

  val a = new A()
  val b = new A()
  a.str = new String("1")
  b.str = new String("1")
  println(a.str == b.str)


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
