package com.bo.keysandvalues.dataprocessing;

import java.util.List;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class OrderedLineFormater implements Formater 
{

    @Override
    public String format(Collection<Entry<String, Object>> kvPairs) 
    {
        List<String> lines = kvPairs.stream()
               .sorted(Comparator.comparing(Entry<String, Object>::getKey, String.CASE_INSENSITIVE_ORDER))
               .map(p -> String.format("%s=%s", p.getKey(), p.getValue()))
               .collect(Collectors.toList());
		return String.join(System.lineSeparator(), lines);
	}

}