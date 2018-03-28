package com.zz.db.route.dbroute;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by zhangkai9@jd.com on 16-12-29.
 */
public class DbShardingConfig {

    static Logger logger = LoggerFactory.getLogger(DbShardingConfig.class);

    /**
     * 需要分库不分表的表名
     */
    private Set<String> tablesForDbSharding = new HashSet<String>();

    /**
     * 需要分库分表的表名
     */
    private Set<String> tablesForTableSharding = new HashSet<String>();

    private static DbShardingConfig dbShardingConfig = new DbShardingConfig();

    private DbShardingConfig() {
        // 默认分库分表
        tablesForTableSharding.add("f_usermessages");
        tablesForTableSharding.add("zz_user");
        // 默认分库 不分表
        tablesForDbSharding.add("cf_task_0000");
        syncDbSharding();

    }

    public static DbShardingConfig getInst() {
        return dbShardingConfig;
    }

    /**
     * 添加分库分表的表名
     *
     * @param _tablesForTableSharding
     */
    public void addTablesForTableSharding(Set<String> _tablesForTableSharding) {
        this.tablesForTableSharding.addAll(_tablesForTableSharding);
        syncDbSharding();
    }

    public void addTablesForTableSharding(String _tablesForTableSharding) {
        this.tablesForTableSharding.add(_tablesForTableSharding);
        syncDbSharding();
    }

    /**
     * 设置分库分表的表名
     *
     * @param _tablesForTableSharding
     */
    public void setTablesForTableSharding(Set<String> _tablesForTableSharding) {
        tablesForDbSharding.removeAll(tablesForTableSharding);
        this.tablesForTableSharding = _tablesForTableSharding;
        syncDbSharding();
    }


    /**
     * 添加只分库的表名
     *
     * @param _tablesForDbSharding
     */
    public void addTablesForDbSharding(Set<String> _tablesForDbSharding) {
        this.tablesForDbSharding.addAll(_tablesForDbSharding);
        syncDbSharding();
    }

    public void addTablesForDbSharding(String _tablesForDbSharding) {
        this.tablesForDbSharding.add(_tablesForDbSharding);
        syncDbSharding();
    }

    /**
     * 添加只分库的表名
     *
     * @param _tablesForDbSharding
     */
    public void setTablesForDbSharding(Set<String> _tablesForDbSharding) {
        this.tablesForDbSharding = _tablesForDbSharding;
        syncDbSharding();
    }


    private void syncDbSharding() {
        tablesForDbSharding.addAll(tablesForTableSharding);
        logger.info("需要分库分表的表有:{}", tablesForTableSharding);
        logger.info("需要分库不分表的表有:{}", tablesForDbSharding);
    }

    /**
     * 是否是分表
     *
     * @param tableName
     * @return
     */
    public boolean isTableSharding(String tableName) {
        return tablesForTableSharding.contains(tableName);
    }

    /**
     * 是否是分库
     *
     * @param tableName
     * @return
     */
    public boolean isDbSharding(String tableName) {
        return tablesForDbSharding.contains(tableName);
    }

    public Set<String> getTablesForDbSharding() {
        return tablesForDbSharding;
    }

    public Set<String> getTablesForTableSharding() {
        return tablesForTableSharding;
    }
}
