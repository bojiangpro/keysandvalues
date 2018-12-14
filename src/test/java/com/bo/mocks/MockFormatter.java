package com.bo.mocks;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.bo.keysandvalues.dataprocessing.Formatter;

public class MockFormatter implements Formatter {

    private Map<String, Object> map;
    private Function<Collection<Entry<String, Object>>, String> formatter;

    @Override
	public String format(Collection<Entry<String, Object>> kvPairs) {
        this.map = kvPairs.stream()
        .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
        if (formatter == null)
        {
            return null;
        }
        return formatter.apply(kvPairs);
	}

    public void setFormatter(Function<Collection<Entry<String, Object>>, String> formatter)
    {
        this.formatter = formatter;
    }
    /**
     * @return the map
     */
    public Map<String, Object> getMap() {
        return map;
    }
}