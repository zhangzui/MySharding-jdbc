package com.zz.db.route.common;

import java.util.Set;

/**
 * 动态数据源实现中KEY的存放工具类
 *
 * @author lvjun
 * @ClassName: DbContextHolder
 * @Description: 动态数据源实现中KEY的存放工具类：使用treadLocal的方式来保证线程安全
 * @date 2012-3-13 下午01:41:35
 */
public class DbContextHolder {
    private static final ThreadLocal<String> dbKeyHolder = new ThreadLocal<String>();
    private static final ThreadLocal<String> tableIndexHolder = new ThreadLocal<String>();
    /**
     * 当前操作的表,为动态数据源的判断使用.
     */
    private static final ThreadLocal<Set<String>> currTableHolder = new ThreadLocal<Set<String>>();


    public static void setDbKey(String dbKey) {
        dbKeyHolder.set(dbKey);
    }

    public static String getDbKey() {
        return (String) dbKeyHolder.get();
    }

    public static void clearDbKey() {
        dbKeyHolder.remove();
    }

    public static void setTableIndex(String tableIndex) {
        tableIndexHolder.set(tableIndex);
    }

    public static String getTableIndex() {
        return (String) tableIndexHolder.get();
    }

    public static void clearTableIndex() {
        tableIndexHolder.remove();
    }

    public static void setCurrTable(Set<String> currTable) {
        currTableHolder.set(currTable);
    }

    public static Set<String> getCurrTable() {
        return currTableHolder.get();
    }

    public static void clearCurrTable() {
        currTableHolder.remove();
    }

}