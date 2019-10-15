package gov.nasa.pds.harvest.search.util;

import java.util.UUID;

/**
 * Singleton to manage transactions.
 * 
 * @author karpenko
 */
public class TransactionManager {
	private static TransactionManager instance = new TransactionManager();

	private String transactionId;

	private TransactionManager() {
		transactionId = UUID.randomUUID().toString();
	}

	public static TransactionManager getInstance() {
		return instance;
	}
	
	public String getTransactionId() {
		return transactionId;
	}
}
