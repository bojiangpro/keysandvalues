package com.bo.keysandvalues.job;

import java.util.List;
import java.util.Map.Entry;

public class Job
{
    private boolean isTrasaction;
    private List<Entry<String, Object>> data;

    public Job(boolean isTrasaction, List<Entry<String, Object>> data)
    {
        this.isTrasaction = isTrasaction;
        this.data = data;
    }

    public boolean isTrasaction(){
        return isTrasaction;
    }

    public List<Entry<String, Object>> getData() {
        return data;
    }
}