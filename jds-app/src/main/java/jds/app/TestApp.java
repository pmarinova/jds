package jds.app;

import jds.JDSTable;
import jds.JDataStorage;

public class TestApp {

	public static void main(String[] args) {
		JDataStorage storage = storage() ;
		
		try {
			storage.connect();
			
			try {
				storage.transactional( () -> store( storage ) );
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			
			storage.transactional( () -> print( storage ) );
		}
		finally {
			storage.close();
		}
	}
	
	private static void store( JDataStorage s ) {
		
		s.insert( new User( "u1", "user 1" ) );
		
		s.store( new UserActivity( "u1" , "login" ) );
		s.store( new UserActivity( "u1" , "logout" ) );
		
		if ( true ) {
			throw new RuntimeException();
		}
	}
	
	private static void print( JDataStorage s ) {
		s.forEachRecord( User.class, System.out::println );
		s.forEachRecord( UserActivity.class, System.out::println );		
	}
	
	private static JDataStorage storage() {
		return 
			new JDataStorage()
				.homeFolder( "target/db" )
				.with( 
					new JDSTable<String, User>( "users" )
						.keyType( String.class )
						.valueType( User.class )
						.keyProvider( User::getId )
				)
				.with( 
						new JDSTable<String, UserActivity>( "user_activities" )
							.keyType( String.class )
							.valueType( UserActivity.class )
							.keyProvider( UserActivity::getId )
					)
		;
	}
}
