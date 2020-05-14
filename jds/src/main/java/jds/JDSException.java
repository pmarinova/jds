package jds;

public class JDSException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public JDSException() {
		super();
	}

	public JDSException(String message, Throwable cause) {
		super(message, cause);
	}

	public JDSException(String message) {
		super(message);
	}

	public JDSException(Throwable cause) {
		super(cause);
	}
}
