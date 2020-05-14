package jds.converter;

public interface JDSConverter<T> {

	byte[] serialize( T inst );
	
	T deserialize( byte[] data ); 
}
