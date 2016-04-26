import org.apache.log4j.net.SocketServer;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by hy on 16-4-18.
 */
public class test {
    public static void main(String[] args) throws Exception {
        PrintWriter pw = null;
        Socket s = null;
        OutputStream os = null;
        try {
            ServerSocket ss = new ServerSocket(9998);
            while (true) {
                s = ss.accept();
                os = s.getOutputStream();
                pw = new PrintWriter(os);
                pw.println("1310 180 350 350 8500");
                pw.flush();
                s.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pw.close();
            os.close();
        }
    }
}
