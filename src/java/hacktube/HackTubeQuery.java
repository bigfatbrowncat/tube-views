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

public class HackTubeQuery {

	private static final String USER_AGENT = "Mozilla/5.0";

	private volatile static HttpsURLConnection connection;
	
	public static void interruptConnection() {
		if (connection != null) connection.disconnect();
	}
	
	public static JSONObject requestJSON(List<String> videoIds) throws RequestException {
		if (videoIds.size() == 0) return new JSONObject();
		
		String queryTemplateBegin = 
		"{\r\n" + 
		"	\"method\": \"query\",\r\n" + 
		"	\"params\": \r\n" + 
		"	{\r\n" + 
		"		\"1\": \r\n" + 
		"		[\r\n"; 

		String queryTemplatePartAFormat = 
		"			{\r\n" + 
		"				\"1\": [[null, 236], [null, 307]],\r\n" + 
		"				\"3\": [[null, 236]],\r\n" + 
		"				\"7\": 1,\r\n" + 
		"				\"8\": 1,\r\n" + 
		"				\"10\": [{\r\n" + 
		"					\"1\": {\"1\": 4},\r\n" + 
		"					\"2\": [{\r\n" + 
		"						\"1\": 0,\r\n" + 
		"						\"2\": \"%1$s\"\r\n" + 
		"					}]\r\n" + 
		"				}]\r\n" + 
		"			},\r\n" + 
		"			{\r\n" + 
		"				\"1\": [[null, 237], [null, 307]],\r\n" + 
		"				\"3\": [[null, 237]],\r\n" + 
		"				\"7\": 1,\r\n" + 
		"				\"8\": 1,\r\n" + 
		"				\"10\": \r\n" + 
		"				[{\r\n" + 
		"					\"1\": {\"1\": 4},\r\n" + 
		"					\"2\": [{\r\n" + 
		"						\"1\": 0,\r\n" + 
		"						\"2\": \"%1$s\"\r\n" + 
		"					}]\r\n" + 
		"				}]\r\n" + 
		"			}";
		String queryTemplatePartADelim = ",\r\n";
		String queryTemplateMiddle =
					"\r\n" + 
		"		],\r\n" + 
		"\r\n" + 
		"		\"2\": \r\n" + 
		"		{\r\n" + 
		"			\"1\": ["; 
		String queryTemplatePartB = "{\"1\": 4}";
		String queryTemplatePartBDelim = ", ";
		
		
		String queryTemplateEnd =
						"],\r\n" + 
		"			\"2\": \"en_US\"\r\n" + 
		"		}\r\n" + 
		"\r\n" + 
		"	}\r\n" + 
		"}";

		StringBuilder queryBuilder = new StringBuilder(queryTemplateBegin);
		for (int i = 0; i < videoIds.size() - 1; i++) {
			queryBuilder.append(String.format(queryTemplatePartAFormat, videoIds.get(i)));
			queryBuilder.append(queryTemplatePartADelim);
		}
		queryBuilder.append(String.format(queryTemplatePartAFormat, videoIds.get(videoIds.size() - 1)));
		queryBuilder.append(queryTemplateMiddle);
		for (int i = 0; i < videoIds.size() * 2 - 1; i++) {
			queryBuilder.append(queryTemplatePartB);
			queryBuilder.append(queryTemplatePartBDelim);
		}
		queryBuilder.append(queryTemplatePartB);
		queryBuilder.append(queryTemplateEnd);
		
		try {
			String url = "https://www.youtube.com/api/analytics/yta/query";
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
			connection.setRequestProperty("x-youtube-ytatoken", "c" + Configuration.getInstance().account.youtubeUserId);			// Necessary (error -302 instead)
	
			// Configuring post request
			connection.setDoOutput(true);
	
			// Sending the request
			DataOutputStream queryStream = new DataOutputStream(connection.getOutputStream());
			BufferedWriter queryWriter = new BufferedWriter(new OutputStreamWriter(queryStream));
			queryWriter.write(queryBuilder.toString());
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
