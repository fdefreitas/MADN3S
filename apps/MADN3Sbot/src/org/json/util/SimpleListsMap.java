package org.json.util;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class SimpleListsMap<K, V> implements Map<K, V> {
   private List<K> keysList = new ArrayList<K>();
   private List<V> valuesList = new ArrayList<V>();
      
   public SimpleListsMap() {
      super();
   }

   public SimpleListsMap(Map<K, V> rhsMap) {
      putAll(rhsMap);
   }
   
   @Override
   public void clear() {
      keysList.clear();
      valuesList.clear();
   }

   @Override
   public boolean containsKey(K key) {
      if(key==null) {
         throw new NullPointerException();
      }
      return keysList.contains(key);
   }

   @Override
   public boolean containsValue(V value) {
      if(value==null) {
         throw new NullPointerException();
      }
      return valuesList.contains(value);
   }

   @Override
   public boolean equals(Map<K, V> rhsMap) {
      if(rhsMap==null) {
         throw new NullPointerException();
      }
      if(rhsMap.size()!=size()) {
         return false;
      }
      for (K key : rhsMap.keysList()) {
         V value = rhsMap.get(key);
         if((!containsKey(key)) ||(!containsValue(value)) || (!value.equals(get(key)))) {
            return false;
         }
      }
      return true;
   }

   @Override
   public V get(K key) {
      if(key==null) {
         throw new NullPointerException();
      }
      if(!keysList.contains(key)) {
         return null;
      }
      
      final int index = getIndex(key);
      return valuesList.get(index);   
      
   }

   @Override
   public boolean isEmpty() {
      return keysList.isEmpty();
   }

   @Override
   public List<K> keysList() {
      return new ArrayList<K>(keysList);
   }

   @Override
   public V put(K key, V value) {
      if((key==null) || (value==null)) {
         throw new NullPointerException();
      }   
      if(containsKey(key)) {
         remove(key);
      }
      keysList.add(key);
      valuesList.add(value);
      return value;
   }

   @Override
   public void putAll(Map<K,V> rhsMap) {
      if(rhsMap==null) {
         throw new NullPointerException();
      }
      for (K key : rhsMap.keysList()) {
         put(key, rhsMap.get(key));
      }
   }

   @Override
   public V remove(K key) {
      if(key==null) {
         throw new NullPointerException();
      }
      if(!keysList.contains(key)) {
         return null;
      }
      final int index = getIndex(key);
      keysList.remove(index);      
      return valuesList.remove(index);
   }

   @Override
   public int size() {
      return keysList.size();
   }

   @Override
   public List<V> valuesList() {
      return new ArrayList<V>(valuesList);
   }
   
   
   
   @Override
   public String toString() {
      StringBuilder strBuilder = new StringBuilder();
      strBuilder.append("[");
      for (int i = 0; i < keysList.size(); i++) {
         K key = keysList.get(i);
         if(i>0) {
            strBuilder.append(",");
         }   
         strBuilder.append(key);
         strBuilder.append("=>");
         strBuilder.append(get(key));
      }
      strBuilder.append("]");
      return strBuilder.toString();
   }
   
   private int getIndex(K key) {
      final int SIZE = keysList.size();
      for (int i = 0; i < SIZE; i++) {
         if(keysList.get(i).equals(key)) {
            return i;
         }         
      }
      throw new IllegalStateException("Looking for the index of an non-existing key!");
   }

	@Override
	public Enumeration<K> keysEnum() {
		return new Enumeration<K>()
		{
			int index = 0;
			
			@Override
			public boolean hasMoreElements() {
				return index<keysList.size();
			}

			@Override
			public K nextElement() {
				K elem = keysList.get(index);
				index++;
				return elem;
			}			
		};
	}
   
//   public static void main(String[] args) {
//      Map<String, Integer> strToIntMap1 = new SimpleListsMap<String, Integer>();
//      Map<String, Integer> strToIntMap2 = new SimpleListsMap<String, Integer>();
//      Map<String, Integer> strToIntMap3 = new SimpleListsMap<String, Integer>();
//   
//      strToIntMap1.put("A", 1);
//      strToIntMap1.put("B", 2);
//      System.out.println(strToIntMap1.size());
//      System.out.println(strToIntMap1.toString());
//      System.out.println(strToIntMap1.get("A"));
//      System.out.println(strToIntMap1.keysList());
//      System.out.println(strToIntMap1.valuesList());
//      strToIntMap1.remove("B");
//      System.out.println(strToIntMap1.toString());
//      strToIntMap1.clear();
//      System.out.println(strToIntMap1.toString());
//      System.out.println(strToIntMap1.isEmpty());
//      strToIntMap1.put("A", 1);
//      strToIntMap1.put("B", 2);
//      System.out.println(strToIntMap1.isEmpty());
//      System.out.println(strToIntMap1.toString());
//      strToIntMap2.put("C", 4);
//      strToIntMap2.put("D", 5);
//      System.out.println(strToIntMap2.toString());
//      
//      strToIntMap1.putAll(strToIntMap2);
//      System.out.println(strToIntMap1.toString());
//      strToIntMap3.put("C", 4);
//      strToIntMap3.put("D", 5);
//      System.out.println(strToIntMap1.equals(strToIntMap2));   
//      System.out.println(strToIntMap2.equals(strToIntMap3));   
//      
//      Map<Integer, String> intToStrMap1= new SimpleListsMap<Integer, String>();
//      Map<Integer, String> intToStrMap2 = new SimpleListsMap<Integer, String>();
//
//   }
}