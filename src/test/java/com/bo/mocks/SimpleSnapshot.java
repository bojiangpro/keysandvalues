package com.bo.mocks;

import com.bo.keysandvalues.storage.Snapshot;

import java.util.Collection;
import java.util.Map;

public class SimpleSnapshot implements Snapshot {

    private Map<String, Object> map;

    SimpleSnapshot(Map<String, Object> map) {
        this.map = map;
    }

    @Override
    public Collection<Map.Entry<String, Object>> entrySet() {
        return map.entrySet();
    }

    public Map<String, Object> getMap() {
        return map;
    }
}
