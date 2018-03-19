
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
 * 当当分库分表组件实践
 *
 * @author zhangzuizui
 * @date 2018/1/9
 */
public class ShardingJDBCDemo {

    public static void main(String[] args) throws SQLException {

        //0.分库分表初始化路由配置
        ShardingRule shardingRule = buildShardingRule();
        //1.初始化dataSource
        DataSource dataSource = new ShardingDataSource(shardingRule);

        String sql = "SELECT * FROM my_order  WHERE order_id = ?";

        //2.获取连接
        Connection conn = dataSource.getConnection();

        /**
         * PrepareStatement接口是Statement接口的子接口，他继承了Statement接口的所有功能。它主要是拿来解决我们使用Statement对象多次执行同一个SQL语句的效率问题的。
         * ParperStatement接口的机制是在数据库支持预编译的情况下预先将SQL语句编译，当多次执行这条SQL语句时，可以直接执行编译好的SQL语句，这样就大大提高了程序的灵活性和执行效率。
         * sql预处理
         */
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, "000001");

        //4.SQL执行和结果归并
        ResultSet rs = pstmt.executeQuery();

        //5.获取结果
        while(rs.next()) {
            System.out.println("id="+rs.getInt(1)+",order_id="+rs.getString(2));
        }
    }
    private static ShardingRule buildShardingRule(){
        //数据库规则
        DataSourceRule dataSourceRule = getDataSourceRule();
        //表规则
        TableRule tableRule = buildTableRule(dataSourceRule);

        //分库分表-策略
        DatabaseShardingStrategy dbStrategy = new DatabaseShardingStrategy("order_id", new ModuloDatabaseShardingAlgorithm());
        TableShardingStrategy tableStrategy = new TableShardingStrategy("order_id", new ModuloTableShardingAlgorithm());

        ShardingRule shardingRule = new ShardingRule.
                ShardingRuleBuilder().
                dataSourceRule(dataSourceRule).
                tableRules(Arrays.asList(tableRule)).
                databaseShardingStrategy(dbStrategy).
                tableShardingStrategy(tableStrategy).
                bindingTableRules((Arrays.asList(new BindingTableRule(Arrays.asList(tableRule))))).
                build();
        return shardingRule;
    }

    /**
     * 库规则
     * @return DataSourceRule
     */
    private static DataSourceRule getDataSourceRule() {
        //数据源配置信息
        Map<String, DataSource> dataSourceMap = new HashMap<String, DataSource>(2);
        dataSourceMap.put("my_shard_01", createDataSource("my_shard_01"));
        dataSourceMap.put("my_shard_02", createDataSource("my_shard_01"));

        //库规则
        return new DataSourceRule(dataSourceMap);
    }

    /**
     * 表规则
     * 1.逻辑表名:String logicTable
     * 2.是否是动态：boolean dynamic
     * 3.实际表名: List<String> actualTables
     * 4.DataSourceRule dataSourceRule
     * 5.Collection<String> dataSourceNames
     * 6.DatabaseShardingStrategy databaseShardingStrategy
     * 7.TableShardingStrategy tableShardingStrategy
     * @param dataSourceRule
     * @return TableRule
     */
    private static TableRule buildTableRule(DataSourceRule dataSourceRule) {
        DatabaseShardingStrategy databaseShardingStrategynew = new DatabaseShardingStrategy("order_id", new ModuloDatabaseShardingAlgorithm());
        TableShardingStrategy tableShardingStrategy = new TableShardingStrategy("order_id", new ModuloTableShardingAlgorithm());
        TableRule orderTableRule = new TableRule("my_order", false, Arrays.asList("my_order_001", "my_order_002"), dataSourceRule,null,databaseShardingStrategynew,tableShardingStrategy);
        return orderTableRule;
    }

    /**
     * 创建数据源
     * @param dataSourceName
     * @return DataSource
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

