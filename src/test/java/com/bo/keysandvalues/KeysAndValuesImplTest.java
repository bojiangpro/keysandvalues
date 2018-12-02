package com.bo.keysandvalues;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.bo.context.Context;
import com.bo.context.ContextImpl;
import com.bo.keysandvalues.dataprocessing.Formater;
import com.bo.keysandvalues.dataprocessing.Parser;
import com.bo.keysandvalues.transaction.TransactionExtractor;
import com.bo.mocks.MockErrorListener;
import com.bo.mocks.MockFormater;
import com.bo.mocks.MockParser;
import com.bo.mocks.MockTransactionExtractor;
import com.bo.utils.TestUtils;

import org.junit.Before;
import org.junit.Test;

public class KeysAndValuesImplTest
{
    private KeysAndValues keysAndValues;
    private MockErrorListener errorListener;
    private MockParser parser;
    private MockFormater formater;
    private MockTransactionExtractor transactionExtractor;

    @Before
    public void setUp() throws Exception 
    {
        Context context = new ContextImpl();
        errorListener = new MockErrorListener();
        parser = new MockParser();
        formater = new MockFormater();
        transactionExtractor = new MockTransactionExtractor();
        context.Register(ErrorListener.class, errorListener);
        context.Register(Parser.class, parser);
        context.Register(Formater.class, formater);
        context.Register(TransactionExtractor.class, transactionExtractor);
        this.keysAndValues = new KeysAndValuesImpl(context);
    }

    private static List<List<Entry<String, Object>>> CreateTransactions(List<Object[]> data)
    {
        if (data == null) return null;
        List<List<Entry<String, Object>>> transactions = new ArrayList<List<Entry<String, Object>>>();
        for (Object[] transaction : data)
        {
            List<Entry<String, Object>> t = TestUtils.createEntries(transaction);
            transactions.add(t);
        }
        return transactions;
    }

    private Map<String, Object> run(List<Object[]> data)
    {
        List<List<Entry<String, Object>>> transactions = CreateTransactions(data);
        transactionExtractor.setTransactions(transactions);
        keysAndValues.accept("");
        keysAndValues.display();
        return formater.getMap();
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
        assertEquals(2, map.get("1"));
        assertEquals(1, map.get("b"));
        assertEquals("1", map.get("2"));
    }

    @Test
    public void testAcceptError()
    {
        parser.setParser(KeysAndValuesImplTest::parseError);
        Map<String, Object> map = run(null);
        assertTrue(map.isEmpty());
        List<String> messages = errorListener.getMessages();
        assertEquals("Input error", messages.get(0));
        assertEquals(IllegalArgumentException.class, errorListener.getErrors().get(0).getClass());
    }

    private static List<Map.Entry<String, String>> parseError(String input)
    {
        throw new IllegalArgumentException();
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

        formater.setFormater(KeysAndValuesImplTest::formatError);
        run(data);

        List<String> messages = errorListener.getMessages();
        assertEquals("Display error", messages.get(0));
        assertEquals(UnsupportedOperationException.class, errorListener.getErrors().get(0).getClass());
    }
}