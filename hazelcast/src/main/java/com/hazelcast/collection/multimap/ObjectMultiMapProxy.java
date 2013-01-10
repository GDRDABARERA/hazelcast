/*
 * Copyright (c) 2008-2012, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hazelcast.collection.multimap;

import com.hazelcast.collection.CollectionContainer;
import com.hazelcast.collection.CollectionPartitionContainer;
import com.hazelcast.collection.CollectionProxy;
import com.hazelcast.collection.CollectionService;
import com.hazelcast.core.EntryListener;
import com.hazelcast.core.MultiMap;
import com.hazelcast.monitor.LocalMapStats;
import com.hazelcast.nio.serialization.Data;
import com.hazelcast.spi.NodeEngine;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @ali 1/2/13
 */
public class ObjectMultiMapProxy<K, V> extends MultiMapProxySupport implements CollectionProxy, MultiMap<K, V> {

    public ObjectMultiMapProxy(String name, CollectionService service, NodeEngine nodeEngine) {
        super(name, service, nodeEngine);
    }

    public String getName() {
        return name;
    }

    public boolean put(K key, V value) {
        Data dataKey = nodeEngine.toData(key);
        Data dataValue = nodeEngine.toData(value);
        return putInternal(dataKey, dataValue);
    }

    public Collection<V> get(K key) {
        Data dataKey = nodeEngine.toData(key);
        MultiMapCollectionResponse result = getInternal(dataKey);
        return result.getObjectCollection(nodeEngine.getSerializationService());
    }

    public boolean remove(Object key, Object value) {
        Data dataKey = nodeEngine.toData(key);
        Data dataValue = nodeEngine.toData(value);
        return removeInternal(dataKey, dataValue);
    }

    public Collection<V> remove(Object key) {
        Data dataKey = nodeEngine.toData(key);
        MultiMapCollectionResponse result = removeInternal(dataKey);
        return result.getObjectCollection(nodeEngine.getSerializationService());
    }

    public Set<K> localKeySet() {
        Set<Data> dataKeySet = localKeySetInternal();
        return toObjectSet(dataKeySet);
    }

    public Set<K> keySet() {
        Set<Data> dataKeySet = keySetInternal();
        return toObjectSet(dataKeySet);
    }

    public Collection<V> values() {
        Map map = valuesInternal();
        Collection values = new LinkedList();
        for (Object obj: map.values()){
            if (obj == null) {
                continue;
            }
            MultiMapCollectionResponse response = nodeEngine.toObject(obj);
            values.addAll(response.getObjectCollection(nodeEngine.getSerializationService()));
        }
        return values;
    }

    public Set<Map.Entry<K, V>> entrySet() {
        Map map = entrySetInternal();
        Set<Map.Entry<K, V>> entrySet = new HashSet<Map.Entry<K, V>>();
        for (Object obj: map.values()){
            if (obj == null) {
                continue;
            }
            MultiMapResponse response = nodeEngine.toObject(obj);
            Set<Map.Entry<K, V>> entries = response.getObjectEntrySet(nodeEngine);
            entrySet.addAll(entries);
        }
        return entrySet;
    }

    public boolean containsKey(K key) {
        Data dataKey = nodeEngine.toData(key);
        return containsInternal(dataKey, null);
    }

    public boolean containsValue(Object value) {
        Data valueKey = nodeEngine.toData(value);
        return containsInternal(null, valueKey);
    }

    public boolean containsEntry(K key, V value) {
        Data dataKey = nodeEngine.toData(key);
        Data valueKey = nodeEngine.toData(value);
        return containsInternal(dataKey, valueKey);
    }


    public int valueCount(K key) {
        Data dataKey = nodeEngine.toData(key);
        return countInternal(dataKey);
    }

    public void addLocalEntryListener(EntryListener<K, V> listener) {
        service.addEntryListener(name, listener, null, false, true);
    }

    public void addEntryListener(EntryListener<K, V> listener, boolean includeValue) {
        service.addEntryListener(name, listener, null, includeValue, false);
    }

    public void removeEntryListener(EntryListener<K, V> listener) {
        service.removeEntryListener(name, listener, null);
    }

    public void addEntryListener(EntryListener<K, V> listener, K key, boolean includeValue) {
        Data dataKey = nodeEngine.toData(key);
        service.addEntryListener(name, listener, dataKey, includeValue, false);
    }

    public void removeEntryListener(EntryListener<K, V> listener, K key) {
        Data dataKey = nodeEngine.toData(key);
        service.removeEntryListener(name, listener, dataKey);
    }

    public void lock(K key) {

    }

    public boolean tryLock(K key) {
        return false;
    }

    public boolean tryLock(K key, long time, TimeUnit timeunit) {
        return false;
    }

    public void unlock(K key) {

    }

    public LocalMapStats getLocalMultiMapStats() {
        int count = nodeEngine.getPartitionCount();
        for (int i = 0; i < count; i++) {
            CollectionPartitionContainer partitionContainer = service.getPartitionContainer(i);
            Map<String, CollectionContainer> multiMaps = partitionContainer.getContainerMap();
            if (multiMaps.size() > 0) {
                System.out.println("partitionId: " + i);
            }
            for (Map.Entry<String, CollectionContainer> entry : multiMaps.entrySet()) {
                System.out.println("\tname: " + entry.getKey());
                CollectionContainer container = entry.getValue();
                Map<Data, Object> map = container.getObjects();
                for (Map.Entry<Data, Object> en : map.entrySet()) {
                    System.out.println("\t\tkey: " + nodeEngine.toObject(en.getKey()));
                    Collection col = (Collection) en.getValue();
                    for (Object o : col) {
                        System.out.println("\t\t\tval: " + nodeEngine.toObject(o));
                    }
                }
            }

        }
        return null;
    }

    public InstanceType getInstanceType() {
        return InstanceType.MULTIMAP;
    }

    public void destroy() {

    }

    public Object getId() {
        return name;
    }

    private Set<K> toObjectSet(Set<Data> dataSet) {
        Set<K> keySet = new HashSet<K>(dataSet.size());
        for (Data dataKey : dataSet) {
            keySet.add((K)nodeEngine.toObject(dataKey));
        }
        return keySet;
    }
}
