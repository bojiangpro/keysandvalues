package com.bo.keysandvalues.transaction;

import java.util.List;
import java.util.Map;

public interface TransactionExtractor
{
    List<List<Map.Entry<String, Object>>> extractTransactions(List<Map.Entry<String, String>> kvPairs);
}