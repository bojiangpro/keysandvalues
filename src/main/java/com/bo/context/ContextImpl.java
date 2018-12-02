package com.bo.context;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ContextImpl implements Context
{
    private final Map<String, Object> defaultInstances;
    private final Map<String, Map<String, Object>> namedInstances;
    private final Map<String, Supplier<Object>> typeSuppliers;

    public ContextImpl()
    {
        this.defaultInstances = new HashMap<>();
        this.namedInstances = new HashMap<>();
        this.typeSuppliers = new HashMap<>();
    }

    @Override
    public <T, E extends T> void Register(Class<T> type, E instance) 
    {
        if (instance == null) return;
        String key = type.getCanonicalName();
        this.defaultInstances.put(key, instance);
    }

    @Override
    public <T, E extends T> void Register(Class<T> type, E instance, String name) 
    {
        if (instance == null) return;
        String key = type.getCanonicalName();
        if (!this.namedInstances.containsKey(key))
        {
            this.namedInstances.put(key, new HashMap<>());
        }
        this.namedInstances.get(key).put(name, instance);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T Resolve(Class<T> type) 
    {
        String key = type.getCanonicalName();
        return (T) this.defaultInstances.getOrDefault(key, (T)null);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T Resolve(Class<T> type, String name) {
        String key = type.getCanonicalName();
        
        if (!this.namedInstances.containsKey(key))
        {
            return null;
        }
        return (T) this.namedInstances.get(key).getOrDefault(name, null);
    }

    @Override
    public <T> void RegisterType(Class<T> type, Supplier<T> getInstance) 
    {
        if (getInstance == null) return;
        String key = type.getCanonicalName();
        this.typeSuppliers.put(key, () -> getInstance.get());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T ResolveType(Class<T> type) 
    {
        String key = type.getCanonicalName();
        if (!this.typeSuppliers.containsKey(key))
        {
            return null;
        }
        return (T) this.typeSuppliers.getOrDefault(key, () -> null).get();
    }

}