package com.bo.keysandvalues.job;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

import com.bo.mocks.MockErrorListener;
import com.bo.utils.TestUtils;

import org.junit.Before;
import org.junit.Test;

public class TransactionExtractorTest
{
    private TransactionExtractor transaction;
    private MockErrorListener errorListener;

    private static Object simpleAggregate(List<String> values)
    {
        return values.get(0);
    }

    @Before
    public void setUp()
    {
        errorListener = new MockErrorListener();
        transaction = new TransactionExtractor(TransactionExtractorTest::simpleAggregate, errorListener);
        transaction.addAtomicGroup(Arrays.asList("441", "442", "500"));
    }

    @Test
    public void testExtractTransactions()
    {
        test(new String[]{"a", "a", "441", "1", "b", "b", "442", "2", "500", "3", "c", "c"}, 
             Arrays.asList(new Object[]{"a", "a", "b", "b", "c", "c"},
                           new Object[]{"441", "1", "442", "2", "500", "3"}));
        test(new String[]{"a", "a", "441", "1", "442", "2", "500", "3", "b", "b", "441", "11", "500", "31", "442", "21"}, 
             Arrays.asList(new Object[]{"a", "a", "b", "b"},
                           new Object[]{"441", "1", "442", "2", "500", "3"},
                           new Object[]{"441", "11", "442", "21", "500", "31"}));
    }

    @Test
    public void testExtractTransactionsMutiGroups()
    {
        transaction.addAtomicGroup(Arrays.asList("a", "b", "c"));
        test(new String[]{"a", "a", "b", "b", "c", "c", "d", "d", "441", "1", "442", "2", "500", "3"}, 
             Arrays.asList(new Object[]{"d", "d"},
                           new Object[]{"a", "a", "b", "b", "c", "c"},
                           new Object[]{"441", "1", "442", "2", "500", "3"}));
    }

    @Test
    public void testExtractTransactionsError()
    {
        List<Object[]> expected = new ArrayList<>();
        expected.add(new Object[]{"a", "a", "b", "b", "c", "c"});
        test(new String[]{"a", "a", "441", "1", "b", "b", "442", "2", "c", "c"}, expected);
        assertEquals("atomic group(441,442,500) missing 500", errorListener.getMessages().get(0));

        expected.add(new Object[]{"441", "1", "442", "2", "500", "3"});
        test(new String[]{"a", "a", "441", "1", "b", "b", "442", "2", "500", "3", "500", "3","c", "c"}, expected);
        assertEquals("atomic group(441,442,500) missing 441,442", errorListener.getMessages().get(1));

        expected.remove(1);
        test(new String[]{"a", "a", "441", "1", "b", "b", "442", "2", "441", "11", "500", "3","c", "c"}, expected);
        assertEquals("keys within the same group cannot overlap", errorListener.getMessages().get(2));
    
        transaction.addAtomicGroup(Arrays.asList("a", "b", "c"));
        expected.clear();
        test(new String[]{"a", "a", "441", "1", "442", "2", "500", "3", "b", "b", "441", "11", "500", "31", "442", "21"}, 
             expected);
        assertEquals("Cannot mix two atomic groups", errorListener.getMessages().get(3));
    }

    private void test(String[] data, List<Object[]> expected)
    {
        List<Entry<String, String>> inputs = TestUtils.createEntries(data);
        List<Job> transactions = transaction.extractJobs(inputs);

        int size = expected.size();
        assertEquals(size, transactions.size());
        for (int i = 0; i < size; i++) 
        {
            List<Entry<String, Object>> t = TestUtils.createEntries(expected.get(i));
            assertArrayEquals(t.toArray(), transactions.get(i).getData().toArray());
        }
    }
}