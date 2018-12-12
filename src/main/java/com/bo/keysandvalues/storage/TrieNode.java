package com.bo.keysandvalues.storage;

import java.util.Map;

class TrieNode {

    private Map<Character, TrieNode> children;
    private Object value;

    Map<Character, TrieNode> getChildren() {
        return children;
    }

    void setChildren(Map<Character, TrieNode> children) {
        this.children = children;
    }

    Object getValue() {
        return value;
    }

    void setValue(Object value) {
        this.value = value;
    }
}