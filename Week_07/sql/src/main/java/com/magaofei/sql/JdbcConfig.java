package com.magaofei.sql;

import com.mysql.cj.jdbc.MysqlDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.TransactionManagementConfigurer;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * @author magaofei
 * @date 2020/12/2
 */
@EnableTransactionManagement
@Configuration
@PropertySource(value = "classpath:jdbc.properties", ignoreResourceNotFound = false, encoding = "UTF-8")
public class JdbcConfig implements TransactionManagementConfigurer {


    @Value("${datasource.username}")
    private String userName;
    @Value("${datasource.password}")
    private String password;
    @Value("${datasource.url}")
    private String url;

    // 从库配置
    @Value("${datasource.slave.username}")
    private String slaveUserName;
    @Value("${datasource.slave.password}")
    private String slavePassword;
    @Value("${datasource.slave.url}")
    private String slaveUrl;


    ////////////////=====配置好两个数据源：
    @Bean
    public DataSource masterDataSource() {
        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setUser(userName);
        dataSource.setPassword(password);
        dataSource.setURL(url);
        return dataSource;
    }

    @Bean
    public DataSource slaveDataSource() {
        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setUser(slaveUserName);
        dataSource.setPassword(slavePassword);
        dataSource.setURL(slaveUrl);
        return dataSource;
    }

    // 定义动态数据源
    @Primary
    @Bean
    public DataSource dataSource() {
        DynamicDataSource dataSource = new DynamicDataSource();
        // 初始化值必须设置进去  且给一个默认值
        Map<Object, Object> map = new HashMap<>();
        map.put(DynamicDataSourceId.MASTER, masterDataSource());
        map.put(DynamicDataSourceId.SLAVE1, slaveDataSource());

        //顺手注册上去，方便后续的判断
        DynamicDataSourceId.DATA_SOURCE_IDS.add(DynamicDataSourceId.MASTER);
        DynamicDataSourceId.DATA_SOURCE_IDS.add(DynamicDataSourceId.SLAVE1);

        dataSource.setTargetDataSources(map);

        dataSource.setDefaultTargetDataSource(masterDataSource());
        return dataSource;
    }

    @Bean
    public JdbcTemplate jdbcTemplate() {
        return new JdbcTemplate(dataSource());
    }

    @Bean
    public PlatformTransactionManager transactionManager() {
        DataSourceTransactionManager dataSourceTransactionManager = new DataSourceTransactionManager(dataSource());
        dataSourceTransactionManager.setEnforceReadOnly(true); // 让事务管理器进行只读事务层面上的优化  建议开启
        return dataSourceTransactionManager;
    }

    // 指定注解使用的事务管理器
    @Override
    public PlatformTransactionManager annotationDrivenTransactionManager() {
        return transactionManager();
    }
}
