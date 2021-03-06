package com.bo.keysandvalues.job;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class TransactionUtilsTest
{
    @Test
    public void testAggregateInteger()
    {
        test(Arrays.asList("1", "2", "3"), 6);
        test(Arrays.asList("aa", "2", "3"), "3");
    }

    @Test(expected = NumberFormatException.class)
    public void testAggregateIntegerError()
    {
        test(Arrays.asList("1", "2", "3", "s"), null);
    }

    private static void test(List<String> values, Object expected)
    {
        Object value = JobUtils.aggregateInteger(values);
        assertEquals(expected, value);
    }
}