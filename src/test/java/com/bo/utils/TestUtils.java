package com.bo.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.AbstractMap.SimpleEntry;

public final class TestUtils
{
    public static <T> List<Entry<String, T>> createEntries(T[] data)
    {
        List<Entry<String, T>> t = new ArrayList<>();
        for (int i = 0; i < data.length; i = i+2) 
        {
            t.add(new SimpleEntry<>(data[i].toString(), data[i + 1]));
        }
        return t;
    }
}