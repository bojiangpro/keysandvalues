package com.bo.keysandvalues;

import com.bo.keysandvalues.job.JobUtils;
import com.bo.keysandvalues.storage.Storage;
import com.bo.keysandvalues.storage.TrieStorage;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import java.util.Collections;
import java.util.List;
import java.util.Arrays;

import com.bo.context.Context;
import com.bo.context.ContextImpl;
import com.bo.keysandvalues.dataprocessing.CsvParser;
import com.bo.keysandvalues.dataprocessing.Formatter;
import com.bo.keysandvalues.dataprocessing.OrderedLineFormatter;
import com.bo.keysandvalues.dataprocessing.Parser;
import com.bo.keysandvalues.job.JobExtractor;
import com.bo.keysandvalues.job.TransactionExtractor;
import com.bo.mocks.MockErrorListener;

public class KeysAndValuesIntegrationTest
{
    private KeysAndValues keysAndValues;
    private MockErrorListener errorListener;

    @Before
    public void setUp() {
        Context context = new ContextImpl();
        errorListener = new MockErrorListener();
        context.Register(ErrorListener.class, errorListener);
        context.Register(Parser.class, new CsvParser());
        context.Register(Formatter.class, new OrderedLineFormatter());
        TransactionExtractor transaction = new TransactionExtractor(context);
        transaction.addAtomicGroup(Arrays.asList("441", "442", "500"));
        transaction.addAtomicGroup(Arrays.asList("a", "b", "c"));
        context.Register(JobExtractor.class, transaction);
        context.RegisterType(Storage.class, () -> new TrieStorage(JobUtils::aggregate));
        keysAndValues = new KeysAndValuesImpl(context);
    }

    private void test(String input, List<String> expected)
    {
        keysAndValues.accept(input);
        verify(expected);
    }

    private void verify(List<String> expected) {
        String text = keysAndValues.display();
        assertEquals(String.join(System.lineSeparator(), expected), text);
    }

    @Test
    public void testNormalAccept()
    {
        test("", Collections.singletonList(""));
        test("pi=314159,hello=world", Arrays.asList("hello=world", "pi=314159"));
    }

    @Test
    public void testRepeatInputAccept()
    {
        test("14=15, 14=7,A=B52, dry=D.R.Y., 14 = 4, dry = Don't Repeat Yourself", 
            Arrays.asList("14=26","A=B52", "dry=Don't Repeat Yourself"));

        test("  14 = -26 ,  dry =  A  ", Arrays.asList("14=0","A=B52", "dry=A"));
    }

    @Test
    public void testDisplay()
    {
        test("one=two", Collections.singletonList("one=two"));
        test("Three=four", Arrays.asList("one=two", "Three=four"));
        test("5=6", Arrays.asList("5=6", "one=two", "Three=four"));
        test("14=x", Arrays.asList("14=x","5=6", "one=two", "Three=four"));
    }

    @Test
    public void testSingleAtomicGroup()
    {
        test("441=one,X=Y, 442=2,500=three", Arrays.asList("441=one", "442=2", "500=three", "X=Y"));
    }

    @Test
    public void testAtomicGroupTwice()
    {
        test("18=zzz,441=one,500=three,442=2,442= A,441 =3,35=D,500=ok  ", 
            Arrays.asList("18=zzz", "35=D", "441=3", "442=A", "500=ok"));
        test("18=zzz,441=one,500=ok,442= A,a=a, b=b,c=c  ", 
            Arrays.asList("18=zzz", "35=D", "441=one", "442=A", "500=ok", "a=a", "b=b", "c=c"));
    }

    @Test
    public void testIncompleteGroup()
    {
        test("441=3,500=not ok,13=qwerty", Collections.singletonList("13=qwerty"));
        assertEquals("atomic group(441,442,500) missing 442", errorListener.getMessages().get(0));
    }

    @Test
    public void testIncompleteSecondGroup()
    {
        test("500= three , 6 = 7 ,441= one,442=1,442=4", 
                Arrays.asList("441=one", "442=1","500=three","6=7"));
        assertEquals("atomic group(441,442,500) missing 441,500", errorListener.getMessages().get(0));
        test("500= three , 6 = 0 ,441= one,442=0,a=4", 
                Arrays.asList("441=one", "442=1","500=three","6=7"));
        assertEquals("atomic group(a,b,c) missing b,c", errorListener.getMessages().get(1));
    }

    @Test
    public void testGroupOverlap()
    {
        test("441=3,500=not ok,441=1, 13=qwerty", Collections.singletonList("13=qwerty"));
        assertEquals("keys within the same group cannot overlap", errorListener.getMessages().get(0));
    }

    @Test
    public void testMixGroups()
    {
        test("441=3,500=not ok, a=qwerty, 442=2", Collections.singletonList(""));
        assertEquals("Cannot mix two atomic groups", errorListener.getMessages().get(0));
    }

    @Test
    public void testUndo() {
        keysAndValues.undo();
        assertEquals("", keysAndValues.display());
        keysAndValues.accept("pi=314159,hello=world");
        keysAndValues.undo();
        assertEquals("", keysAndValues.display());
        keysAndValues.accept("one=two");
        keysAndValues.accept("Three=four");
        keysAndValues.accept("5=6");
        keysAndValues.accept("5=1");
        keysAndValues.accept("14=x");
        verify(Arrays.asList("14=x","5=7", "one=two", "Three=four"));
        keysAndValues.undo();
        verify(Arrays.asList("5=7", "one=two", "Three=four"));
        keysAndValues.undo();
        verify(Arrays.asList("5=6", "one=two", "Three=four"));
        keysAndValues.undo();
        verify(Arrays.asList("one=two", "Three=four"));
        keysAndValues.undo();
        verify(Collections.singletonList("one=two"));
        keysAndValues.undo();
        assertEquals("", keysAndValues.display());
        keysAndValues.undo();
        assertEquals("", keysAndValues.display());

        // test undo to a partial succeeded submission
        keysAndValues.accept("500= three , 6 = 7 ,441= one,442=1,442=4");
        keysAndValues.accept("500= three , 6 = 0 ,441= one,442=0,a=4");
        keysAndValues.undo();
        verify(Arrays.asList("441=one", "442=1","500=three","6=7"));
    }
}