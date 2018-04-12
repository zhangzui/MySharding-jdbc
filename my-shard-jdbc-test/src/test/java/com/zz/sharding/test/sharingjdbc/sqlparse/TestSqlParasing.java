package com.zz.sharding.test.sharingjdbc.sqlparse;

import com.alibaba.fastjson.JSON;
import io.shardingjdbc.core.api.ShardingDataSourceFactory;
import io.shardingjdbc.core.api.config.ShardingRuleConfiguration;
import io.shardingjdbc.core.api.config.TableRuleConfiguration;
import io.shardingjdbc.core.api.config.strategy.InlineShardingStrategyConfiguration;
import io.shardingjdbc.core.constant.DatabaseType;
import io.shardingjdbc.core.jdbc.core.connection.ShardingConnection;
import io.shardingjdbc.core.parsing.lexer.LexerEngine;
import io.shardingjdbc.core.parsing.lexer.LexerEngineFactory;
import io.shardingjdbc.core.parsing.parser.context.condition.Column;
import io.shardingjdbc.core.parsing.parser.context.condition.Condition;
import io.shardingjdbc.core.parsing.parser.dialect.mysql.sql.MySQLSelectParser;
import io.shardingjdbc.core.parsing.parser.sql.dql.select.SelectStatement;
import io.shardingjdbc.core.routing.router.ParsingSQLRouter;
import org.apache.commons.dbcp.BasicDataSource;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zhangzuizui
 * @date 2018/4/12 14:14
 */
public class TestSqlParasing {

    /**
     * 测试sharding-jdbc的sql解析
     * @param args
     */
    public static void main(String[] args) throws SQLException {
        DataSource dataSource = getDataSource();
        String sql = "SELECT id, order_id, info FROM my_order WHERE order_id = ?";
        try {
            ShardingConnection shardingConnection = (ShardingConnection) dataSource.getConnection();
            LexerEngine lexerEngine = LexerEngineFactory.newInstance(DatabaseType.MySQL, sql);
            MySQLSelectParser mySQLSelectParser = new MySQLSelectParser(shardingConnection.getShardingContext().getShardingRule(), lexerEngine);
            SelectStatement result = mySQLSelectParser.parse();
            Map<Column, Condition>  map = result.getConditions().getConditions();
            for (Map.Entry entry:map.entrySet()) {
                String key = entry.getKey().toString();
                Condition value = (Condition) entry.getValue();
                System.out.println("column:"+key);
//                System.out.println("value:"+value.);
            }
            System.out.println("getSqlTokens:"+JSON.toJSONString(result.getSqlTokens()));
            System.out.println("getConditions:"+JSON.toJSONString(result.getConditions().getConditions()));

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static DataSource getDataSource() throws SQLException {
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
        orderTableRuleConfig.setDatabaseShardingStrategyConfig(new InlineShardingStrategyConfiguration("order_id", "my_shard_0${order_id.hashCode()%2+1}"));

        // 配置分表策略
        orderTableRuleConfig.setTableShardingStrategyConfig(new InlineShardingStrategyConfiguration("order_id", "my_order_00${order_id.hashCode()%2+1}"));

        // 配置分片规则
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTableRuleConfigs().add(orderTableRuleConfig);

        // 省略配置order_item表规则...
        Properties properties = new Properties();
        properties.setProperty("sql.show","true");
        // 获取数据源对象
        return ShardingDataSourceFactory.createDataSource(dataSourceMap, shardingRuleConfig, new ConcurrentHashMap(), properties);
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
