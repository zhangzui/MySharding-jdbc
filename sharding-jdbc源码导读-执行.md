# sharding-jdbc源码阅读之执行原理
## 简介
sharding-jdbc的执行流程
#
```
 List<ResultSet> resultSets = new PreparedStatementExecutor(
                    getConnection().getShardingContext().getExecutorEngine(), routeResult.getSqlStatement().getType(), preparedStatementUnits, getParameters()).executeQuery();
1.构造PreparedStatementExecutor
2.executeQuery
```
##1.构造PreparedStatementExecutor
```
executorEngine执行引擎是在初始化配置的时候一并初始化的：
public ExecutorEngine(final int executorSize) {
        executorService = MoreExecutors.listeningDecorator(new ThreadPoolExecutor(
                executorSize, executorSize, 0, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(), new ThreadFactoryBuilder().setDaemon(true).setNameFormat("ShardingJDBC-%d").build()));
        MoreExecutors.addDelayedShutdownHook(executorService, 60, TimeUnit.SECONDS);
    }
这里执行executePreparedStatement
executorEngine.executePreparedStatement(sqlType, preparedStatementUnits, parameters, new ExecuteCallback<ResultSet>() {
            @Override
            public ResultSet execute(final BaseStatementUnit baseStatementUnit) throws Exception {
                return ((PreparedStatement) baseStatementUnit.getStatement()).executeQuery();
            }
        });

```
##2.SJ核心执行逻辑
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
##3.单个内部执行器：executeInternal
//这里通过回调函数，sqlStatement执行调用JDBC的PreparedStatement，并将结果返回
```
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

