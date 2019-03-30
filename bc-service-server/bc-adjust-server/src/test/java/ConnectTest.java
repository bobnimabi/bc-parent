import java.io.IOException;
import java.net.*;

/**
 * Created by mrt on 2019/3/29 0029 下午 7:00
 */
public class ConnectTest {

    public static void main(String[] args) throws Exception{
        getcookies();
    }


    public static String getcookies() throws IOException {
        CookieManager manager=new CookieManager();
        manager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        CookieHandler.setDefault(manager);
        URL url=new URL("https://ovwyfq040.prealmd.com/agent/agent");
        HttpURLConnection conn= (HttpURLConnection) url.openConnection();
        conn.getHeaderFields();
        CookieStore store = manager.getCookieStore();
        String cookies = store.getCookies().toString();
        cookies = cookies.replace("[", "");
        cookies = cookies.replace("]", "");

        return cookies;
    }


}
