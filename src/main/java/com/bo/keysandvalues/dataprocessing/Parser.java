package com.bo.keysandvalues.dataprocessing;

import java.util.List;
import java.util.Map.Entry;

public interface Parser
{
    /**
     * Parse key-value pairs string into key-value pairs list
     * @param input string contains key value pairs
     * @return key-value pairs list
     * @throws IllegalArgumentException exception for invalid input
     */
    List<Entry<String, String>> parse(String input) throws IllegalArgumentException;
}