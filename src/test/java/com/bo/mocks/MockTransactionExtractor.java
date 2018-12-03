package com.bo.mocks;

import java.util.List;
import java.util.Map.Entry;

import com.bo.keysandvalues.job.Job;
import com.bo.keysandvalues.job.JobExtractor;

public class MockTransactionExtractor implements JobExtractor 
{
    private List<Job> transactions;

    @Override
    public List<Job> extractJobs(List<Entry<String, String>> kvPairs) 
    {
		return getTransactions();
	}

    /**
     * @return the transactions
     */
    public List<Job> getTransactions() {
        return transactions;
    }

    /**
     * @param transactions the transactions to set
     */
    public void setTransactions(List<Job> transactions) {
        this.transactions = transactions;
    }

}