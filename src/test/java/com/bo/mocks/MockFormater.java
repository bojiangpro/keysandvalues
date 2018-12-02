package com.bo.mocks;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.bo.keysandvalues.dataprocessing.Formater;

public class MockFormater implements Formater {

    private Map<String, Object> map;
    private Function<Collection<Entry<String, Object>>, String> formater;

    @Override
	public String format(Collection<Entry<String, Object>> kvPairs) {
        this.map = kvPairs.stream()
        .collect(Collectors.toMap(Entry<String, Object>::getKey, Entry<String, Object>::getValue));
        if (formater == null)
        {
            return null;
        }
        return formater.apply(kvPairs);
	}

    public void setFormater(Function<Collection<Entry<String, Object>>, String> formater)
    {
        this.formater = formater;
    }
    /**
     * @return the map
     */
    public Map<String, Object> getMap() {
        return map;
    }
}