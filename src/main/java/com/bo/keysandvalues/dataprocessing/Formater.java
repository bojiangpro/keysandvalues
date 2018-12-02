package com.bo.keysandvalues.dataprocessing;

import java.util.Collection;
import java.util.Map;

public interface Formater
{
    String format(Collection<Map.Entry<String, Object>> kvPairs);
}