package jds;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.Environment;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

public class TableStorage<K, V> {

	private final JDSTable<K, V> table;

	private Database db;

	private final Map<String, IndexStorage<?, V>> indexStores = new HashMap<>(); 
	
	public TableStorage( JDataStorage storage, JDSTable<K, V> table ) {
		this.table = table;
		
		for ( JDSIndex<?, V> i : table.indexes() ) {
			IndexStorage<?, V> is = new IndexStorage<>( storage, i );
			
			indexStores.put( i.name(), is );
		}
	}

	void connect( Environment env ) {

		DatabaseConfig cfg = new DatabaseConfig();
		cfg.setAllowCreate( true );
		cfg.setTransactional( true );
		
		this.db = env.openDatabase( Transactions.require(), table.name(), cfg );
		
		indexStores.values().forEach( i -> i.connect( env, db ) );
	}
	
	public V get( K k ) {
		DatabaseEntry key = serializeKey( k );
		DatabaseEntry value = new DatabaseEntry();
		
		if ( db.get( Transactions.require(), key, value, LockMode.DEFAULT ) != OperationStatus.SUCCESS ) {
			return null;
		}
		
		return table.valueConverter().deserialize( value.getData() );
	}
	
	public boolean insert( V o ) {
		return db.putNoOverwrite( Transactions.require(), keyEntry( o ), serializeValue( o ) ) == OperationStatus.SUCCESS;
	}
	
	public boolean update( V o ) {
		if ( get( key( o ) ) == null ) {
			return false;
		}
		return store( o );
	}
	
	public boolean store( V o ) {
		return db.put( Transactions.require(), keyEntry( o ), serializeValue( o ) ) == OperationStatus.SUCCESS;
	}
	
	public boolean delete( V o ) {
		return db.delete( Transactions.require(), keyEntry( o ) ) == OperationStatus.SUCCESS;
	}
	
	public boolean deleteByKey( K k ) {
		return db.delete( Transactions.require(), serializeKey( k ) ) == OperationStatus.SUCCESS;
	}
	
	public void forEachRecord( Consumer<V> c ) {
		DatabaseEntry key = new DatabaseEntry();
        DatabaseEntry data = new DatabaseEntry();
		
		try ( Cursor cursor =  this.db.openCursor( Transactions.require(), null ) ) {
			while( cursor.getNext( key, data, LockMode.DEFAULT ) == OperationStatus.SUCCESS ) {
				V obj = table.valueConverter().deserialize( data.getData() );
				
				c.accept( obj );
			}
		}
	}
	
	public <I> V uniqueIndexGet( String idx, I key ) {
		return index( idx ).find( key );
	}
	
	public <I> List<V> nonUniqueIndexGet( String idx, I key ) {
		return index( idx ).list( key );
	}
	
	void close() {
		
		indexStores.values().forEach( IndexStorage::close );
		
		if ( db != null ) {
			JDataStorage.silently( db::close );
		}
	}
	
	private DatabaseEntry keyEntry( V o ) {
		return serializeKey( key( o ) );
	}
	
	private K key( V o ) {
		return table.keyProvider().apply( o );
	}
	
	private DatabaseEntry serializeKey( K key ) {
		return new DatabaseEntry( table.keyConverter().serialize( key ) );
	}
	
	private DatabaseEntry serializeValue( V v ) {
		return new DatabaseEntry( table.valueConverter().serialize( v ) );
	}
	
	@SuppressWarnings("unchecked")
	private <I> IndexStorage<I, V> index( String name ) {
		IndexStorage<I, V> s = (IndexStorage<I, V>) indexStores.get( name );
		if ( s == null  ) {
			throw new IllegalArgumentException( "No such index: " + name );
		}
		return s;
	}
	
	public JDSTable<K, V> table() {
		return table;
	}
	
	Database db() {
		return db;
	}
	
}
