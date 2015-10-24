package hacktube;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class HackTubeLoginStage1 {

	private static final String USER_AGENT = "Mozilla/5.0";
	
	private volatile static HttpsURLConnection connection;
	
	public static void interruptConnection() {
		if (connection != null) connection.disconnect();
	}
	
	public static void requestLoginPage() throws RequestException {
		try {
			String url = "https://accounts.google.com/ServiceLogin";
			URL obj = new URL(url);
	
			connection = (HttpsURLConnection) obj.openConnection();
	
			// add request headers
			connection.setRequestMethod("GET");
			connection.setRequestProperty("accept", "*/*");
			connection.setRequestProperty("content-type", "application/javascript; charset=UTF-8");
			connection.setRequestProperty("User-Agent", USER_AGENT);
		
			int responseCode = connection.getResponseCode();

			System.out.println("\nSending 'GET' request to URL : " + url);
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
