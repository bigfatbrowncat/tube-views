package hacktube;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class HackTubeLoginStage2 {
	
	private static final String USER_AGENT = "Mozilla/5.0";
	
	private volatile static HttpsURLConnection connection;
	
	public static void interruptConnection() {
		if (connection != null) connection.disconnect();
	}

	public static void requestLoginPage(String email, String passwd) throws RequestException {
		HttpsURLConnection.setFollowRedirects(true);
		
		try {
			String url = "https://accounts.google.com/ServiceLoginAuth";
			URL obj = new URL(url);
	
			connection = (HttpsURLConnection) obj.openConnection();
	
			// add request headers
			connection.setRequestMethod("POST");
			connection.setRequestProperty("accept", "*/*");
			connection.setRequestProperty("content-type", "application/x-www-form-urlencoded");
			connection.setRequestProperty("User-Agent", USER_AGENT);
			connection.setRequestProperty("origin", "https://accounts.google.com");
			connection.setRequestProperty("referer", "https://accounts.google.com/AccountLoginInfo");

			ArrayList<String> formParameters = new ArrayList<>();
			formParameters.add("Page=PasswordSeparationSignIn");

			CookieManager manager = (CookieManager)CookieHandler.getDefault();
			CookieStore cookieJar =  manager.getCookieStore();
			List<HttpCookie> cookies = cookieJar.getCookies();
			boolean galxFound = false;
			for (HttpCookie cookie: cookies) {
				if (cookie.getName().equals("GALX")) {
					formParameters.add("GALX=" + cookie.getValue());
					galxFound = true;
				}
			}
			if (!galxFound) {
				throw new RequestException("GALX missing");
			}

			formParameters.add("continue=https://www.youtube.com/signin?next=%2F&action_handle_signin=true&hl=en&app=desktop&feature=shortcut");
			formParameters.add("service=youtube");
			formParameters.add("hl=en");
			formParameters.add("checkConnection=");
			formParameters.add("pstMsg=0");
			formParameters.add("bgresponse=js_disabled");
			formParameters.add("dnConn=");
			formParameters.add("checkConnection=");
			formParameters.add("checkedDomains=youtube");
			formParameters.add("Email=" + email);
			formParameters.add("Passwd=" + passwd);
			formParameters.add("signIn=Sign%20in");
			formParameters.add("PersistentCookie=yes");
			formParameters.add("rmShown=1");
			
			connection.setDoOutput(true);
			
			OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream(), "8859_1");   
			for (int i = 0; i < formParameters.size() - 1; i++) {
				out.write(formParameters.get(i));
				out.write('&');
			}
			out.write(formParameters.get(formParameters.size() - 1));
			out.flush();   
			out.close();
			
			int responseCode = connection.getResponseCode();

			System.out.println("\nSending 'POST' request to URL : " + url);
			System.out.println("Response Code : " + responseCode);
	
			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();
			
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
				//System.out.println(inputLine);
			}
			in.close();
			
		} catch (IOException e) {
			throw new RequestException(e);
		} finally {
			connection = null;
		}
	}
}
