package com.bo.keysandvalues.dataprocessing;

import java.util.Collection;
import java.util.Map.Entry;

import com.bo.utils.TestUtils;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class OrderedLineFormaterTest
{
    @Test
    public void TestFormat()
    {
        Collection<Entry<String, Object>> kvPairs = TestUtils.createEntries(new Object[] {
            "a", "a", "c", 3, "B", "b"
        });
        Formatter formater = new OrderedLineFormatter();
        String expected = String.join(System.lineSeparator(), new String[]{ "a=a", "B=b", "c=3" });
        assertEquals(expected, formater.format(kvPairs));
    }
}