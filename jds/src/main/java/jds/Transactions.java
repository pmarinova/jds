package jds;

import com.sleepycat.je.Transaction;

class Transactions {

	private static final ThreadLocal<Transaction> DATA = new ThreadLocal<>();
	
	static void set( Transaction t ) {
		DATA.set( t );
	}
	
	static void clear() {
		DATA.remove();
	}
	
	static Transaction current() {
		return DATA.get();
	}
	
	public static Transaction require() {
		
		Transaction tx = current();
		
		if ( tx == null ) {
			throw new JDSException( "No active transaction" );
		}
		
		return tx;
	}
}
