package com.zz.db.route.mybatis;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.alibaba.druid.sql.ast.statement.SQLInsertStatement;
import com.alibaba.druid.sql.ast.statement.SQLSelectQueryBlock;
import com.alibaba.druid.sql.ast.statement.SQLUpdateStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlOutputVisitor;
import com.zz.db.route.common.DbContextHolder;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by zhangkai9@jd.com on 16-10-27.
 */
public class RenameTableNameVisitor extends MySqlOutputVisitor {
    static Logger logger = LoggerFactory.getLogger(RenameTableNameVisitor.class);
    private Set<String> renameTables = new HashSet<String>();
    private Set<String> tableNames = new HashSet<String>();

    /**
     * dryRun模式,空跑模式,不应用变更
     */
    private boolean dryRun = false;
    private boolean errGetTableIndex = false;
    /**
     * 是否走分库分表
     */
    private boolean isShardingDb = false;

    {
        this.setPrettyFormat(false);
    }

    private RenameTableNameVisitor(Appendable appender) {
        super(appender);
    }

    public RenameTableNameVisitor(Appendable appender, Set<String> renameTables, boolean dryRun) {
        super(appender);
        this.renameTables = renameTables;
        this.dryRun = dryRun;
    }

    /**
     * 转换tablename.
     *
     * @param x
     * @return
     */
    @Override
    public boolean visit(SQLExprTableSource x) {
        SQLExpr expr = x.getExpr();
        if (expr instanceof SQLIdentifierExpr) {

            SQLIdentifierExpr sqlIdentifierExpr = (SQLIdentifierExpr) expr;
            String tableWholeName = sqlIdentifierExpr.getName();
            tableNames.add(tableWholeName);
            if (renameTables.contains(tableWholeName.toLowerCase())) {
                isShardingDb = true;
                if (StringUtils.isBlank(DbContextHolder.getTableIndex())) {
                    // 需要获取tableIndex,但是没有获取到..需要检查配置
                    errGetTableIndex = true;
                }
                if (!dryRun) {
                    sqlIdentifierExpr.setName("`" + tableWholeName + DbContextHolder.getTableIndex() + "`");
                }
            } else {
                logger.debug("表:[{}]不需要转换.", tableWholeName);
            }

        } else {
            logger.warn("解析到的类型异常:" + expr.getClass() + ":" + expr.toString());
        }
        return super.visit(x);
    }

//    public boolean visit(MySqlSelectQueryBlock x) {
//        CheckRoutingKeyContext.getContext().setDmlType(CheckRoutingKeyVo.DMLType.SELECT);
//        return super.visit(x);
//    }
//
//    public boolean visit(SQLSelectQueryBlock x) {
//        CheckRoutingKeyContext.getContext().setDmlType(CheckRoutingKeyVo.DMLType.SELECT);
//        return super.visit(x);
//    }
//
//    public boolean visit(SQLInsertStatement x) {
//        CheckRoutingKeyContext.getContext().setDmlType(CheckRoutingKeyVo.DMLType.INSERT);
//        return super.visit(x);
//    }
//
//    public boolean visit(SQLUpdateStatement x) {
//        CheckRoutingKeyContext.getContext().setDmlType(CheckRoutingKeyVo.DMLType.UPDATE);
//        return super.visit(x);
//    }


    /**
     * 获取解析出来的表名
     *
     * @return
     */
    public Set<String> getTableNames() {
        return tableNames;
    }

    /**
     * 获取转换完的sql
     *
     * @return
     */
    public String getSql() {
        return super.getAppender().toString();
    }

    /**
     * 是否获取到了tableIndex
     *
     * @return
     */
    public boolean isErrGetTableIndex() {
        return errGetTableIndex;
    }

    public boolean isShardingDb() {
        return isShardingDb;
    }
}
