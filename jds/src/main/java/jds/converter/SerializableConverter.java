package jds.converter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class SerializableConverter<T> implements JDSConverter<T> {

	@Override
	public byte[] serialize( T inst ) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try ( ObjectOutputStream o = new ObjectOutputStream( out ) ) {
			o.writeObject( inst );
			return out.toByteArray();
		} 
		catch ( IOException e ) {
			throw new RuntimeException( e );
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public T deserialize( byte[] data ) {
		ByteArrayInputStream in = new ByteArrayInputStream( data );
		try( ObjectInputStream i = new ObjectInputStream( in ) ) {
			return (T) i.readObject();
		} 
		catch  (IOException | ClassNotFoundException e ) {
			throw new RuntimeException( e );
		}
	}
}
