package service;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;

class TreeHashMap<K extends Comparable<K>, V> {
    private static final int DEFAULT_CAPACITY = 16;
    private static final float DEFAULT_LOAD_FACTOR = 0.75f;

    private TreeMap<K, V>[] buckets;
    private int size;
    private final float loadFactor;

    @SuppressWarnings("unchecked")
    public TreeHashMap() {
        this(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR);
    }

    @SuppressWarnings("unchecked")
    public TreeHashMap(int capacity, float loadFactor) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be greater than zero");
        }
        if (loadFactor <= 0 || loadFactor > 1) {
            throw new IllegalArgumentException("Load factor must be in (0, 1]");
        }
        this.buckets = new TreeMap[capacity];
        this.size = 0;
        this.loadFactor = loadFactor;
    }

    private int getBucketIndex(K key) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        return (key.hashCode() & 0x7FFFFFFF) % buckets.length;
    }

    private void resize() {
        int newCapacity = buckets.length * 2;
        TreeMap<K, V>[] newBuckets = new TreeMap[newCapacity];

        for (TreeMap<K, V> bucket : buckets) {
            if (bucket != null) {
                for (K key : bucket.keySet()) {
                    int newIndex = (key.hashCode() & 0x7FFFFFFF) % newCapacity;
                    if (newBuckets[newIndex] == null) {
                        newBuckets[newIndex] = new TreeMap<>();
                    }
                    newBuckets[newIndex].put(key, bucket.get(key));
                }
            }
        }
        buckets = newBuckets;
    }

    public void put(K key, V value) {
        int index = getBucketIndex(key);
        if (buckets[index] == null) {
            buckets[index] = new TreeMap<>();
        }
        if (!buckets[index].containsKey(key)) {
            size++;
        }
        buckets[index].put(key, value);

        if ((float) size / buckets.length > loadFactor) {
            resize();
        }
    }

    public V get(K key) {
        int index = getBucketIndex(key);
        if (buckets[index] == null) {
            return null;
        }
        return buckets[index].get(key);
    }

    public V remove(K key) {
        int index = getBucketIndex(key);
        if (buckets[index] == null || !buckets[index].containsKey(key)) {
            return null;
        }

        size--;
        return buckets[index].remove(key);
    }

    public void clear() {
        for (TreeMap<K, V> bucket : buckets) {
            if (bucket != null) {
                bucket.clear();
            }
        }
        size = 0;
    }

    public boolean containsKey(K key) {
        int index = getBucketIndex(key);
        return buckets[index] != null && buckets[index].containsKey(key);
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public Set<K> keySet() {
        Set<K> keys = new HashSet<>();
        for (TreeMap<K, V> bucket : buckets) {
            if (bucket != null) {
                keys.addAll(bucket.keySet());
            }
        }
        return keys;
    }
}
