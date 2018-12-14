package com.bo.mocks;

import com.bo.keysandvalues.storage.Snapshot;
import com.bo.keysandvalues.storage.Storage;

import java.util.HashMap;
import java.util.Map;

public class SimpleStorage implements Storage {

    private SimpleSnapshot snapshot;

    private String errorKey;

    @Override
    public void initialize() {
        snapshot = new SimpleSnapshot(new HashMap<>());
    }

    @Override
    public void initialize(Snapshot snapshot) {
        Map<String, Object> map = ((SimpleSnapshot) snapshot).getMap();
        this.snapshot = new SimpleSnapshot(new HashMap<>(map));
    }

    @Override
    public void put(String key, Object value) {
        if (key.equals(errorKey)) {
            throw new RuntimeException();
        }
        snapshot.getMap().put(key, value);
    }

    @Override
    public Snapshot createSnapshot() {
        Snapshot s = snapshot;
        initialize(snapshot);
        return s;
    }

    @Override
    public boolean isDirty() {
        return true;
    }

    public void setErrorKey(String errorKey) {
        this.errorKey = errorKey;
    }
}
