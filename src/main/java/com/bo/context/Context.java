package com.bo.context;

import java.util.function.Supplier;

/**
 * Application context to manager objects for dependency injection
 */
public interface Context
{
    <T, E extends T> void Register(Class<T> type, E instance);
    <T, E extends T> void Register(Class<T> type, E instance, String name);
    <T> T Resolve(Class<T> type);
    <T> T Resolve(Class<T> type, String name);
    <T> void RegisterType(Class<T> type, Supplier<T> getInstance);
    <T> T ResolveType(Class<T> type);
}