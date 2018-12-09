package com.bo.keysandvalues.job;

import java.util.List;
import java.util.Map.Entry;

public class Job
{
    private boolean isTransaction;
    private List<Entry<String, Object>> data;

    public Job(boolean isTransaction, List<Entry<String, Object>> data)
    {
        this.isTransaction = isTransaction;
        this.data = data;
    }

    public boolean isTransaction(){
        return isTransaction;
    }

    public List<Entry<String, Object>> getData() {
        return data;
    }
}