# my-sharding-jdbc
#快速分库分表
## 简介
简单的基于Mysql的分库分表，快速分库分表组件：
## 思路
集成Mybatis+Spring
1、分片字段路由，将路有结果保存线程本地
2、利用Mybatis插件进行Sql重写；（依赖driue:sql解析工具）
3、执行sql，并处理结果集；
4、处理完毕，清除线程缓存和sql缓存信息
## 流程介绍
###
```

```
###dataSource.xml配置
```

```
###数据库
```

```
