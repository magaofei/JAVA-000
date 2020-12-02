package com.magaofei.db;

import com.mysql.cj.conf.HostInfo;
import com.mysql.cj.jdbc.ConnectionImpl;

import java.sql.*;

/**
 * @author magaofei
 * @date 2020/11/22
 */
public class JdbcTemp {

    public static void main(String[] args) {


        try (Connection connection = new ConnectionImpl(hostInfo)){
            statement = connection.createStatement();
            statement.execute("insert into user(name) values(\"mark\")");
            statement.executeUpdate("update user set name = 'mark1'");
            statement.execute("delete from user where name = 'mark'");
            ResultSet resultSet = statement.executeQuery("select * from user");
            resultSet.getObject(0);
        } catch (SQLException e) {
            e.printStackTrace();
        }


    }
}
