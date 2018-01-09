package com.zz.sharding.jdbc.main;

import com.dangdang.ddframe.rdb.sharding.api.ShardingValue;
import com.dangdang.ddframe.rdb.sharding.api.strategy.database.SingleKeyDatabaseShardingAlgorithm;
import com.google.common.collect.Range;

import java.util.Collection;
import java.util.LinkedHashSet;

/**
 * Created by zhangzuizui on 2018/1/9.
 */
public class ModuloDatabaseShardingAlgorithm implements SingleKeyDatabaseShardingAlgorithm<String> {


    public String doEqualSharding(Collection<String> availableTargetNames, ShardingValue<String> shardingValue) {
        for (String each : availableTargetNames) {
            if (each.endsWith(shardingValue.getValue().hashCode() % 2 + "")) {
                return each;
            }
        }
        throw new IllegalArgumentException();
    }

    public Collection<String> doInSharding(Collection<String> collection, ShardingValue<String> shardingValue) {
        return null;
    }

    public Collection<String> doBetweenSharding(Collection<String> collection, ShardingValue<String> shardingValue) {
        return null;
    }
}
