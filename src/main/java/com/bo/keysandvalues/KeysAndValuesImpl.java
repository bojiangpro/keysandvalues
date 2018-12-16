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
        // use resolve type to get newly created storage instance
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
            List<Entry<String, String>> pairs = parser.parse(kvPairs);
            List<Job> jobs = jobExtractor.extractJobs(pairs);
            Snapshot currentSnapshot = snapshots.isEmpty() ? null : snapshots.peek();
            Snapshot checkPoint = currentSnapshot;
            for (Job job : jobs) 
            {
                Snapshot s = acceptJob(job, checkPoint);
                if (s != null) {
                    checkPoint = s;
                }
            }
            // record snapshot of last succeeded job result
            if (checkPoint != currentSnapshot) {
                snapshots.push(checkPoint);
            }
        } 
        catch (IllegalArgumentException e) 
        {
            errorListener.onError("Input error", e);
        } 
        catch (Exception e)
        {
            errorListener.onError("Error", e);
        }
    }

    private Snapshot acceptJob(Job job, Snapshot checkPoint)
    {
        try
        {
            for (Entry<String, Object> p : job.getData())
            {
                storage.put(p.getKey(), p.getValue());
            }
        } catch (Exception e) {
            if (job.isTransaction())
            {
                errorListener.onError("Executing transaction error", e);
                withdrawChanges(checkPoint);
                errorListener.onError("Transaction rolled back");
                return null;
            } else {
                errorListener.onError("Executing job error", e);
            }
        }
        if (storage.isDirty()) {
            Snapshot snapshot = storage.createSnapshot();
            storage.initialize(snapshot);
            return snapshot;
        }
        return null;
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
            return formatter.format(Collections.unmodifiableCollection(snapshot.entrySet()));
        } 
        catch (Exception e) 
        {
            errorListener.onError("Display error", e);
            return null;
        }
    }

    @Override
    public synchronized void undo() {
        if (snapshots.isEmpty()) return;
        snapshots.pop();
        withdrawChanges(snapshots.isEmpty() ? null : snapshots.peek());
    }

    private void withdrawChanges(Snapshot checkPoint) {
        if (checkPoint == null){
            storage.initialize();
        } else {
            storage.initialize(checkPoint);
        }
    }

}