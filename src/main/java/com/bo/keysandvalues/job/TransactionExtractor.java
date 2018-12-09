package com.bo.keysandvalues.job;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.bo.context.Context;
import com.bo.keysandvalues.ErrorListener;

/**
 * Extract transactions based on given atomic groups.
 */
public class TransactionExtractor implements JobExtractor {
    private final List<Set<String>> atomicGroups;
    private final Function<List<String>, Object> aggregator;
    private final ErrorListener errorListener;

    public TransactionExtractor(Context context) {
        this(JobUtils::aggregateInteger, context.Resolve(ErrorListener.class));
    }

    /**
     * Constructor.
     * @param aggregator function to aggregate values of the same key
     * @param errorListener error listener
     */
    public TransactionExtractor(Function<List<String>, Object> aggregator, ErrorListener errorListener) {
        this.aggregator = aggregator;
        this.errorListener = errorListener;
        atomicGroups = new ArrayList<>();
    }

    public void addAtomicGroup(List<String> group)
    {
        if(group.stream().anyMatch(k -> GetGroup(k).isPresent()))
        {
            return;
        }
        Set<String> set = new HashSet<>(group);
        atomicGroups.add(set);
    }

    private Optional<Set<String>> GetGroup(String key)
    {
        return atomicGroups.stream()
                           .filter(s -> s.contains(key))
                           .findAny();
    } 

    @Override
    public List<Job> extractJobs(List<Entry<String, String>> kvPairs) 
    {
        int size = kvPairs.size();
        List<Job> transactions = new ArrayList<>();
        List<Entry<String, String>> nonAtomics = new ArrayList<>();
        List<Entry<Integer, Set<String>>> atomics = new ArrayList<>();

        for (int i = 0; i < size; i++) 
        {
            Entry<String, String> kv = kvPairs.get(i);
            Optional<Set<String>> g = GetGroup(kv.getKey());
            if (g.isPresent())
            {
                atomics.add(new SimpleEntry<>(i, g.get()));
            }
            else
            {
                nonAtomics.add(kv);
            }
        }
        if (!nonAtomics.isEmpty())
        {
            transactions.add(buildJob(false, nonAtomics, aggregator));
        }

        transactions.addAll(getTransactions(atomics, kvPairs));
        return transactions;
    }

    private List<Job> getTransactions(List<Entry<Integer, Set<String>>> atomics, List<Entry<String, String>> kvPairs)
    {
        int size = atomics.size();
        List<Job> transactions = new ArrayList<>();
        if (size == 0) return transactions;
        Set<String> keys = new HashSet<>();
        Set<String> group = atomics.get(0).getValue();
        keys.add(kvPairs.get(atomics.get(0).getKey()).getKey());
        int start = 0;
		for (int i = 1; i < size; i++) 
        {
            Entry<Integer, Set<String>> atomic = atomics.get(i);
            String key = kvPairs.get(atomic.getKey()).getKey();
            if (keys.size() == group.size())
            {
                List<Entry<String, String>> pairs =atomics.subList(start, i).stream()
                                                        .map(a -> kvPairs.get(a.getKey()))
                                                        .collect(Collectors.toList());
                transactions.add(buildJob(true, pairs, aggregator));
                start = i;
                group = atomic.getValue();
                keys.clear();
                keys.add(key);
                continue;
            }
            if (atomic.getValue() != group)
            {
                errorListener.onError("Cannot mix two atomic groups");
                return transactions;
            }
            if (keys.contains(key))
            {
                errorListener.onError("keys within the same group cannot overlap");
                return transactions;
            }
            keys.add(key);
        }

        if (keys.size() == group.size())
        {
            List<Entry<String, String>> pairs =atomics.subList(start, size).stream()
                                                    .map(a -> kvPairs.get(a.getKey()))
                                                    .collect(Collectors.toList());
            transactions.add(buildJob(true, pairs, aggregator));
        }
        else
        {
            List<String> missings = group.stream().filter(v -> !keys.contains(v)).collect(Collectors.toList());
            errorListener.onError(String.format("atomic group(%s) missing %s", 
                                String.join(",", group), String.join(",", missings)));
        }

        return transactions;
    }
    
    private static Job buildJob(boolean isTransaction, List<Entry<String, String>> kvPairs, Function<List<String>, Object> aggregator)
    {
        Map<String, List<String>> groups = kvPairs.stream()
                                                  .collect(Collectors.groupingBy(Entry::getKey,
                                                        Collectors.mapping(Entry<String, String>::getValue, 
                                                        Collectors.toList())));
        List<Entry<String, Object>> transaction = new ArrayList<>();
        for (Entry<String, List<String>> e : groups.entrySet()) 
        {
            Object obj = aggregator.apply(e.getValue());
            transaction.add(new SimpleEntry<>(e.getKey(), obj));
        }
        return new Job(isTransaction, transaction);
    }
}