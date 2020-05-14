package jds.converter;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class JDSStringConverter implements JDSConverter<String> {

	private static final byte[] EMPTY = new byte[0];
	
	private final Charset encoding;
	
	public JDSStringConverter() {
		this( StandardCharsets.UTF_8 );
	}
	
	public JDSStringConverter(Charset encoding) {
		this.encoding = encoding;
	}

	@Override
	public byte[] serialize(String inst) {
		if ( inst == null ) {
			return EMPTY;
		}
		return inst.getBytes( encoding );
	}

	@Override
	public String deserialize(byte[] data) {
		if ( data == null || data.length == 0 ) {
			return null;
		}
		return new String( data, encoding );
	}

}
