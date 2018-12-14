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

    private void run(List<Object[]> data)
    {
        List<Job> transactions = CreateTransactions(data);
        transactionExtractor.setTransactions(transactions);
        keysAndValues.accept("");
    }

    @Test
    public void testAcceptDisplay()
    {
        List<Object[]> data = new ArrayList<>();
        data.add(new Object[]{"a", "a", "1", 1});
        data.add(new Object[]{"b", "b", "2", 1});
        
        run(data);
        verify(new Object[]{"a", "a", "1", 1, "b", "b", "2", 1});

        data = new ArrayList<>();
        data.add(new Object[]{"a", "aa", "1", 2});
        data.add(new Object[]{"b", 1, "2", "1"});
        
        run(data);
        verify(new Object[]{"a", "aa", "1", 2, "b", 1, "2", "1"});
    }

    @Test
    public void testAcceptError()
    {
        parser.setParser(KeysAndValuesImplTest::parseError);
        run(null);
        assertEquals("", keysAndValues.display());
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
        keysAndValues.display();
        List<String> messages = errorListener.getMessages();
        assertEquals("Display error", messages.get(0));
        assertEquals(UnsupportedOperationException.class, errorListener.getErrors().get(0).getClass());
    }

    @Test
    public void testAcceptJobError()
    {
        storage.setErrorKey("1");
        run(Arrays.asList(new Object[]{"a", "a"}, new Object[]{"b", "b"}, new Object[]{"1", 1}));
        keysAndValues.accept("doesn't matter");
        verify(new Object[]{"b", "b", "a", "a"});

        List<String> messages = errorListener.getMessages();
        assertEquals("Executing job error", messages.get(0));
        assertEquals(RuntimeException.class, errorListener.getErrors().get(0).getClass());
    }

    @Test
    public void testAcceptTransactionErrorRollback()
    {
        run(Arrays.asList(new Object[]{"a", "a"}, new Object[]{"1", 1}));

        transactionExtractor.setTransactions(Collections.singletonList(new Job(true,
                Arrays.asList(new SimpleEntry<>("a", "b"), new SimpleEntry<>("b", "b"), new SimpleEntry<>("1", 2)))));

        storage.setErrorKey("1");

        keysAndValues.accept("doesn't matter");
        verify(new Object[]{"1", 1, "a", "a"});
        List<String> messages = errorListener.getMessages();
        assertEquals("Executing transaction error", messages.get(0));
        assertEquals(RuntimeException.class, errorListener.getErrors().get(0).getClass());
        assertEquals("Transaction rolled back", messages.get(1));
    }

    @Test
    public void testUndo() {
        keysAndValues.undo();
        assertEquals("", keysAndValues.display());
        run(Collections.singletonList(new Object[]{"a", "a", "1", 1}));
        run(Collections.singletonList(new Object[]{"b", "b", "2", 1}));
        verify(new Object[]{"a", "a", "1", 1, "b", "b", "2", 1});

        keysAndValues.undo();
        verify(new Object[]{"a", "a", "1", 1});

        run(Collections.singletonList(new Object[]{"a", "aa", "1", 2}));
        verify(new Object[]{"a", "aa", "1", 2});

        keysAndValues.undo();
        verify(new Object[]{"a", "a", "1", 1});

        keysAndValues.undo();
        assertEquals("", keysAndValues.display());
        keysAndValues.undo();
        assertEquals("", keysAndValues.display());
    }

    private void verify(Object[] expected) {
        keysAndValues.display();
        Map<String, Object> map = formatter.getMap();
        assertEquals(expected.length/2, map.size());
        for (int i = 0; i < expected.length; i += 2) {
            assertEquals(expected[i+1], map.get(expected[i].toString()));
        }
    }
}