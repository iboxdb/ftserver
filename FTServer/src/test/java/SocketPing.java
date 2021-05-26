
import java.io.IOException;
import java.net.*;

public class SocketPing {

    public static void main(String[] arg) throws IOException {
        
        System.out.println("Ping");
        System.out.println("systemctl stop firewalld");
        ServerSocket ss = new ServerSocket(9000);
        while (true) {
            Socket s = ss.accept();
            System.out.println(System.currentTimeMillis());
            System.out.println(((InetSocketAddress) s.getLocalSocketAddress()).getAddress().getHostAddress());
            System.out.println(((InetSocketAddress) s.getRemoteSocketAddress()).getAddress().getHostAddress());
            int a = s.getInputStream().read();
            a++;
            s.getOutputStream().write(a);
            s.getOutputStream().flush();
            s.close();
            System.out.println();
        }
    }
}
