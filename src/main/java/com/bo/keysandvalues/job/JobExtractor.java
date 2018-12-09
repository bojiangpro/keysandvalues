package com.bo.keysandvalues.job;

import java.util.List;
import java.util.Map.Entry;

public interface JobExtractor
{
    /**
     * Extract jobs from key-value pairs.
     * @param kvPairs key value pairs
     * @return jobs
     */
    List<Job> extractJobs(List<Entry<String, String>> kvPairs);
}