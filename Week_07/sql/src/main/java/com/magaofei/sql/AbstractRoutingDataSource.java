package com.magaofei.sql;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.datasource.AbstractDataSource;
import org.springframework.jdbc.datasource.lookup.DataSourceLookup;
import org.springframework.jdbc.datasource.lookup.JndiDataSourceLookup;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author magaofei
 * @date 2020/12/2
 */
public abstract class AbstractRoutingDataSource extends AbstractDataSource implements InitializingBean {

    @Nullable
    private Map<Object, Object> targetDataSources;

    private Object defaultTargetDataSource; // 默认数据源

    private boolean lenientFallback = true;

    private DataSourceLookup dataSourceLookup = new JndiDataSourceLookup();
    @Nullable
    private Map<Object, DataSource> resolvedDataSources;
    @Nullable
    private DataSource resolvedDefaultDataSource;

    @Override
    public void afterPropertiesSet() {
        if (this.targetDataSources == null) {
            throw new IllegalArgumentException("Property 'targetDataSources' is required");
        }
        this.resolvedDataSources = new HashMap<>(this.targetDataSources.size());

        // 遍历设置进来的目标数据源们~~~~
        this.targetDataSources.forEach((key, value) -> {
            Object lookupKey = resolveSpecifiedLookupKey(key);
            DataSource dataSource = resolveSpecifiedDataSource(value);
            // 把已经解决好的缓存起来（注意key和value和上有可能就是不同的了）
            // 注意：key可以是个Object  而不一定只能是String类型
            this.resolvedDataSources.put(lookupKey, dataSource);
        });
        if (this.defaultTargetDataSource != null) {
            this.resolvedDefaultDataSource = resolveSpecifiedDataSource(this.defaultTargetDataSource);
        }
    }

    // 子类IsolationLevelDataSourceRouter有复写此方法
    // 绝大多数情况下，我们会直接使用DataSource
    protected Object resolveSpecifiedLookupKey(Object lookupKey) {
        return lookupKey;
    }
    // 此处兼容String类型，若是string就使用dataSourceLookup去查找（默认是JNDI）
    protected DataSource resolveSpecifiedDataSource(Object dataSource) throws IllegalArgumentException {
        if (dataSource instanceof DataSource) {
            return (DataSource) dataSource;
        } else if (dataSource instanceof String) {
            return this.dataSourceLookup.getDataSource((String) dataSource);
        } else {
            throw new IllegalArgumentException(
                    "Illegal data source value - only [javax.sql.DataSource] and String supported: " + dataSource);
        }
    }

    /////////////////////链接数据库
    @Override
    public Connection getConnection() throws SQLException {
        return determineTargetDataSource().getConnection();
    }
    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return determineTargetDataSource().getConnection(username, password);
    }
    protected DataSource determineTargetDataSource() {
        Assert.notNull(this.resolvedDataSources, "DataSource router not initialized");
        Object lookupKey = determineCurrentLookupKey();
        DataSource dataSource = this.resolvedDataSources.get(lookupKey);

        // 若根据key没有找到dataSource，并且lenientFallback=true或者lookupKey == null  那就回滚到使用默认的数据源
        // 备注：此处可以看出key=null和this.lenientFallback =true有一样的效果
        if (dataSource == null && (this.lenientFallback || lookupKey == null)) {
            dataSource = this.resolvedDefaultDataSource;
        }
        if (dataSource == null) {
            throw new IllegalStateException("Cannot determine target DataSource for lookup key [" + lookupKey + "]");
        }
        return dataSource;
    }

    // 子类必须实现的抽象方法：提供key即可~~~~
    @Nullable
    protected abstract Object determineCurrentLookupKey();



    @Override
    @SuppressWarnings("unchecked")
    public <T> T unwrap(Class<T> iface) throws SQLException {
        if (iface.isInstance(this)) {
            return (T) this;
        }
        return determineTargetDataSource().unwrap(iface);
    }
    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return (iface.isInstance(this) || determineTargetDataSource().isWrapperFor(iface));
    }

    @Nullable
    public Map<Object, Object> getTargetDataSources() {
        return targetDataSources;
    }

    public void setTargetDataSources(@Nullable Map<Object, Object> targetDataSources) {
        this.targetDataSources = targetDataSources;
    }

    public Object getDefaultTargetDataSource() {
        return defaultTargetDataSource;
    }

    public void setDefaultTargetDataSource(Object defaultTargetDataSource) {
        this.defaultTargetDataSource = defaultTargetDataSource;
    }

    public boolean isLenientFallback() {
        return lenientFallback;
    }

    public void setLenientFallback(boolean lenientFallback) {
        this.lenientFallback = lenientFallback;
    }

    public DataSourceLookup getDataSourceLookup() {
        return dataSourceLookup;
    }

    public void setDataSourceLookup(DataSourceLookup dataSourceLookup) {
        this.dataSourceLookup = dataSourceLookup;
    }

    @Nullable
    public Map<Object, DataSource> getResolvedDataSources() {
        return resolvedDataSources;
    }

    public void setResolvedDataSources(@Nullable Map<Object, DataSource> resolvedDataSources) {
        this.resolvedDataSources = resolvedDataSources;
    }

    @Nullable
    public DataSource getResolvedDefaultDataSource() {
        return resolvedDefaultDataSource;
    }

    public void setResolvedDefaultDataSource(@Nullable DataSource resolvedDefaultDataSource) {
        this.resolvedDefaultDataSource = resolvedDefaultDataSource;
    }
}
