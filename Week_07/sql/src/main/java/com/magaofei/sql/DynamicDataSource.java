package com.magaofei.sql;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.AbstractDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author magaofei
 * @date 2020/12/2
 */
public class DynamicDataSource extends AbstractRoutingDataSource {

    private static final Logger logger = LoggerFactory.getLogger(DynamicDataSource.class);

    @Override
    protected Object determineCurrentLookupKey() {
        String dataSourceId = DynamicDataSourceContextHolder.getDataSourceId();
        if (dataSourceId != null) { //有指定切换数据源切换的时候，才给输出日志 并且也只给输出成debug级别的 否则日志太多了
            logger.debug("线程[{}]，此时切换到的数据源为:{}", Thread.currentThread().getId(), dataSourceId);
        }
        return dataSourceId;
    }



}
