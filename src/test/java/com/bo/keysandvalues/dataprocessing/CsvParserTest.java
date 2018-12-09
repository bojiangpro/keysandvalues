package com.bo.keysandvalues.dataprocessing;

import java.util.List;
import java.util.Map.Entry;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class CsvParserTest
{
    private Parser parser;
    @Before
    public void setUp()
    {
        parser = new CsvParser();
    }

    @Test
    public void testParse()
    {
        test("a=b  ", new String[]{"a", "b"});
        test("14=15, 14=7,A=B52, 14 = 4, dry = Don't Repeat Yourself", 
             new String[]{"14", "15", "14", "7", "A", "B52", "14", "4", "dry", "Don't Repeat Yourself"});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseError()
    {
        test("1!4=15, 14=7,A=B52, 14 = 4, dry = Don't Repeat Yourself", new String[0]);
    }

    private void test(String input, String[] expected)
    {
        List<Entry<String, String>> pairs = parser.parse(input);

        assertEquals(expected.length/2, pairs.size());
        int i = 0;
        for (Entry<String, String> p : pairs) 
        {
            assertEquals(p.getKey(), expected[i]);
            assertEquals(p.getValue(), expected[i+1]);
            i += 2;
        }
    }
}