package jds;

import java.util.function.Consumer;

import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.Environment;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

class TableStorage<K, V> {

	private final JDSTable<K, V> table;

	private Database db;
	
	public TableStorage( JDSTable<K, V> table ) {
		this.table = table;
	}

	public void connect( Environment env ) {

		DatabaseConfig cfg = new DatabaseConfig();
		cfg.setAllowCreate( true );
		cfg.setTransactional( true );
		
		this.db = env.openDatabase( Transactions.require(), table.name(), cfg );
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
	
	public void close() {
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
	
	public JDSTable<K, V> table() {
		return table;
	}
	
	public Database db() {
		return db;
	}
	
}
