package com.bo.keysandvalues.storage;


import java.util.*;
import java.util.Map.Entry;
import java.util.function.BiFunction;

public class TrieStorage implements Storage{

    private TrieNode root;
    private BiFunction<Object, Object, Object> aggregator;
    private Set<TrieNode> mutableNodes;

    public TrieStorage(BiFunction<Object, Object, Object> aggregator) {
        mutableNodes = new HashSet<>();
        this.aggregator = aggregator;
    }

    @Override
    public void initialize() {
        init(null);
    }

    @Override
    public void initialize(Snapshot snapshot) {
        if (snapshot instanceof TrieSnapshot) {
            init(((TrieSnapshot) snapshot).getRoot());
        } else {
            init(null);
            Collection<Entry<String, Object>> entries = snapshot.entrySet();
            for (Entry<String, Object> e : entries) {
                put(e.getKey(), e.getValue());
            }
        }
    }

    private void init(TrieNode root) {
        mutableNodes.clear();
        this.root = getMutableNode(root);
    }

    public void put(String key, Object value) {
        int length = key.length();
        int index = 0;
        // search node
        TrieNode node = root;
        while (index < length) {
            Map<Character, TrieNode> children = node.getChildren();
            if (children == null) {
                children = new HashMap<>();
                node.setChildren(children);
            }
            node = children.compute(key.charAt(index), (k, n) -> getMutableNode(n));
            index ++;
        }
        // update value
        Object old = node.getValue();
        if (old != null) {
            value = aggregator.apply(old, value);
        }
        node.setValue(value);
    }

    @Override
    public Snapshot createSnapshot() {
        return new TrieSnapshot(root);
    }

    @Override
    public boolean isDirty() {
        // exclude root
        return mutableNodes.size() > 1;
    }

    private TrieNode getMutableNode(TrieNode node) {
        if (node == null) {
          node = new TrieNode();
          mutableNodes.add(node);
        } else if (!mutableNodes.contains(node)) {
            node = new TrieNode(node);
            mutableNodes.add(node);
        }
        return node;
    }
}
