package com.bo.keysandvalues;

public interface ErrorListener 
{
    void onError(String msg);
    void onError(String msg, Exception e);
}