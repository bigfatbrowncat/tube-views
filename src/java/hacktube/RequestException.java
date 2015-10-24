package hacktube;
@SuppressWarnings("serial")
public class RequestException extends HackTubeException {
	public RequestException(Throwable cause) {
		super("Request to YouTube failed", cause);
	}
	
	public RequestException(String message) {
		super(message);
	}
}
