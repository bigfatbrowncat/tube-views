package hacktube;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.json.JSONException;
import org.json.JSONObject;

public class Configuration {
	
	@SuppressWarnings("serial")
	public static class ConfigurationException extends RuntimeException {
		public ConfigurationException(Throwable t) {
			super(t);
		}
	}
	
	public final String email;
	public final String password;
	public final String youtubeUserId;
	
	Configuration(File configFile) {
		try {
			byte[] encoded = Files.readAllBytes(Paths.get(configFile.toURI()));
			String contents = new String(encoded, StandardCharsets.UTF_8);
	
			JSONObject configObj = new JSONObject(contents);
			
			email = configObj.getString("email");
			password = configObj.getString("password");
			youtubeUserId = configObj.getString("youtube-user-id");
			
		} catch (IOException | JSONException e) {
			throw new ConfigurationException(e);
		}
	}
	
	private static Configuration configuration;
	
	public static Configuration getInstance() {
		if (configuration == null) {
			configuration = new Configuration(new File("login.json"));
		}
		return configuration;
	}
}
