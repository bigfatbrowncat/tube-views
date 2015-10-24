package hacktube;

@SuppressWarnings("serial")
public class HackTubeException extends Exception {

	public HackTubeException() {
		super();
	}

	public HackTubeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public HackTubeException(String message, Throwable cause) {
		super(message, cause);
	}

	public HackTubeException(String message) {
		super(message);
	}

	public HackTubeException(Throwable cause) {
		super(cause);
	}
	
}
