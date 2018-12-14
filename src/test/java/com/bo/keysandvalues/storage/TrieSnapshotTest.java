package com.bo.keysandvalues.storage;

import org.junit.Test;

import java.util.*;
import java.util.AbstractMap.SimpleEntry;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class TrieSnapshotTest {

    @Test
    public void entrySet() {
        TrieSnapshot snapshot = getTrieSnapshot();

        Collection<Map.Entry<String, Object>> entries = snapshot.entrySet();
        Map map = entries.stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        assertEquals(7, map.size());
        assertEquals(2, map.get("aa"));
        assertEquals(3, map.get("aaa"));
        assertEquals(3, map.get("aab"));
        assertEquals(3, map.get("abc"));
        assertEquals(1, map.get("b"));
        assertEquals(2, map.get("ba"));
        assertEquals(4, map.get("cbad"));
    }

    private TrieSnapshot getTrieSnapshot() {
        TrieNode root = new TrieNode();
        root.setChildren(new HashMap<>());
        /*       a         b.1       c
         *   a.2    b      a.2       b
         * a.3 b.3  c.3              a
         *                           d.4
         */
        LinkedList<TrieNode> nodes = new LinkedList<>();
        // level 1:
        nodes.add(addChild(root, new SimpleEntry<>('a', null)));
        nodes.add(addChild(root, new SimpleEntry<>('b', 1)));
        nodes.add(addChild(root, new SimpleEntry<>('c', null)));
        // level 2:
        TrieNode node = nodes.remove();
        nodes.add(addChild(node, new SimpleEntry<>('a', 2)));
        nodes.add(addChild(node, new SimpleEntry<>('b', null)));

        node = nodes.remove();
        nodes.add(addChild(node, new SimpleEntry<>('a', 2)));

        node = nodes.remove();
        nodes.add(addChild(node, new SimpleEntry<>('b', null)));
        // level 3:
        node = nodes.remove();
        nodes.add(addChild(node, new SimpleEntry<>('a', 3)));
        nodes.add(addChild(node, new SimpleEntry<>('b', 3)));

        node = nodes.remove();
        nodes.add(addChild(node, new SimpleEntry<>('c', 3)));

        nodes.remove();

        node = nodes.remove();
        nodes.add(addChild(node, new SimpleEntry<>('a', null)));
        // level 4:
        node = nodes.removeLast();
        nodes.add(addChild(node, new SimpleEntry<>('d', 4)));

        return new TrieSnapshot(root);
    }

    private static TrieNode addChild(TrieNode parent, Map.Entry<Character, Object> child) {
        TrieNode node = new TrieNode();
        node.setValue(child.getValue());
        node.setChildren(new HashMap<>());
        parent.getChildren().put(child.getKey(), node);
        return node;
    }
}