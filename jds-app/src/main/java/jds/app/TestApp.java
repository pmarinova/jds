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
		
		s.store( new UserActivity( "u2" , "login" ) );
		s.store( new UserActivity( "u2" , "login" ) );
	}
	
	private static void print( JDataStorage s ) {
		s.forEachRecord( User.class, System.out::println );
		s.forEachRecord( UserActivity.class, System.out::println );
		
		System.out.println( "----------------------------Index search: u1--------------------------------" );
		s.nonUniqueIndexGet( UserActivity.class, "userId", "u1" ).forEach( System.out::println ); 
		
		System.out.println( "----------------------------Index search: u2--------------------------------" );
		s.nonUniqueIndexGet( UserActivity.class, "userId", "u2" ).forEach( System.out::println );
		
		System.out.println( "----------------------------Index search: u3--------------------------------" );
		s.nonUniqueIndexGet( UserActivity.class, "userId", "u3" ).forEach( System.out::println );
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
						.nonUniqueIndex( "userId", String.class, UserActivity::getUserId )
				)
		;
	}
}
