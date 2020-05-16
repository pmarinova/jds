package jds;

import com.sleepycat.je.Transaction;

public class JDSTransaction {

	private final Transaction tx;

	JDSTransaction( Transaction tx ) {
		this.tx = tx;
	}

	void setup() {
		Transactions.set( this );
	}

	public boolean isValid() {
		return tx.isValid();
	}
	
	public void abort() {
		Transactions.clear();
		tx.abort();
	}

	public void commit() {
		Transactions.clear();
		tx.commit();
	}
	
	Transaction transaction() {
		return tx;
	}
}
