package com.bo.keysandvalues.storage;

import com.bo.keysandvalues.job.JobUtils;
import com.bo.mocks.SimpleSnapshot;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class TrieStorageTest {

    private TrieStorage trieStorage;

    @Before
    public void setUp() {
         trieStorage = new TrieStorage(JobUtils::aggregate);
    }

    @Test
    public void put() {
        trieStorage.initialize();
        trieStorage.put("a", 1);
        trieStorage.put("aa", 1);
        trieStorage.put("ab", 1);
        trieStorage.put("babc", "1");
        Snapshot snapshot = trieStorage.createSnapshot();
        verify(snapshot, new Object[]{"a", 1, "aa", 1, "ab", 1, "babc", "1"});
        trieStorage.put("a", 2);
        trieStorage.put("babc", "2");
        verify(snapshot, new Object[]{"a", 3, "aa", 1, "ab", 1, "babc", "2"});
    }

    @Test
    public void initialize() {
        put();
        assertTrue(trieStorage.isDirty());
        trieStorage.initialize();
        assertFalse(trieStorage.isDirty());
        Snapshot snapshot = trieStorage.createSnapshot();
        TrieNode root = ((TrieSnapshot) snapshot).getRoot();
        assertNull(root.getValue());
        assertNull(root.getChildren());
        assertTrue(snapshot.entrySet().isEmpty());
    }

    @Test
    public void createSnapshot() {
        put();
        Snapshot snapshot = trieStorage.createSnapshot();
        trieStorage.initialize();
        trieStorage.put("other", 1);
        // snapshot data not changed
        verify(snapshot, new Object[]{"a", 3, "aa", 1, "ab", 1, "babc", "2"});
    }

    @Test
    public void isDirty() {
        assertFalse(trieStorage.isDirty());
        trieStorage.initialize();
        trieStorage.put("key", 1);
        assertTrue(trieStorage.isDirty());
        trieStorage.initialize(trieStorage.createSnapshot());
        assertFalse(trieStorage.isDirty());
        trieStorage.put("key", 1);
        assertTrue(trieStorage.isDirty());
        trieStorage.initialize();
        assertFalse(trieStorage.isDirty());
    }

    @Test
    public void initializeWithSnapshot() {
        put();
        Snapshot snapshot = trieStorage.createSnapshot();
        setUp();
        assertFalse(trieStorage.isDirty());
        trieStorage.initialize(snapshot);
        trieStorage.put("other", 1);
        assertTrue(trieStorage.isDirty());
        verify(trieStorage.createSnapshot(), new Object[]{"a", 3, "aa", 1, "ab", 1, "babc", "2", "other", 1});
        // snapshot data not changed
        verify(snapshot, new Object[]{"a", 3, "aa", 1, "ab", 1, "babc", "2"});
    }

    @Test
    public void initializeWithAnySnapshot() {
        Map<String, Object> map = new HashMap<>();
        SimpleSnapshot snapshot = new SimpleSnapshot(map);
        map.put("a", 1);
        map.put("aa", 1);
        map.put("ab", 1);
        map.put("babc", "1");
        trieStorage.initialize(snapshot);
        trieStorage.put("other", 1);
        assertTrue(trieStorage.isDirty());
        verify(trieStorage.createSnapshot(), new Object[]{"a", 1, "aa", 1, "ab", 1, "babc", "1", "other", 1});
        // snapshot data not changed
        verify(snapshot, new Object[]{"a", 1, "aa", 1, "ab", 1, "babc", "1"});
    }

    private void verify(Snapshot snapshot, Object[] expected) {
        Collection<Map.Entry<String, Object>> entries = snapshot.entrySet();
        Map map = entries.stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        assertEquals(expected.length/2, map.size());
        for (int i = 0; i < expected.length; i += 2) {
            assertEquals(expected[i+1], map.get(expected[i].toString()));
        }
    }
}