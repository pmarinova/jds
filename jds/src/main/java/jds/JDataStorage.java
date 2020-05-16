package jds;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;

import jds.converter.JDSConverter;
import jds.converter.JDSConverterRegistry;
import jds.converter.JDSConverters;

public class JDataStorage {

	private String dbHomeFolder = "db";
	
	private Environment env;
	
	private final Map<Class<?>, TableStorage<?, ?>> tables = new HashMap<>();
	
	private JDSConverterRegistry converters = JDSConverters.registry();
	
	public void connect() {

		EnvironmentConfig envConfig = new EnvironmentConfig();
		envConfig.setAllowCreate( true );
		envConfig.setTransactional( true );
		envConfig.setReadOnly( false );
		
		File dbHome = new File( dbHomeFolder );
		
		if ( !dbHome.exists() ) {
			dbHome.mkdirs();
		}
		
		this.env = new Environment( dbHome, envConfig );

		DatabaseConfig dbConfig = new DatabaseConfig();
		dbConfig.setAllowCreate( true );
		dbConfig.setTransactional( true );
		
		transactional( () -> {
			
			for ( TableStorage<?, ?> t : tables.values() ) {
				t.connect( this.env );
			}
			
		});
	}
	
	public JDSTransaction beginTransaction() {
		JDSTransaction tx = new JDSTransaction( env.beginTransaction( null, null ) );
		tx.setup();
		return tx;
	}
	
	public JDSTransaction currentTransaction() {
		return Transactions.currentJDSTransaction();
	}
	
	public void transactional( Runnable r ) {
		
		JDSTransaction tx = beginTransaction();
		
		try {
			r.run();
			tx.commit();
		}
		finally {
			if ( tx.isValid() ) {
				tx.abort();
			}
		}
	}
	
	public <T> T transactionalFn( Supplier<T> r ) {
		
		JDSTransaction tx = beginTransaction();
		
		try {
			T result = r.get();
			tx.commit();
			return result;
		}
		finally {
			if ( tx.isValid() ) {
				tx.abort();
			}
		}
	}
	
	public boolean insert( Object o ) {
		TableStorage<Object, Object> table = requireStorage( o );
		return table.insert( o );
	}
	
	public boolean update( Object o ) {
		TableStorage<Object, Object> table = requireStorage( o );
		return table.update( o );
	}
	
	public boolean store( Object o ) {
		TableStorage<Object, Object> table = requireStorage( o );
		return table.store( o );
	}
	
	public boolean delete( Object o ) {
		TableStorage<Object, Object> table = requireStorage( o );
		return table.delete( o );
	}
	
	public <K, V> boolean deleteByKey( Class<V> type, K key ) {
		TableStorage<K, V> table = requireStorageForType( type );
		return table.deleteByKey( key );
	}
	
	public <K, V> V get( Class<V> type, K key ) {
		TableStorage<K, V> table = requireStorageForType( type );
		return table.get( key );
	}
	
	public <T> List<T> list( Class<T> type ) {
		List<T> r = new ArrayList<>();
		forEachRecord( type, r::add );
		return r;
	}
	
	public <T> void forEachRecord( Class<T> type, Consumer<T> c ) {
		TableStorage<Object, T> table = requireStorageForType( type );
		table.forEachRecord( c );
	}
	
	public <K, T> T uniqueIndexGet( Class<T> type, String idx, K key ) {
		TableStorage<Object, T> table = requireStorageForType( type );
		return table.uniqueIndexGet( idx, key );
	}
	
	public <K, T> List<T> nonUniqueIndexGet( Class<T> type, String idx, K key ) {
		TableStorage<Object, T> table = requireStorageForType( type );
		return table.nonUniqueIndexGet( idx, key );
	}
	
	@SuppressWarnings("unchecked")
	private <K, V> TableStorage<K, V> requireStorage( V o ) {
		Class<V> type = (Class<V>) o.getClass();
		return requireStorageForType( type );
	}
	
	private <K, V> TableStorage<K, V> requireStorageForType( Class<V> type ) {
		TableStorage<K, V> t =  storage( type );
		
		if ( t == null ) {
			throw new IllegalStateException( "No table for: " + type );
		}
		
		return t;
	}
	
	@SuppressWarnings("unchecked")
	public <K, V> TableStorage<K, V> storage( Class<V> dataType ) {
		return (TableStorage<K, V>) tables.get( dataType );
	}
	
	public void close() {
		
		if ( env == null || env.isClosed() ) {
			return ;
		}
		
		tables.values().forEach( TableStorage::close );
		
		if ( !env.isClosed() ) {
			silently( env::close );
		}
	}
	
	public JDataStorage homeFolder( String f ) {
		this.dbHomeFolder = f;
		return this;
	}
	
	public <T> JDataStorage converter( Class<T> type, JDSConverter<T> c ) {
		converters.put( type, c ); 
		return this;
	}
	
	public <T> JDataStorage converterRegistry( JDSConverterRegistry r ) {
		this.converters = Objects.requireNonNull( r, "No registry specified" );
		return this;
	}
	
	public <K, V> JDataStorage with( JDSTable<K, V> table ) {
		Objects.requireNonNull( table.name(), "Table name required" );
		Objects.requireNonNull( table.keyType(), "Table keyType required" );
		Objects.requireNonNull( table.valueType(), "Table valueType required" );
		Objects.requireNonNull( table.keyProvider(), "Table keyProvider required" );
		
		if ( table.keyConverter() == null ) {
			table.keyConverter( converters.converter( table.keyType() ) );
		}
		
		if ( table.valueConverter() == null ) {
			table.valueConverter( converters.converter( table.valueType() ) );
		}
		
		this.tables.put( table.valueType(), new TableStorage<>( this, table ) );
		return this;
	}
	
	static void silently( Runnable r ) {
		try {
			r.run();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unchecked")
	public byte[] serialize( Object o ) {
		Class<Object> c = (Class<Object>) o.getClass();
		return converters.converter( c ).serialize( o );
	}
	
	public <T> T deserialize( byte[] data, Class<T> target ) {
		return converters.converter( target ).deserialize( data );
	}
}
