package com.bo.keysandvalues.dataprocessing;

import java.util.Collection;
import java.util.Map.Entry;

public interface Formater
{
    /**
     * Format key-value pairs into string.
     * @param kvPairs
     * @return formated string representation
     */
    String format(Collection<Entry<String, Object>> kvPairs);
}