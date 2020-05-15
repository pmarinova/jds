package jds;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.Environment;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.SecondaryConfig;
import com.sleepycat.je.SecondaryDatabase;
import com.sleepycat.je.SecondaryKeyCreator;

class IndexStorage<K, V> {

	private final JDataStorage storage;
	
	private final JDSIndex<K, V> index;

	private SecondaryDatabase db;
	
	public IndexStorage( JDataStorage storage, JDSIndex<K, V> index ) {
		this.storage = storage;
		this.index = index;
	}
	
	void connect( Environment env, Database primary ) {
		SecondaryConfig cfg = new SecondaryConfig();
		cfg.setAllowCreate( true );
		cfg.setAllowPopulate( true );
		cfg.setTransactional( true );
		cfg.setSortedDuplicates( !index.unique() );
		cfg.setKeyCreator( new IndexKeyCreator<>( storage, index ) );
		
		String name = primary.getDatabaseName() + "-" + index.name();
		
		this.db = env.openSecondaryDatabase( Transactions.require(), name, primary, cfg );
	}

	public V find( K k ) {
		
		DatabaseEntry key = new DatabaseEntry( storage.serialize( k ) );
		DatabaseEntry data = new DatabaseEntry();
		
		if ( db.get( Transactions.require(), key, data, LockMode.DEFAULT ) != OperationStatus.SUCCESS ) {
			return null;
		}
		
		return storage.deserialize( data.getData(), index.valueType() );
	}
	
	public List<V> list( K k ) {
		List<V> r = new ArrayList<>();
		forEachRecord( k, r::add );
		return r;
	}
	
	public void forEachRecord( K k, Consumer<V> c ) {
		DatabaseEntry key = new DatabaseEntry( storage.serialize( k ) );
        DatabaseEntry data = new DatabaseEntry();
		
		try ( Cursor cursor =  this.db.openCursor( Transactions.require(), null ) ) {
			
			if ( cursor.getSearchKey( key, data, LockMode.DEFAULT ) != OperationStatus.SUCCESS ) {
				return ;
			}
			
			do {
				V obj = storage.deserialize( data.getData(), index.valueType() );
				
				c.accept( obj );
			}
			while( cursor.getNextDup( key, data, LockMode.DEFAULT ) == OperationStatus.SUCCESS );
		}
	}
	
	void close() {
		if ( db != null ) {
			JDataStorage.silently( db::close );
		}
	}
	
	public String name() {
		return index.name();
	}
	
	private static class IndexKeyCreator<K, V> implements SecondaryKeyCreator {

		private final JDataStorage storage;
		
		private final JDSIndex<K, V> index;
		
		public IndexKeyCreator(JDataStorage storage, JDSIndex<K, V> index) {
			this.storage = storage;
			this.index = index;
		}

		@Override
		public boolean createSecondaryKey(
				SecondaryDatabase secondary, 
				DatabaseEntry key, 
				DatabaseEntry data,
				DatabaseEntry result
				) 
		{
			V val = storage.deserialize( data.getData(), index.valueType() );
			
			K indexKey = index.indexer().apply( val );
			
			if ( indexKey == null ) {
				return false;
			}
			
			result.setData( storage.serialize( indexKey ) );
			return true;
		}
	}
}
