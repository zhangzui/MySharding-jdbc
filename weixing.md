最近在研究分库分表，之前业务也有过分库分表，依赖Mybatis插件来实现的，感觉很鸡肋。最近研究了一下市面上的分库分表的组件，对Sharding-JDBC简单的研究了一下，源码阅读和与作者简单的沟通，当前已经很多公司已经在用Sharding-JDBC，很多功能都已经很成熟。接下来通过SJ的文档一个简单的demo入手，看一下简单的查询一个分库分表是如何实现的。
#DEMO示例
git地址：https://github.com/zhangzui/my-sharding-jdbc：其中有几个简单的示例：简单的JDBC分库分表查询，集成Spring和Mybatis-Mapper的例子
```
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
        pstmt.setString(1, "000001");
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
```
#Sharding-JDBC源码简单的流程梳理
##一.初始化配置文件
```
1.创建数据源：通过ShardingDataSourceFactory创建datasource，会初始化下面几个重要的对象：
    a.执行引擎：executorEngine，这里会初始化一个线程池;
    b.一些配置信息：是否打印sql，showSQL=true，和一些shardingProperties;
    c.ShardingContext：shardingRule规则，数据库类型：getDatabaseType，和上面的执行引擎等参数;
2.获取连接：getConnect：返回一个new ShardingConnection(this.shardingContext);
```
##二.sql预处理
```
1.new PreparedStatementRoutingEngine();
2.构造
public PreparedStatementRoutingEngine(String logicSQL, ShardingContext shardingContext) {
        this.logicSQL = logicSQL;
        this.sqlRouter = SQLRouterFactory.createSQLRouter(shardingContext);
    }
3.根据isDatabaseShardingOnly判断构造那个sqlRouter；
(SQLRouter)(HintManagerHolder.isDatabaseShardingOnly() ? new DatabaseHintSQLRouter(shardingContext) : new ParsingSQLRouter(shardingContext));
    a.提示sqlRouter:DatabaseHintSQLRouter
    b.解释sqlRouter:ParsingSQLRouter
```
##三.Sharding-JDBC执行
里边干了很多事，首先是sql解析和路由，然后再executeQuery，最后处理结果集
```
public ResultSet executeQuery() throws SQLException {
        ShardingResultSet result;
        try {
            Collection<PreparedStatementUnit> preparedStatementUnits = this.route();
            List<ResultSet> resultSets = (new PreparedStatementExecutor(this.getConnection().getShardingContext().getExecutorEngine(), this.routeResult.getSqlStatement().getType(), preparedStatementUnits, this.getParameters())).executeQuery();
            result = new ShardingResultSet(resultSets, (new MergeEngine(resultSets, (SelectStatement)this.routeResult.getSqlStatement())).merge(), this);
        } finally {
            this.clearBatch();
        }

        this.currentResultSet = result;
        return result;
    }
```
###1.sql解析
```
  //首先加载Mysql的字典目录，里边都是定义好的Mysql相关的关键字枚举;
  //再根据lexerEngine.nextToken()获取不同的token（就是一些关键字）然后走到不同的parse流程，
  //最后处理获取结果SQLStatement->SelectStatement
  public SQLRouteResult route(List<Object> parameters) {
        if (null == this.sqlStatement) {
            this.sqlStatement = this.sqlRouter.parse(this.logicSQL, parameters.size());
        }

        return this.sqlRouter.route(this.logicSQL, parameters, this.sqlStatement);
    }
 //解析是基于LexerEngine, lexerEngine.nextToken();下面是select的解析过程：
 protected void parseInternal(SelectStatement selectStatement) {
        this.parseDistinct();
        this.parseSelectOption();
        this.parseSelectList(selectStatement, this.getItems());
        this.parseFrom(selectStatement);
        this.parseWhere(this.getShardingRule(), selectStatement, this.getItems());
        this.parseGroupBy(selectStatement);
        this.parseHaving();
        this.parseOrderBy(selectStatement);
        this.parseLimit(selectStatement);
        this.parseSelectRest();
    }
```
###2.获取路由库和表结果,这里将路由库的流程贴一下，首先获取配置的分库策略：
```
private Collection<String> routeDataSources(TableRule tableRule, List<ShardingValue> databaseShardingValues) {
        Collection<String> availableTargetDatabases = tableRule.getActualDatasourceNames();
        if (databaseShardingValues.isEmpty()) {
            return availableTargetDatabases;
        } else {
            Collection<String> result = this.shardingRule.getDatabaseShardingStrategy(tableRule).doSharding(availableTargetDatabases, databaseShardingValues);
            Preconditions.checkState(!result.isEmpty(), "no database route info");
            return result;
        }
    }
```
###3.然后进行分库操作，将一个表达式和路由字段value交给groovy代理对象执行类调用call()，然后将路由结果返回：
```
public Collection<String> doSharding(Collection<String> availableTargetNames, Collection<ShardingValue> shardingValues) {
        ShardingValue shardingValue = (ShardingValue)shardingValues.iterator().next();
        Preconditions.checkState(shardingValue instanceof ListShardingValue, "Inline strategy cannot support range sharding.");
        Collection<String> shardingResult = this.doSharding((ListShardingValue)shardingValue);
        Collection<String> result = new TreeSet(String.CASE_INSENSITIVE_ORDER);
        result.addAll(shardingResult);
        return result;
    }

    private Collection<String> doSharding(ListShardingValue shardingValue) {
        Collection<String> result = new LinkedList();
        Iterator i$ = this.transferToPreciseShardingValues(shardingValue).iterator();

        while(i$.hasNext()) {
            PreciseShardingValue<?> each = (PreciseShardingValue)i$.next();
            result.add(this.execute(each));
        }

        return result;
    }
private String execute(PreciseShardingValue shardingValue) {
        Closure<?> result = this.closure.rehydrate(new Expando(), (Object)null, (Object)null);
        result.setResolveStrategy(3);
        result.setProperty(shardingValue.getColumnName(), shardingValue.getValue());
        return result.call().toString();
    }
```
###4.执行路由sql，进行数据库操作：
1.JDBC常规流程：
```
List<ResultSet> resultSets = new PreparedStatementExecutor(
                    getConnection().getShardingContext().getExecutorEngine(), routeResult.getSqlStatement().getType(), preparedStatementUnits, getParameters()).executeQuery();
```
2.核心执行逻辑
```
private  <T> List<T> execute(
            final SQLType sqlType, final Collection<? extends BaseStatementUnit> baseStatementUnits,
            final List<List<Object>> parameterSets, final ExecuteCallback<T> executeCallback) throws SQLException {
        if (baseStatementUnits.isEmpty()) {
            return Collections.emptyList();
        }
        OverallExecutionEvent event = new OverallExecutionEvent(sqlType, baseStatementUnits.size());
        EventBusInstance.getInstance().post(event);
        Iterator<? extends BaseStatementUnit> iterator = baseStatementUnits.iterator();
        BaseStatementUnit firstInput = iterator.next();
        //首先异步线程池执行
        ListenableFuture<List<T>> restFutures = asyncExecute(sqlType, Lists.newArrayList(iterator), parameterSets, executeCallback);
        T firstOutput;
        List<T> restOutputs;
        try {
            //这里将第一个Statement同步执行，就不再开线程进行多线程处理了，避免多开线程，和线程切换，带来资源浪费
            firstOutput = syncExecute(sqlType, firstInput, parameterSets, executeCallback);
            restOutputs = restFutures.get();
            //CHECKSTYLE:OFF
        } catch (final Exception ex) {
            //CHECKSTYLE:ON
            event.setException(ex);
            event.setEventExecutionType(EventExecutionType.EXECUTE_FAILURE);
            EventBusInstance.getInstance().post(event);
            ExecutorExceptionHandler.handleException(ex);
            return null;
        }
        event.setEventExecutionType(EventExecutionType.EXECUTE_SUCCESS);
        EventBusInstance.getInstance().post(event);
        List<T> result = Lists.newLinkedList(restOutputs);
        //最后合并结果，将同步的和异步的合并返回
        result.add(0, firstOutput);
        return result;
    }
```
3.内部执行器
```
 //executeInternal,这里通过回调函数，sqlStatement执行调用JDBC的PreparedStatement，并将结果返回
 private <T> T executeInternal(final SQLType sqlType, final BaseStatementUnit baseStatementUnit, final List<List<Object>> parameterSets, final ExecuteCallback<T> executeCallback,
                          final boolean isExceptionThrown, final Map<String, Object> dataMap) throws Exception {
        synchronized (baseStatementUnit.getStatement().getConnection()) {
            T result;
            ExecutorExceptionHandler.setExceptionThrown(isExceptionThrown);
            ExecutorDataMap.setDataMap(dataMap);
            List<AbstractExecutionEvent> events = new LinkedList<>();
            if (parameterSets.isEmpty()) {
                events.add(getExecutionEvent(sqlType, baseStatementUnit, Collections.emptyList()));
            }
            for (List<Object> each : parameterSets) {
                events.add(getExecutionEvent(sqlType, baseStatementUnit, each));
            }
            for (AbstractExecutionEvent event : events) {
                EventBusInstance.getInstance().post(event);
            }
            try {
                //这里通过回调函数，执行sqlStatement
                result = executeCallback.execute(baseStatementUnit);
            } catch (final SQLException ex) {
                for (AbstractExecutionEvent each : events) {
                    each.setEventExecutionType(EventExecutionType.EXECUTE_FAILURE);
                    each.setException(ex);
                    EventBusInstance.getInstance().post(each);
                    ExecutorExceptionHandler.handleException(ex);
                }
                return null;
            }
            for (AbstractExecutionEvent each : events) {
                each.setEventExecutionType(EventExecutionType.EXECUTE_SUCCESS);
                EventBusInstance.getInstance().post(each);
            }
            return result;
        }
    }
```
4.这里简单的说一下集成Mybaitis时，在queryFromDatabase处，从SharingConnect创建PreparedStatement->ShardingPreparedStatement
所有ps.execute();这里就会走到Sharding-jdbc中来，进行分库分表的数据库操作，如果没有，Mybatis就直接会调用JDBC的executor。
```
public <E> List<E> query(Statement statement, ResultHandler resultHandler) throws SQLException {
        PreparedStatement ps = (PreparedStatement)statement;
        ps.execute();
        return this.resultSetHandler.handleResultSets(ps);
    }
```
##四.结果并归：
```
result = new ShardingResultSet(resultSets, (new MergeEngine(resultSets, (SelectStatement)this.routeResult.getSqlStatement())).merge(), this);
```