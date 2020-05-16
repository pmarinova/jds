package jds;

import com.sleepycat.je.Transaction;

class Transactions {

	private static final ThreadLocal<JDSTransaction> DATA = new ThreadLocal<>();
	
	static void set( JDSTransaction t ) {
		DATA.set( t );
	}
	
	static void clear() {
		DATA.remove();
	}
	
	static JDSTransaction currentJDSTransaction() {
		return DATA.get();
	}
	
	static Transaction current() {
		JDSTransaction c = currentJDSTransaction();
		return c != null ? c.transaction() : null;
	}
	
	public static Transaction require() {
		
		Transaction tx = current();
		
		if ( tx == null ) {
			throw new JDSException( "No active transaction" );
		}
		
		return tx;
	}
}
