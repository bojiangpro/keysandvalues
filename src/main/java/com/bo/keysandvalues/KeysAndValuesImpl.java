package com.bo.keysandvalues;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import java.util.Collections;
import java.util.HashMap;

import com.bo.context.Context;
import com.bo.keysandvalues.dataprocessing.Formater;
import com.bo.keysandvalues.dataprocessing.Parser;
import com.bo.keysandvalues.transaction.TransactionExtractor;

public class KeysAndValuesImpl implements KeysAndValues
{
    private final Parser parser;
    private final Formater formater;
    private final ErrorListener errorListener;
    private final TransactionExtractor transactionExtractor;
    private final Map<String, Object> map;

    public KeysAndValuesImpl(Context context)
    {
        this(context.Resolve(Parser.class), context.Resolve(Formater.class), 
             context.Resolve(TransactionExtractor.class), context.Resolve(ErrorListener.class));
    }

    public KeysAndValuesImpl(Parser parser, Formater formater, 
                             TransactionExtractor transactionExtractor, ErrorListener listener)
    {
        this.parser = parser;
        this.formater = formater;
        this.errorListener = listener;
        this.transactionExtractor = transactionExtractor;
        this.map = new HashMap<>();
    }

    @Override
    public void accept(String kvPairs) 
    {
        try 
        {
            List<Entry<String, String>> pairs = this.parser.parse(kvPairs);
            List<List<Entry<String, Object>>> transactions = this.transactionExtractor.extractTransactions(pairs);
            for (List<Entry<String, Object>> transaction : transactions) 
            {
                accept(transaction, this.map, this.errorListener);
            }
        } 
        catch (IllegalArgumentException e) 
        {
            this.errorListener.onError("Input error", e);
        } 
        catch (Exception e)
        {
            this.errorListener.onError("Error", e);
        }
    }

    private static void accept(List<Entry<String, Object>> transaction, Map<String, Object> map, ErrorListener errorListener)
    {
        if (transaction.stream().anyMatch(p -> !aggregate(map, p, errorListener)))
        {
            errorListener.onError("Transaction failed!");
            return;
        }
        for (Entry<String, Object> e : transaction)
        {
            map.put(e.getKey(), e.getValue());
        }
    }

    private static boolean aggregate(Map<String, Object> map, Entry<String, Object> pair, ErrorListener errorListener)
    {
        String key = pair.getKey();
        if (map.containsKey(key))
        {
            Object old = map.get(key);
            Object newObj = pair.getValue();
            if (newObj instanceof Integer && old instanceof Integer)
            {
                newObj = (Integer) old + (Integer) newObj;
                pair.setValue(newObj);
            }
        }
        return true;
    }

    @Override
    public String display() 
    {
        try 
        {
            return this.formater.format(Collections.unmodifiableCollection(this.map.entrySet())); 
        } 
        catch (Exception e) 
        {
            this.errorListener.onError("Display error", e);
            return null;
        }
    }

}