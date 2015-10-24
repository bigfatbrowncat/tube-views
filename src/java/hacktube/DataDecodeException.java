package hacktube;
@SuppressWarnings("serial")
public class DataDecodeException extends HackTubeException {
	public DataDecodeException(Throwable cause) {
		super("Can't decode the data", cause);
	}
	public DataDecodeException(String message) {
		super(message);
	}
}
