package com.bo.keysandvalues;

import com.bo.keysandvalues.job.Job;
import com.bo.mocks.*;
import com.bo.utils.TestUtils;
import org.junit.Before;
import org.junit.Test;

import java.util.AbstractMap.SimpleEntry;
import java.util.*;
import java.util.Map.Entry;

import static org.junit.Assert.*;

public class KeysAndValuesImplTest
{
    private KeysAndValues keysAndValues;
    private MockErrorListener errorListener;
    private MockParser parser;
    private MockFormatter formatter;
    private MockTransactionExtractor transactionExtractor;
    private SimpleStorage storage;

    @Before
    public void setUp() {
        errorListener = new MockErrorListener();
        parser = new MockParser();
        formatter = new MockFormatter();
        transactionExtractor = new MockTransactionExtractor();
        storage = new SimpleStorage();
        this.keysAndValues = new KeysAndValuesImpl(parser, formatter,
                transactionExtractor, errorListener, storage);
    }

    private static List<Job> CreateTransactions(List<Object[]> data) {
        if (data == null) return null;
        List<Job> transactions = new ArrayList<>();
        for (Object[] transaction : data)
        {
            List<Entry<String, Object>> t = TestUtils.createEntries(transaction);
            transactions.add(new Job(false, t));
        }
        return transactions;
    }

    private Map<String, Object> run(List<Object[]> data)
    {
        List<Job> transactions = CreateTransactions(data);
        transactionExtractor.setTransactions(transactions);
        keysAndValues.accept("");
        keysAndValues.display();
        return formatter.getMap();
    }

    @Test
    public void testAcceptDisplay()
    {
        List<Object[]> data = new ArrayList<>();
        data.add(new Object[]{"a", "a", "1", 1});
        data.add(new Object[]{"b", "b", "2", 1});
        
        Map<String, Object> map = run(data);

        assertEquals(4, map.size());
        assertTrue(map.containsKey("a"));
        assertTrue(map.containsKey("1"));
        assertTrue(map.containsKey("b"));
        assertTrue(map.containsKey("2"));
        assertEquals("a", map.get("a"));
        assertEquals(1, map.get("1"));
        assertEquals("b", map.get("b"));
        assertEquals(1, map.get("2"));

        data = new ArrayList<>();
        data.add(new Object[]{"a", "aa", "1", 1});
        data.add(new Object[]{"b", 1, "2", "1"});
        
        map = run(data);
        assertEquals("aa", map.get("a"));
        assertEquals(1, map.get("1"));
        assertEquals(1, map.get("b"));
        assertEquals("1", map.get("2"));
    }

    @Test
    public void testAcceptError()
    {
        parser.setParser(KeysAndValuesImplTest::parseError);
        Map<String, Object> map = run(null);
        assertNull(map);
        List<String> messages = errorListener.getMessages();
        assertEquals("Input error", messages.get(0));
        assertEquals(IllegalArgumentException.class, errorListener.getErrors().get(0).getClass());
    }

    private static List<Map.Entry<String, String>> parseError(String input)
    {
        throw new IllegalArgumentException(input);
    }

    private static String formatError(Collection<Entry<String, Object>> kvPairs)
    {
        kvPairs.add(null);
        return null;
    }

    @Test
    public void testDisplayError()
    {
        List<Object[]> data = new ArrayList<>();
        data.add(new Object[]{"a", "a", "1", 1});

        formatter.setFormatter(KeysAndValuesImplTest::formatError);
        run(data);

        List<String> messages = errorListener.getMessages();
        assertEquals("Display error", messages.get(0));
        assertEquals(UnsupportedOperationException.class, errorListener.getErrors().get(0).getClass());
    }

    @Test
    public void testAcceptJobError()
    {
        transactionExtractor.setTransactions(Collections.singletonList(new Job(false, null)));
        keysAndValues.accept("doesn't matter");
        List<String> messages = errorListener.getMessages();
        assertEquals("Executing job error", messages.get(0));
        assertEquals(NullPointerException.class, errorListener.getErrors().get(0).getClass());
    }

    @Test
    public void testAcceptTransactionErrorRollback()
    {
        run(Arrays.asList(new Object[]{"a", "a"}, new Object[]{"1", 1}));

        transactionExtractor.setTransactions(Collections.singletonList(new Job(true,
                Arrays.asList(new SimpleEntry<>("a", "b"), new SimpleEntry<>("b", "b"), new SimpleEntry<>("1", 2)))));

        storage.setException(new RuntimeException());

        keysAndValues.accept("doesn't matter");
        Map<String, Object> map = formatter.getMap();
        assertEquals(2, map.size());
        assertEquals(1, map.get("1"));
        assertEquals("a", map.get("a"));
        List<String> messages = errorListener.getMessages();
        assertEquals("Executing transaction error", messages.get(0));
        assertEquals(RuntimeException.class, errorListener.getErrors().get(0).getClass());
        assertEquals("Transaction rolled back", messages.get(1));
    }
}