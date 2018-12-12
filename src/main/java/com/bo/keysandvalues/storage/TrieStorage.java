package com.bo.keysandvalues.storage;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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
        }
    }

    private void init(TrieNode root) {
        mutableNodes.clear();
        this.root = getMutableNode(root);
    }

    public void put(String key, Object value) {
        TrieNode parent  = root;
        int length = key.length();
        int index = 0;

        while (true) {
            if (index >= length) {
                Object old = parent.getValue();
                if (old != null) {
                    value = aggregator.apply(old, value);
                }
                parent.setValue(value);
                return;
            }
            Map<Character, TrieNode> children = parent.getChildren();
            if (children == null) {
                children = new HashMap<>();
                parent.setChildren(children);
            } else {
                parent = children.compute(key.charAt(index), (k, n) -> getMutableNode(n));
                index ++;
            }
        }

    }

    @Override
    public Snapshot createSnapshot() {
        Snapshot snapshot = new TrieSnapshot(root);
        // prepare for next snapshot
        init(root);
        return snapshot;
    }

    private TrieNode getMutableNode(TrieNode node) {
        if (node == null) {
          node = new TrieNode();
          mutableNodes.add(node);
        } else if (!mutableNodes.contains(node)) {
            Object value = node.getValue();
            Map<Character, TrieNode> children = node.getChildren();
            if (children != null) {
                children = new HashMap<>(children);
            }
            node = new TrieNode();
            node.setValue(value);
            node.setChildren(children);
            mutableNodes.add(node);
        }
        return node;
    }
}
