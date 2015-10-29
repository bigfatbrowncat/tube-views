package hacktube;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.util.List;

public class HackTube {
	
	static {
        CookieHandler.setDefault(new CookieManager());
        Configuration conf = Configuration.getInstance();
        if (conf.network != null) {
        	if (conf.network.httpProxy != null) {
        		System.setProperty("http.proxyHost", conf.network.httpProxy.host);
        		System.setProperty("http.proxyPort", String.valueOf(conf.network.httpProxy.port));
        	}
        	if (conf.network.httpsProxy != null) {
        		System.setProperty("https.proxyHost", conf.network.httpsProxy.host);
        		System.setProperty("https.proxyPort", String.valueOf(conf.network.httpsProxy.port));
        	}
        }
	}

	private static void printCookies() {
		CookieManager manager = (CookieManager) CookieHandler.getDefault();
		CookieStore cookieJar = manager.getCookieStore();
		List<HttpCookie> cookies = cookieJar.getCookies();
		for (HttpCookie cookie : cookies) {
			System.out.println("Cookie: " + cookie);
		}
	}
	
	public static void login() throws HackTubeException {
		try {
			System.out.println(" -------------------- STAGE 1 ----------------------");
	
			HackTubeServiceLogin.requestLoginPage();
			printCookies();
			
			System.out.println(" -------------------- STAGE 2 ----------------------");
			
			HackTubeServiceLoginAuth.requestLoginPage(Configuration.getInstance().account.email, Configuration.getInstance().account.password);
			printCookies();
			
	        System.out.println(" -------------------- STAGE 3 ----------------------");
		} catch (RequestException e) {
			throw new HackTubeException(e);
		}

	}
}
