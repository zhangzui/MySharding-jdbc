package com.zz.db.route.mybatis;

import com.zz.db.route.common.DbContextHolder;
import org.apache.ibatis.mapping.DatabaseIdProvider;
import org.apache.ibatis.mapping.VendorDatabaseIdProvider;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Properties;

/**
 * 用于解决动态数据源下,没有默认连接池的情况下,mybatis获取databaseId异常的问题.
 */
public class DbShardingVendorDatabaseIdProvider implements DatabaseIdProvider {
    private DatabaseIdProvider databaseIdProvider = new VendorDatabaseIdProvider();

    public DbShardingVendorDatabaseIdProvider() {
    }

    private String dbKey;

    @Override
    public void setProperties(Properties p) {
        databaseIdProvider.setProperties(p);
    }

    @Override
    public String getDatabaseId(DataSource dataSource) throws SQLException {
        boolean needClear = false;
        try {
            if (DbContextHolder.getDbKey() == null) {
                DbContextHolder.setDbKey(dbKey);
                needClear = true;
            }
            return databaseIdProvider.getDatabaseId(dataSource);
        } finally {
            if (needClear) {
                DbContextHolder.clearDbKey();
            }
        }
    }

    public String getDbKey() {
        return dbKey;
    }

    public void setDbKey(String dbKey) {
        this.dbKey = dbKey;
    }
}
