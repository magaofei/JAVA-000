import com.magaofei.sql.DynamicDataSourceContextHolder;
import com.magaofei.sql.DynamicDataSourceId;
import com.magaofei.sql.JdbcConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Objects;

/**
 * @author magaofei
 * @date 2020/12/2
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {JdbcConfig.class})
public class TestSpringBean {


    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private DataSource dataSource;

    @Test
    public void test1() throws SQLException {
        System.out.println(jdbcTemplate.getDataSource() == dataSource); //true
        System.out.println(DataSourceUtils.getConnection(Objects.requireNonNull(jdbcTemplate.getDataSource()))); //com.mysql.jdbc.JDBC4Connection@17503f6b

        // 更改当前线程对应的 datasourceId
        DynamicDataSourceContextHolder.setDataSourceId(DynamicDataSourceId.SLAVE1);

        System.out.println(jdbcTemplate.getDataSource() == dataSource); //true
        // getConnection 去获取当前线程的 datasourceId
        System.out.println(DataSourceUtils.getConnection(jdbcTemplate.getDataSource())); //com.mysql.jdbc.JDBC4Connection@20bd8be5


        // 完成操作后  最好把数据源再set回去  否则可能会对该线程后续再使用JdbcTemplate的时候造成影响
        //DynamicDataSourceContextHolder.setDataSourceId(DynamicDataSourceId.MASTER);
    }
}
