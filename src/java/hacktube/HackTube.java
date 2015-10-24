package hacktube;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.util.List;

public class HackTube {
	
	static {
        CookieHandler.setDefault(new CookieManager());
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
	
			HackTubeLoginStage1.requestLoginPage();
			printCookies();
			
			System.out.println(" -------------------- STAGE 2 ----------------------");
			
			HackTubeLoginStage2.requestLoginPage(Configuration.getInstance().email, Configuration.getInstance().password);
			printCookies();
			
	        System.out.println(" -------------------- STAGE 3 ----------------------");
		} catch (RequestException e) {
			throw new HackTubeException(e);
		}

	}
}
