package com.bo.keysandvalues;

import com.bo.context.Context;
import com.bo.keysandvalues.dataprocessing.Formatter;
import com.bo.keysandvalues.dataprocessing.Parser;
import com.bo.keysandvalues.job.Job;
import com.bo.keysandvalues.job.JobExtractor;
import com.bo.keysandvalues.storage.Snapshot;
import com.bo.keysandvalues.storage.Storage;

import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Stack;

public class KeysAndValuesImpl implements KeysAndValues
{
    private final Parser parser;
    private final Formatter formatter;
    private final ErrorListener errorListener;
    private final JobExtractor jobExtractor;
    private final Storage storage;
    private final Stack<Snapshot> snapshots;

    public KeysAndValuesImpl(Context context)
    {
        this(context.Resolve(Parser.class), context.Resolve(Formatter.class),
             context.Resolve(JobExtractor.class), context.Resolve(ErrorListener.class),
             context.ResolveType(Storage.class));
    }

    KeysAndValuesImpl(Parser parser, Formatter formatter, JobExtractor jobExtractor,
                      ErrorListener listener, Storage storage)
    {
        this.parser = parser;
        this.formatter = formatter;
        this.errorListener = listener;
        this.jobExtractor = jobExtractor;
        this.storage = storage;
        this.storage.initialize();
        snapshots = new Stack<>();
    }

    @Override
    public synchronized void accept(String kvPairs) 
    {
        try 
        {
            List<Entry<String, String>> pairs = this.parser.parse(kvPairs);
            List<Job> jobs = this.jobExtractor.extractJobs(pairs);
            Snapshot snapshot = null;
            for (Job job : jobs) 
            {
                Snapshot s = acceptJob(job);
                if (s != null) {
                    snapshot = s;
                }
            }
            if (snapshot != null) {
                snapshots.push(snapshot);
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

    private Snapshot acceptJob(Job job)
    {
        try
        {
            return runJob(job, storage);
        } catch (Exception e) {
            if (job.isTransaction())
            {
                errorListener.onError("Executing transaction error", e);
                errorListener.onError("Transaction rolled back");
            } else {
                errorListener.onError("Executing job error", e);
            }

            return null;
        }
    }

    private Snapshot runJob(Job job, Storage storage)
    {
        List<Entry<String, Object>> data = job.getData();
		for (Entry<String, Object> p : data)
		{
		    storage.put(p.getKey(), p.getValue());
        }

		return storage.createSnapshot();
    }

    @Override
    public String display() 
    {
        try 
        {
            if (snapshots.isEmpty()) {
                return "";
            }
            Snapshot snapshot = snapshots.peek();
            return this.formatter.format(Collections.unmodifiableCollection(snapshot.entrySet()));
        } 
        catch (Exception e) 
        {
            this.errorListener.onError("Display error", e);
            return null;
        }
    }

    @Override
    public void undo() {
        if (snapshots.isEmpty()) return;
        snapshots.pop();
        if (snapshots.isEmpty()){
            storage.initialize();
        } else {
            storage.initialize(snapshots.peek());
        }
    }

}