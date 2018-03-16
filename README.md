# my-sharding-jdbc
#快速分库分表
## 简介
​	 基于当当的分库分表组件，一个接入的demo，另提供一个轻量级的快速分库分表的组件；

## 使用示例
my-shard-jdbc-test：Spring+Mybatis +sharding-jdbc分库分表的示例；

my-db-route：轻量级的快速分库分表的组件，欢迎大家一起讨论和设计；


## sharding-jdbc 使用
###sharding-jdbc-core MAVEN 坐标
```
<dependency>
    <groupId>com.dangdang</groupId>
    <artifactId>sharding-jdbc-core</artifactId>
    <version>2.0.2</version>
</dependency>
```
###dataSource.xml配置信息
```
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xmlns:tx="http://www.springframework.org/schema/tx" xmlns:rdb="http://www.dangdang.com/schema/ddframe/rdb"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
                        http://www.springframework.org/schema/beans/spring-beans.xsd
                        http://www.springframework.org/schema/tx
                        http://www.springframework.org/schema/tx/spring-tx.xsd
                        http://www.springframework.org/schema/context
                        http://www.springframework.org/schema/context/spring-context.xsd http://www.dangdang.com/schema/ddframe/rdb http://www.dangdang.com/schema/ddframe/rdb/rdb.xsd">

    <context:component-scan base-package="com.zz.*"/>

    <bean id="propertiesFactoryBean"
          class="org.springframework.beans.factory.config.PropertiesFactoryBean">
        <property name="locations">
            <list>
                <value>classpath:properties/jdbc.properties</value>
            </list>
        </property>
    </bean>

    <context:property-placeholder properties-ref="propertiesFactoryBean" ignore-unresolvable="true"/>

    <!--数据源0-->
    <!--<bean id="ds_1" parent="parentDataSource">-->
    <bean id="my-shard-01" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
        <property name="driverClassName" value="${jdbc-driver}"/>
        <property name="url" value="${jdbc-url-1}"/>
        <property name="username" value="${jdbc-user-1}"/>
        <property name="password" value="${jdbc-password-1}"/>
    </bean>

    <!--数据源1-->
    <bean id="my-shard-02" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
        <property name="driverClassName" value="${jdbc-driver}"/>
        <property name="url" value="${jdbc-url-2}"/>
        <property name="username" value="${jdbc-user-2}"/>
        <property name="password" value="${jdbc-password-2}"/>
    </bean>

    <!-- 路由表、路由库规则配置，可以指定algorithm-class专门自己实现具体的路由规则，也可以使用表达式-->
    <rdb:strategy id="userDatabaseShardingStrategy" sharding-columns="order_id" algorithm-class="com.zz.sharding.jdbc.algorithm.ModuloDatabaseShardingAlgorithm"/>

    <rdb:strategy id="userTableShardingStrategy" sharding-columns="order_id" algorithm-class="com.zz.sharding.jdbc.algorithm.ModuloTableShardingAlgorithm"/>

    <!-- 使用表达式方式 -->
    <!--<rdb:strategy id="studentDatabaseShardingStrategy" sharding-columns="order_id" algorithm-expression="sharding_${student_id.longValue() % 2}"/>-->

    <!--<rdb:strategy id="studentTableShardingStrategy" sharding-columns="loan_id" algorithm-expression="t_student_${student_id.longValue() % 2}"/>-->

    <rdb:data-source id="shardingDataSource">
        <rdb:sharding-rule data-sources="my-shard-01,my-shard-02">
            <rdb:table-rules>
                <rdb:table-rule logic-table="my_order" actual-tables="my-shard-0${1..2}.my_order_00${1..2}" database-strategy="userDatabaseShardingStrategy" table-strategy="userTableShardingStrategy"/>
               <!-- <rdb:table-rule logic-table="my_loan" actual-tables="my_shard_${0..1}.my_loan_${0..2}" database-strategy="studentDatabaseShardingStrategy" table-strategy="studentTableShardingStrategy"/>-->
            </rdb:table-rules>
            <!--<rdb:default-database-strategy sharding-columns="none" algorithm-class="com.dangdang.ddframe.rdb.sharding.api.strategy.database.NoneDatabaseShardingAlgorithm"/>-->
            <!--<rdb:default-table-strategy sharding-columns="none" algorithm-class="com.dangdang.ddframe.rdb.sharding.api.strategy.table.NoneTableShardingAlgorithm"/>-->
        </rdb:sharding-rule>
        <rdb:props>
            <prop key="metrics.enable">false</prop>
        </rdb:props>
    </rdb:data-source>

    <!--mybatis配置-->
    <bean id="sqlSessionFactory" class="org.mybatis.spring.SqlSessionFactoryBean">
        <property name="dataSource" ref="shardingDataSource"/>
        <property name="mapperLocations" value="classpath:mappers/OrderMapper.xml"/>
    </bean>

    <bean class="org.mybatis.spring.mapper.MapperScannerConfigurer">
        <property name="basePackage" value="com.zz.sharding.jdbc.dao"/>
        <property name="sqlSessionFactoryBeanName" value="sqlSessionFactory"/>
    </bean>

</beans>
```
###数据库
```
jdbc-driver=com.mysql.jdbc.Driver

jdbc-url-1=jdbc:mysql://localhost:3306/my-shard-01?useUnicode=true&characterEncoding=utf8&serverTimezone=UTC
jdbc-user-1=root
jdbc-password-1=123456

jdbc-url-2=jdbc:mysql://localhost:3306/my-shard-02?useUnicode=true&characterEncoding=utf8&serverTimezone=UTC
jdbc-user-2=root
jdbc-password-2=123456
```
