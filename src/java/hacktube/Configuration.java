package hacktube;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;

import org.json.JSONException;
import org.json.JSONObject;

public class Configuration {
	
	@SuppressWarnings("serial")
	public static class Exception extends RuntimeException {
		public Exception(Throwable t) {
			super(t);
		}
		public Exception(String message) {
			super(message);
		}
	}

	public class Account {
		
		public final String email;
		public final String password;
		public final String youtubeUserId;
		
		Account(JSONObject configObj) {
			email = configObj.getString("email");
			password = configObj.getString("password");
			youtubeUserId = configObj.getString("youtube-user-id");

		}
	}

	public class Network {

		public class Proxy {
			
			public final String host;
			public final int port;
			
			public Proxy(JSONObject proxyObj) {
				host = proxyObj.getString("host");
				port = Integer.parseInt(proxyObj.getString("port"));
			}
		}
	
		public final Proxy httpProxy, httpsProxy;
		
		public Network(JSONObject networkObj) {
			JSONObject httpProxyObj = networkObj.getJSONObject("http-proxy");
			JSONObject httpsProxyObj = networkObj.getJSONObject("https-proxy");
			if (httpProxyObj != null) {
				httpProxy = new Proxy(httpProxyObj); 
			} else {
				httpProxy = null;
			}
			
			if (httpsProxyObj != null) {
				httpsProxy = new Proxy(httpsProxyObj); 
			} else {
				httpsProxy = null;
			}
			
		}
	}
	
	private static Configuration configuration;
	
	public final Account account;
	public final Network network;
	
	public static String readFileAsString(File file) throws IOException {
	    DataInputStream dis = new DataInputStream(new FileInputStream(file));
	    try {
	        long len = file.length();
	        if (len > Integer.MAX_VALUE) throw new IOException("File is too large: " + len +" bytes.");
	        byte[] bytes = new byte[(int) len];
	        dis.readFully(bytes);
	        return new String(bytes, "UTF-8");
	    } finally {
	        dis.close();
	    }
	}
	
	Configuration(File configFile) {
		try {
			String contents = readFileAsString(configFile);
			
			JSONObject confObj = new JSONObject(contents);
			
			if (confObj.has("account")) {
				JSONObject accountObj = confObj.getJSONObject("account");
				account = new Account(accountObj);				
			} else {
				throw new Exception("missing account field");
			}

			if (confObj.has("network")) {
				JSONObject networkObj = confObj.getJSONObject("network");
				network = new Network(networkObj);
			} else {
				network = null;
			}
			
		} catch (IOException | JSONException e) {
			throw new Exception(e);
		}
	}

	public static Configuration getInstance() {
		if (configuration == null) {
			configuration = new Configuration(new File("login.json"));
		}
		return configuration;
	}
}
