package com.bo.keysandvalues.job;

import java.util.List;

public final class JobUtils
{
    public static Object aggregateInteger(List<String> values)
    {
        int size = values.size();
		if (values == null || size == 0)
        {
            return null;
        }
        Integer number = tryParseInteger(values.get(0));
        if (number == null)
        {
            return values.get(size-1);
        }
        for (int i = 1; i < size; i++) 
        {
            String value = values.get(i);
            Integer t = tryParseInteger(value);
            if (t == null)
            {
                throw new NumberFormatException(value + " is not an integer");
            }
            number += t;
        }
        return number;
    }

    private static Integer tryParseInteger(String value)
    {
        try 
        {
            return Integer.parseInt(value);
        } 
        catch (NumberFormatException e) 
        {
            return null;
        }
    }

    public static Object aggregate(Object old, Object newObj) {
        if (newObj instanceof Integer && old instanceof Integer) {
            newObj = (Integer) old + (Integer) newObj;
        }
        return newObj;
    }
}