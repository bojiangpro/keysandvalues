package com.bo.keysandvalues.storage;

import java.util.Collection;
import java.util.Map.Entry;

public interface Snapshot {

    Collection<Entry<String, Object>> entrySet();
}
