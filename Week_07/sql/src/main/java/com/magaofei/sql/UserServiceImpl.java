package com.magaofei.sql;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * @author magaofei
 * @date 2020/12/2
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public Object getUser(String dataSource) {
        if (dataSource.equals("master")) {
            DynamicDataSourceContextHolder.setDataSourceId(DynamicDataSourceId.MASTER);
        } else if (dataSource.equals("slave1")) {
            DynamicDataSourceContextHolder.setDataSourceId(DynamicDataSourceId.SLAVE1);
        }
        return jdbcTemplate.queryForList("select * from user limit 1");
    }
}
