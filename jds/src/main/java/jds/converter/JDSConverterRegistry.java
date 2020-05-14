package jds.converter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class JDSConverterRegistry {

	private final Map<Class<?>, JDSConverter<?>> items = new HashMap<>();

	private boolean useJavaSerialization = true;
	
	public <T> JDSConverter<T> converter( Class<T> t ) {
		
		JDSConverter<T> c = getConverter( t );
		
		if ( c != null ) {
			return c;
		}
		
		if ( useJavaSerialization && Serializable.class.isAssignableFrom( t ) ) {
			return new SerializableConverter<>();
		}
		
		throw new IllegalArgumentException( "No converter registered for: " + t );
	}
	
	@SuppressWarnings("unchecked")
	public <T> JDSConverter<T> getConverter( Class<T> t ) {
		return (JDSConverter<T>) items.get( t );
	}
	
	public <T> JDSConverterRegistry put( Class<T> t, JDSConverter<T> c ) {
		items.put( t, c );
		return this;
	}
	
	public JDSConverterRegistry useJavaSerialization( boolean b ) {
		this.useJavaSerialization = b;
		return this;
	}
	
	public boolean useJavaSerialization() {
		return useJavaSerialization;
	}
}
