package com.bo.keysandvalues.storage;

public interface Storage {

    void initialize();

    void initialize(Snapshot snapshot);

    void put(String key, Object value);

    Snapshot createSnapshot();

    boolean isDirty();
}
