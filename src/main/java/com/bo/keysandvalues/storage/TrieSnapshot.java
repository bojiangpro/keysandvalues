package com.bo.keysandvalues.storage;

import java.util.*;
import java.util.Map.Entry;
import java.util.AbstractMap.SimpleEntry;
import java.util.function.BiConsumer;

public class TrieSnapshot implements Snapshot{

    private TrieNode root;

    TrieSnapshot(TrieNode root) {
        this.root = root;
    }

    TrieNode getRoot() {
        return root;
    }

    @Override
    public Collection<Entry<String, Object>> entrySet() {
        Collection<Entry<String, Object>> entries = new ArrayList<>();
        BiConsumer<TrieNode, StringBuilder> visitor = (node, path) -> {
          Object value = node.getValue();
          if (value != null) {
              entries.add(new SimpleEntry<>(path.substring(1), value));
          }
        };

        depthFirstTraverse(root, visitor);
        return entries;
    }

    private static void depthFirstTraverse(TrieNode root, BiConsumer<TrieNode, StringBuilder> visitor) {
        Stack<Iterator<Entry<Character, TrieNode>>> stack = new Stack<>();
        StringBuilder path = new StringBuilder();
        push(stack, path, root, '\0');

        while (!stack.isEmpty()) {
            Iterator<Entry<Character, TrieNode>> iterator = stack.peek();
            if (iterator == null || !iterator.hasNext()) {
                pop(stack, path);
            } else {
                Entry<Character, TrieNode> child = iterator.next();
                TrieNode node = child.getValue();
                push(stack, path, node, child.getKey());
                visitor.accept(node, path);
            }
        }
    }

    private static void push(Stack<Iterator<Entry<Character, TrieNode>>> stack,
                             StringBuilder path, TrieNode node, char key) {
        Map<Character, TrieNode> children = node.getChildren();
        stack.push(children == null ? null : children.entrySet().iterator());
        path.append(key);
    }

    private static void pop(Stack<Iterator<Entry<Character, TrieNode>>> stack, StringBuilder path) {
        stack.pop();
        path.setLength(path.length()-1);
    }
}
