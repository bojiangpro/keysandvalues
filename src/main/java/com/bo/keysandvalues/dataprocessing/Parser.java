package com.bo.keysandvalues.dataprocessing;

import java.util.List;
import java.util.Map;

public interface Parser
{
    List<Map.Entry<String, String>> parse(String input) throws IllegalArgumentException;
}