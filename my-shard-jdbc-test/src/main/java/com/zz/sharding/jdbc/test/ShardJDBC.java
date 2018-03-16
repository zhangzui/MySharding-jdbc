
package com.zz.sharding.jdbc.test;

import com.dangdang.ddframe.rdb.sharding.api.rule.BindingTableRule;
import com.dangdang.ddframe.rdb.sharding.api.rule.DataSourceRule;
import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.api.rule.TableRule;
import com.dangdang.ddframe.rdb.sharding.api.strategy.database.DatabaseShardingStrategy;
import com.dangdang.ddframe.rdb.sharding.api.strategy.table.TableShardingStrategy;
import com.dangdang.ddframe.rdb.sharding.jdbc.ShardingDataSource;
import com.zz.sharding.jdbc.algorithm.ModuloDatabaseShardingAlgorithm;
import com.zz.sharding.jdbc.algorithm.ModuloTableShardingAlgorithm;
import org.apache.commons.dbcp.BasicDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by zhangzuizui on 2018/1/9.
 */

public class ShardJDBC {

    public static void main(String[] args) throws SQLException {

        //数据源
        Map<String, DataSource> dataSourceMap = new HashMap<String, DataSource>(2);
        dataSourceMap.put("my-shard-01", createDataSource("my-shard-01"));
        dataSourceMap.put("my-shard-02", createDataSource("my-shard-02"));

        DataSourceRule dataSourceRule = new DataSourceRule(dataSourceMap);
        TableRule orderTableRule = buildTableRule(dataSourceRule);

        /**
         * DatabaseShardingStrategy 分库策略
         * 参数一：根据哪个字段分库
         * 参数二：分库路由函数
         * TableShardingStrategy 分表策略
         * 参数一：根据哪个字段分表
         * 参数二：分表路由函数
         *
         */

        ShardingRule shardingRule = new ShardingRule(dataSourceRule, Arrays.asList(orderTableRule),
                Arrays.asList(new BindingTableRule(Arrays.asList(orderTableRule))),
                new DatabaseShardingStrategy("order_id", new ModuloDatabaseShardingAlgorithm()),
                new TableShardingStrategy("order_id", new ModuloTableShardingAlgorithm()));

        DataSource dataSource = new ShardingDataSource(shardingRule);

        String sql = "SELECT * FROM my_order  WHERE order_id = ?";
        Connection conn = dataSource.getConnection();

        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, "001");
        ResultSet rs = pstmt.executeQuery();

        while(rs.next()) {
            System.out.println(rs.getInt(1));
            System.out.println(rs.getString(2));
        }
    }

    private static TableRule buildTableRule(DataSourceRule dataSourceRule) {
        //分表分库的表，，，第三个是实际库
        //逻辑表名:String logicTable
        //是否是动态：boolean dynamic
        //实际表名: List<String> actualTables
        //DataSourceRule dataSourceRule
        //Collection<String> dataSourceNames
        //DatabaseShardingStrategy databaseShardingStrategy
        //TableShardingStrategy tableShardingStrategy
        DatabaseShardingStrategy databaseShardingStrategynew = new DatabaseShardingStrategy("order_id", new ModuloDatabaseShardingAlgorithm());
        TableShardingStrategy tableShardingStrategy = new TableShardingStrategy("order_id", new ModuloTableShardingAlgorithm());
        TableRule orderTableRule = new TableRule("my_order",
                false,
                Arrays.asList("my_order_001", "my_order_002"),
                dataSourceRule,null,databaseShardingStrategynew,tableShardingStrategy);
        return orderTableRule;
    }


/**
     * 创建数据源
     * @param dataSourceName
     * @return
     */

    private static DataSource createDataSource(String dataSourceName) {
        BasicDataSource result = new BasicDataSource();
        result.setDriverClassName(com.mysql.jdbc.Driver.class.getName());
        result.setUrl(String.format("jdbc:mysql://localhost:3306/%s", dataSourceName));
        result.setUsername("root");
        result.setPassword("123456");
        return result;
    }

}

