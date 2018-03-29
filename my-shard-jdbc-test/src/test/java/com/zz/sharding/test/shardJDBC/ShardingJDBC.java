
package com.zz.sharding.test.shardJDBC;

import io.shardingjdbc.core.api.ShardingDataSourceFactory;
import io.shardingjdbc.core.api.config.ShardingRuleConfiguration;
import io.shardingjdbc.core.api.config.TableRuleConfiguration;
import io.shardingjdbc.core.api.config.strategy.InlineShardingStrategyConfiguration;
import org.apache.commons.dbcp.BasicDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;


/**
 * 当当分库分表组件实践
 *
 * @author zhangzuizui
 * @date 2018/1/9
 */
public class ShardingJDBC {

    public static void main(String[] args) throws SQLException {
        Map<String, DataSource> dataSourceMap = new HashMap<>();
        // 配置第一个数据源
        dataSourceMap.put("my_shard_01",createDataSource("my_shard_01"));

        // 配置第二个数据源
        dataSourceMap.put("my_shard_02", createDataSource("my_shard_02"));

        // 配置Order表规则
        TableRuleConfiguration orderTableRuleConfig = new TableRuleConfiguration();
        orderTableRuleConfig.setLogicTable("my_order");
        orderTableRuleConfig.setActualDataNodes("my_shard_0${1..2}.my_order_00${1..2}");

        // 配置分库策略
        orderTableRuleConfig.setDatabaseShardingStrategyConfig(new InlineShardingStrategyConfiguration("order_id", "my_shard_0${1}"));

        // 配置分表策略
        orderTableRuleConfig.setTableShardingStrategyConfig(new InlineShardingStrategyConfiguration("order_id", "my_order_00${1}"));

        // 配置分片规则
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTableRuleConfigs().add(orderTableRuleConfig);

        // 省略配置order_item表规则...
        Properties properties = new Properties();
        properties.setProperty("sql.show","true");
        // 获取数据源对象
        DataSource dataSource = ShardingDataSourceFactory.createDataSource(dataSourceMap, shardingRuleConfig, new ConcurrentHashMap(), properties);

        String sql = "SELECT * FROM my_order WHERE order_id = ?";
        //2.获取连接
        Connection conn = dataSource.getConnection();
        /**
         * PrepareStatement接口是Statement接口的子接口，他继承了Statement接口的所有功能。它主要是拿来解决我们使用Stateme
         * ParperStatement接口的机制是在数据库支持预编译的情况下预先将SQL语句编译，当多次执行这条SQL语句时，可以直接执行编译好的SQL语句，
         * sql预处理
         */
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, "001");
        //4.SQL执行和结果归并
        ResultSet rs = pstmt.executeQuery();
        //5.获取结果
        while(rs.next()) {
            System.out.println("id="+rs.getInt(1)+",order_id="+rs.getString(2));
        }
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

