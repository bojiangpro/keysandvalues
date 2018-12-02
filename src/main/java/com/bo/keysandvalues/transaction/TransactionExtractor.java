package com.bo.keysandvalues.transaction;

import java.util.List;
import java.util.Map.Entry;

public interface TransactionExtractor
{
    /**
     * Extract transactions from key-value pairs.
     * @param kvPairs
     * @return transactions
     */
    List<List<Entry<String, Object>>> extractTransactions(List<Entry<String, String>> kvPairs);
}