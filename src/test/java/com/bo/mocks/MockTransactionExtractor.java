package com.bo.mocks;

import java.util.List;
import java.util.Map.Entry;

import com.bo.keysandvalues.transaction.TransactionExtractor;

public class MockTransactionExtractor implements TransactionExtractor 
{
    private List<List<Entry<String, Object>>> transactions;

    @Override
    public List<List<Entry<String, Object>>> extractTransactions(List<Entry<String, String>> kvPairs) 
    {
		return getTransactions();
	}

    /**
     * @return the transactions
     */
    public List<List<Entry<String, Object>>> getTransactions() {
        return transactions;
    }

    /**
     * @param transactions the transactions to set
     */
    public void setTransactions(List<List<Entry<String, Object>>> transactions) {
        this.transactions = transactions;
    }

}