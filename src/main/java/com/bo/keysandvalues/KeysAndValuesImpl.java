package com.bo.keysandvalues;

import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.AbstractMap.SimpleEntry;

import java.util.Collections;
import java.util.HashMap;

import com.bo.context.Context;
import com.bo.keysandvalues.dataprocessing.Formater;
import com.bo.keysandvalues.dataprocessing.Parser;
import com.bo.keysandvalues.job.Job;
import com.bo.keysandvalues.job.JobExtractor;
import com.bo.keysandvalues.job.JobUtils;

public class KeysAndValuesImpl implements KeysAndValues
{
    private final Parser parser;
    private final Formater formater;
    private final ErrorListener errorListener;
    private final JobExtractor jobExtractor;
    private final Map<String, Object> map;
    private final BiFunction<Object, Object, Object> aggregator;

    public KeysAndValuesImpl(Context context)
    {
        this(context.Resolve(Parser.class), context.Resolve(Formater.class), 
             context.Resolve(JobExtractor.class), context.Resolve(ErrorListener.class), JobUtils::aggregate);
    }

    public KeysAndValuesImpl(Parser parser, Formater formater, 
                             JobExtractor jobExtractor, ErrorListener listener,
                             BiFunction<Object, Object, Object> aggregator)
    {
        this.parser = parser;
        this.formater = formater;
        this.errorListener = listener;
        this.jobExtractor = jobExtractor;
        this.aggregator = aggregator;
        this.map = new HashMap<>();
    }

    @Override
    public synchronized void accept(String kvPairs) 
    {
        try 
        {
            List<Entry<String, String>> pairs = this.parser.parse(kvPairs);
            List<Job> jobs = this.jobExtractor.extractJobs(pairs);
            for (Job job : jobs) 
            {
                if (job.isTrasaction())
                {
                    acceptTransaction(job);
                }
                else
                {
                    acceptJob(job);
                }
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

    private void acceptJob(Job job)
    {
        try 
        {
            runJob(job, map, (k, v) -> {});
        } catch (Exception e) {
            errorListener.onError("Excuting job error", e);
        }
    }

    private void acceptTransaction(Job job)
    {
        Stack<Entry<String, Object>> checkPoints = new Stack<>();
        try 
        {
            checkPoints.setSize(job.getData().size());
            checkPoints.clear();
            runJob(job, map, (k, v) -> checkPoints.add(new SimpleEntry<>(k, v)));
        } 
        catch (Exception e) 
        {
            errorListener.onError("Excuting trasaction error", e);
            while (!checkPoints.empty()) 
            {
                Entry<String, Object> checkPoint = checkPoints.pop();
                Object backup = checkPoint.getValue();
                if (backup == null) {
                    map.remove(checkPoint.getKey());
                } else
                {
                    map.put(checkPoint.getKey(), backup);
                }
            }
            errorListener.onError("Trasaction rolled back");
        }
    }

    private void runJob(Job job, Map<String, Object> map, BiConsumer<String, Object> onCheckPoint) 
    {
        List<Entry<String, Object>> data = job.getData();
		for (Entry<String, Object> p : data)
		{
		    String key = p.getKey();
		    Object value = p.getValue();
		    Object backup = null;
		    if (map.containsKey(key))
		    {
		        backup = map.get(key);
		        value = aggregator.apply(backup, p.getValue());
            }
            map.put(key, value);
            onCheckPoint.accept(key, backup);
        }
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