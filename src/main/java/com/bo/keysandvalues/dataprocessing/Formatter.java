package com.bo.keysandvalues.dataprocessing;

import java.util.Collection;
import java.util.Map.Entry;

public interface Formatter
{
    /**
     * Format key-value pairs into string.
     * @param kvPairs key value pairs
     * @return formatted string representation
     */
    String format(Collection<Entry<String, Object>> kvPairs);
}