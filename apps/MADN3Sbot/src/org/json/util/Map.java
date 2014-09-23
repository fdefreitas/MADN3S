package org.json.util;

import java.util.Enumeration;
import java.util.List;

public interface Map<K, V> {
   public void clear();
   public boolean containsKey(K key);
   public boolean containsValue(V value);
   public List<K> keysList();
   public Enumeration<K> keysEnum();
   public boolean equals(Map<K, V> rhsMap);
   public V get(K key);
   public int hashCode();
   public boolean isEmpty();
   public V put(K key, V value);
   public void putAll(Map<K, V> rhsMap);
   public V remove(K key);
   public int size();
   public List<V> valuesList();
}
