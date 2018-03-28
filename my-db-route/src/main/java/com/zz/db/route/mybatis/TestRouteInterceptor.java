package com.zz.db.route.mybatis;


import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.parser.SQLParserUtils;
import com.alibaba.druid.sql.parser.SQLStatementParser;
import com.alibaba.druid.util.JdbcUtils;
import com.zz.db.route.common.DbContextHolder;
import com.zz.db.route.dbroute.DbShardingConfig;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.statement.RoutingStatementHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.ParameterMode;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * mybatis 插件 拦截器
 * 进行sql重写
 * @author zhangzuizui
 * @date 2018/3/28 20:26
 */
@Intercepts({@Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class}),
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class,
                ResultHandler.class, CacheKey.class, BoundSql.class}),

        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class,
                ResultHandler.class}),

        @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class})
})
public class TestRouteInterceptor implements Interceptor {

    private static Field sqlField;
    private static Field delegateField;

    static Logger logger = LoggerFactory.getLogger(TestRouteInterceptor.class);

    private ThreadLocal<String> sqlCache = new ThreadLocal<String>();

    /**
     * 需要分库分表的表名
     */
    private Set<String> tablesForTableSharding = new HashSet<String>();

    static {
        try {
            sqlField = BoundSql.class.getDeclaredField("sql");
            sqlField.setAccessible(true);
            delegateField = RoutingStatementHandler.class.getDeclaredField("delegate");
            delegateField.setAccessible(true);
            started = true;
        } catch (NoSuchFieldException e) {
            logger.error("", e);
            started = false;
        }
    }
    /**
     * 是否开启异步验证切分键的逻辑
     */
    private boolean asyncCheckRoutingKeyOpen = true;
    private static volatile boolean started = false;

    /**
     * dryRun模式,空跑模式,不应用变更
     */
    private boolean dryRun = false;

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
//        SQLStatement sqlStatement = null;
//        MappedStatement mappedStatement = null;
//        if (invocation.getTarget() instanceof Executor) {
//            String sqlId = "";
//            try {
//                mappedStatement = (MappedStatement) invocation.getArgs()[0];
//                BoundSql boundSql = buildBoundSql(invocation, mappedStatement);
//                sqlId = mappedStatement.getId();
//                if (boundSql == null) {
//                    throw new IllegalStateException("获取boundSql失败");
//                }
//                String orgSql = boundSql.getSql().replaceAll("[\\s]+", " ");
//
//                SQLStatementParser sqlStatementParser = SQLParserUtils.createSQLStatementParser(orgSql, JdbcUtils.MYSQL);
//                List<SQLStatement> statementList = sqlStatementParser.parseStatementList();
//                if (statementList.size() == 1) {
//                    StringBuilder out = new StringBuilder();
//                    RenameTableNameVisitor visitor = new RenameTableNameVisitor(out, DbShardingConfig.getInst().getTablesForTableSharding(), dryRun);
//                    sqlStatement = statementList.get(0);
//                    sqlStatement.accept(visitor);
//
//                    if (visitor.isErrGetTableIndex()) {
//                        //应该有tableIndex,却没有,需要检查程序/配置
//                        //Profiler.businessAlarm("getTableIndexErr_" + Utils.fetchAppCodeFromContext() + "_" + sqlId, "获取tableIndex失败,请检查程序配置.");
//                        //logger.warn("获取tableIndex失败,请检查程序配置:{}_{}", Utils.fetchAppCodeFromContext(), sqlId);
//                    }
//                    sqlCache.set(out.toString());
//                    DbContextHolder.setCurrTable(visitor.getTableNames());
//                } else {
//                    //Profiler.businessAlarm("db_sharing_parse_" + sqlId, orgSql);
//                    logger.warn("解析的sql有问题:{}", orgSql);
//                }
//            } catch (Exception e) {
//                //Profiler.businessAlarm("db_sharing_exception1_" + sqlId, "转换表名异常(忽略处理,保证业务正常运行):" + e.getMessage());
//                logger.warn("转换表名异常(忽略处理,保证业务正常运行):", e);
//                sqlCache.remove();
//            }
//
//        } else if (invocation.getTarget() instanceof RoutingStatementHandler) {
//            try {
//                RoutingStatementHandler statementHandler = (RoutingStatementHandler) invocation.getTarget();
//                StatementHandler delegate = (StatementHandler) ReflectionUtils.getField(delegateField, statementHandler);
//                BoundSql boundSql = delegate.getBoundSql();
//                String newSql = sqlCache.get();
//                logger.debug("org:{},new:{}", boundSql.getSql().replaceAll("[\\s]+", " "), newSql);
//                ReflectionUtils.setField(sqlField, boundSql, newSql);
//            } catch (Exception e) {
//                //Profiler.businessAlarm("db_sharing_exception2", "转换表名异常(忽略处理,保证业务正常运行):" + e.getMessage());
//                logger.warn("转换表名异常(忽略处理,保证业务正常运行):", e);
//            }
//        } else {
//            String name = invocation.getTarget().getClass().getName();
//            //Profiler.businessAlarm("db_sharing", "目标对象类型是" + name + ",而不是RoutingStatementHandler.");
//            logger.warn("目标对象类型是{},而不是RoutingStatementHandler? bug", name);
//        }
//
//        Object proceed = null;
//        try {
//            proceed = invocation.proceed();
//            return proceed;
//        } finally {
//            if (invocation.getTarget() instanceof Executor) {
//                if (asyncCheckRoutingKeyOpen) {
//                    //asyncCheckRoutingKey(invocation, mappedStatement, proceed);
//                    System.out.println("xxxxxxxxxxxxxxx");
//                }
//                sqlCache.remove();
//                DbContextHolder.clearCurrTable();
//            }
//        }

