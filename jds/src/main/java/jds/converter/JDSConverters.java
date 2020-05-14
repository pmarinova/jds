package jds.converter;

import java.util.function.BiConsumer;
import java.util.function.Function;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class JDSConverters {

	public static JDSConverterRegistry registry() {
		return new JDSConverterRegistry()
				.useJavaSerialization( true )
				.put( String.class, new JDSStringConverter() )
				.put( Integer.class, new TupleConverter<>( (i, o) -> o.writeInt( i ), TupleInput::readInt ) )
				.put( int.class, new TupleConverter<>( (i, o) -> o.writeInt( i ), TupleInput::readInt ) )
				.put( Long.class, new TupleConverter<>( (i, o) -> o.writeLong( i ), TupleInput::readLong ) )
				.put( long.class, new TupleConverter<>( (i, o) -> o.writeLong( i ), TupleInput::readLong ) )
		;
	}
	
	private static class TupleConverter<T> implements JDSConverter<T> {

		private final BiConsumer<T, TupleOutput> serializer;
		
		private final Function<TupleInput, T> deserializer;
		
		public TupleConverter(
				BiConsumer<T, TupleOutput> serializer,
				Function<TupleInput, T> deserializer
				) 
		{
			this.serializer = serializer;
			this.deserializer = deserializer;
		}

		@Override
		public byte[] serialize( T inst ) {
			TupleOutput s = new TupleOutput();
			serializer.accept (inst, s );
			return s.getBufferBytes();
		}

		@Override
		public T deserialize ( byte[] data ) {
			TupleInput in = new TupleInput( data );
			return deserializer.apply( in );
		}
	}
}
