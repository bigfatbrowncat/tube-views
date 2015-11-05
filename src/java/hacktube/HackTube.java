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
	
	private static volatile boolean cancelled = false;
	
	public static void cancelAllConnections() {
		cancelled = true;
		HackTubeServiceLogin.interruptConnection();
		HackTubeServiceLoginAuth.interruptConnection();
		HackTubeQuery.interruptConnection();
		HackTubeSearch.interruptConnection();
	}

	private static void printCookies() {
		CookieManager manager = (CookieManager) CookieHandler.getDefault();
		CookieStore cookieJar = manager.getCookieStore();
		List<HttpCookie> cookies = cookieJar.getCookies();
		for (HttpCookie cookie : cookies) {
			System.out.println("Cookie: " + cookie);
		}
	}
	
	public static boolean login() throws RequestException {
		System.out.println("Stage 1: Opening login page");

		try {
			HackTubeServiceLogin.requestLoginPage();
			printCookies();
		} catch (RequestException e) {
			if (!cancelled) {
				throw e;
			}
		}
		if (cancelled) return false;
		
		System.out.println("Stage 2: Logging into YouTube");

		try {
			HackTubeServiceLoginAuth.requestLoginPage(Configuration.getInstance().account.email, Configuration.getInstance().account.password);
			printCookies();
		} catch (RequestException e) {
			if (!cancelled) {
				throw e;
			}
		}
		if (cancelled) return false;
		
        return true;
	}
}