        SQLStatement sqlStatement = null;
        MappedStatement mappedStatement = null;
        if (invocation.getTarget() instanceof Executor) {
            String sqlId = "";
            try {
                mappedStatement = (MappedStatement) invocation.getArgs()[0];
                BoundSql boundSql = buildBoundSql(invocation, mappedStatement);
                sqlId = mappedStatement.getId();
                if (boundSql == null) {
                    throw new IllegalStateException("获取boundSql失败");
                }
                String orgSql = boundSql.getSql().replaceAll("[\\s]+", " ");

                SQLStatementParser sqlStatementParser = SQLParserUtils.createSQLStatementParser(orgSql, JdbcUtils.MYSQL);
                List<SQLStatement> statementList = sqlStatementParser.parseStatementList();
                if (statementList.size() == 1) {
                    StringBuilder out = new StringBuilder();
                    RenameTableNameVisitor visitor = new RenameTableNameVisitor(out, DbShardingConfig.getInst().getTablesForTableSharding(), dryRun);
                    sqlStatement = statementList.get(0);
                    sqlStatement.accept(visitor);

                    if (visitor.isErrGetTableIndex()) {
                        //应该有tableIndex,却没有,需要检查程序/配置
                        //Profiler.businessAlarm("getTableIndexErr_" + Utils.fetchAppCodeFromContext() + "_" + sqlId, "获取tableIndex失败,请检查程序配置.");
                        //logger.warn("获取tableIndex失败,请检查程序配置:{}_{}", Utils.fetchAppCodeFromContext(), sqlId);
                    }
                    sqlCache.set(out.toString());
                    DbContextHolder.setCurrTable(visitor.getTableNames());
                } else {
                    //Profiler.businessAlarm("db_sharing_parse_" + sqlId, orgSql);
                    logger.warn("解析的sql有问题:{}", orgSql);
                }
            } catch (Exception e) {
                //Profiler.businessAlarm("db_sharing_exception1_" + sqlId, "转换表名异常(忽略处理,保证业务正常运行):" + e.getMessage());
                logger.warn("转换表名异常(忽略处理,保证业务正常运行):", e);
                sqlCache.remove();
            }

        } else if (invocation.getTarget() instanceof RoutingStatementHandler) {
            try {
                RoutingStatementHandler statementHandler = (RoutingStatementHandler) invocation.getTarget();
                StatementHandler delegate = (StatementHandler) ReflectionUtils.getField(delegateField, statementHandler);
                BoundSql boundSql = delegate.getBoundSql();
                String newSql = sqlCache.get();
                logger.debug("org:{},new:{}", boundSql.getSql().replaceAll("[\\s]+", " "), newSql);
                ReflectionUtils.setField(sqlField, boundSql, newSql);
            } catch (Exception e) {
                //Profiler.businessAlarm("db_sharing_exception2", "转换表名异常(忽略处理,保证业务正常运行):" + e.getMessage());
                logger.warn("转换表名异常(忽略处理,保证业务正常运行):", e);
            }
        } else {
            String name = invocation.getTarget().getClass().getName();
            //Profiler.businessAlarm("db_sharing", "目标对象类型是" + name + ",而不是RoutingStatementHandler.");
            logger.warn("目标对象类型是{},而不是RoutingStatementHandler? bug", name);
        }

        Object proceed = null;
        try {
            proceed = invocation.proceed();
            return proceed;
        } finally {
            if (invocation.getTarget() instanceof Executor) {
                if (asyncCheckRoutingKeyOpen) {
                    //asyncCheckRoutingKey(invocation, mappedStatement, proceed);
                    System.out.println("xxxxxxxxxxxxxxx");
                }
                sqlCache.remove();
                DbContextHolder.clearCurrTable();
            }
        }
    }


    @Override
    public Object plugin(Object target) {
        if (target instanceof Executor || target instanceof StatementHandler) {
            return Plugin.wrap(target, this);
        }else {
            return target;
        }
    }

    @Override
    public void setProperties(Properties properties) {

    }
    private BoundSql buildBoundSql(Invocation invocation, MappedStatement mappedStatement) {
        Object[] args = invocation.getArgs();
        BoundSql boundSql = null;
        if (args.length == 6) {//select
            boundSql = (BoundSql) args[5];
        } else if (args.length == 4) {// select
            boundSql = mappedStatement.getBoundSql(args[1]);
        } else if (args.length == 2) { //update
            boundSql = mappedStatement.getBoundSql(args[1]);
        }
        return boundSql;
    }
}
