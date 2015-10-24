package hacktube;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import org.json.JSONObject;

public class HackTubeSearchQuery {

	private static final String USER_AGENT = "Mozilla/5.0";

	private volatile static HttpsURLConnection connection;
	
	public static void interruptConnection() {
		if (connection != null) connection.disconnect();
	}
	
	public static JSONObject requestJSON() throws RequestException {
		
		String query = 
		"{\"method\":\"search\",\"params\":{\"1\":[{\"1\":\"\",\"2\":[[,4]],\"3\":{\"1\":1},\"8\":{\"2\":5}},{\"1\":\"\",\"2\":[[,132]],\"3\":{\"1\":0},\"5\":{\"2\":1},\"8\":{\"2\":5}}],\"6\":{\"3\":\"#r=summary;fi=u-TEfV3TZMNwL2a8a9SoRNKg\"}}}";

		try {
			String url = "https://www.youtube.com/api/analytics/yta/search";
			URL obj = new URL(url);
	
			connection = (HttpsURLConnection) obj.openConnection();
	
			// add request headers
			connection.setRequestMethod("POST");
			connection.setRequestProperty("accept", "*/*");
			connection.setRequestProperty("content-type", "application/javascript; charset=UTF-8");
			connection.setRequestProperty("User-Agent", USER_AGENT);
			
			connection.setRequestProperty("origin", "https://www.youtube.com");
			connection.setRequestProperty("referer", "https://www.youtube.com/analytics?o=U");

			connection.setRequestProperty("x-gwt-permutation", "0");	// Necessary (404 instead)
			connection.setRequestProperty("x-youtube-ytatoken", "c" + Configuration.getInstance().youtubeUserId);			// Necessary (error -302 instead)
	
			// Configuring post request
			connection.setDoOutput(true);
	
			// Sending the request
			DataOutputStream queryStream = new DataOutputStream(connection.getOutputStream());
			BufferedWriter queryWriter = new BufferedWriter(new OutputStreamWriter(queryStream));
			queryWriter.write(query);
			queryWriter.flush();
			queryWriter.close();
			
			int responseCode = connection.getResponseCode();
			System.out.println("Sending 'POST' request to URL : " + url);
			System.out.println("Response Code : " + responseCode);
	
			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();
			
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			
			// print result
			String respJSON = response.toString();
			
			JSONObject resp = new JSONObject(respJSON);
			
			return resp;
		} catch (IOException e) {
			throw new RequestException(e);
		} finally {
			connection = null;
		}
	}
}
