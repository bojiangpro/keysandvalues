package com.bo.keysandvalues.dataprocessing;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.AbstractMap.SimpleEntry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * parse comma separated, key-value pairs
 * with only alphanumeric keys are allowed. Trim all leading & trailing whitespace.
 */
public class CsvParser implements Parser 
{
    private final Pattern pattern;

    public CsvParser()
    {
      pattern = Pattern.compile("^\\s*([a-zA-Z0-9]+)\\s*=(.+)$");
    }

    @Override
    public List<Entry<String, String>> parse(String input) throws IllegalArgumentException 
    {
      String[] segments = input.split(",", 0);
      List<Entry<String, String>> pairs = new ArrayList<>();
      for (String s : segments) 
      {
        Matcher matcher = pattern.matcher(s);
        if (!matcher.matches())
        {
          throw new IllegalArgumentException("Invalid format: " + s);
        }
        pairs.add(new SimpleEntry<>(matcher.group(1), matcher.group(2).trim()));
      }
		  return pairs;
    }

}