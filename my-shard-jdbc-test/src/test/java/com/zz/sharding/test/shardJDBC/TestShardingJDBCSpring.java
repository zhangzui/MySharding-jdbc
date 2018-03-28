package com.zz.sharding.test.shardJDBC;

import com.zz.sharding.jdbc.bean.Order;
import com.zz.sharding.jdbc.dao.OrderMapper;
import io.shardingjdbc.core.api.ShardingDataSourceFactory;
import io.shardingjdbc.core.jdbc.core.datasource.ShardingDataSource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

/**
 * @author zhangzuizui
 * @date 2018/3/15 15:09
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:/shard-datasources-spring.xml"})
public class TestShardingJDBCSpring {

    @Autowired
    private ShardingDataSource shardingDataSource;

    @Test
    public void test(){
        String sql = "SELECT * FROM my_order WHERE order_id = ?";
        try {
            Connection conn = shardingDataSource.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, "001");
            //4.SQL执行和结果归并
            ResultSet rs = pstmt.executeQuery();
            //5.获取结果
            while(rs.next()) {
                System.out.println("id="+rs.getInt(1)+",order_id="+rs.getString(2));
            }
        }catch (Exception e){
          e.printStackTrace();
        }
    }
}
