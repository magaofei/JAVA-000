package com.magaofei.db;

import com.zaxxer.hikari.HikariDataSource;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * @author mark
 * @date 2020/11/22
 */
public class HikariDemo {
    public static void main(String[] args) throws SQLException {
        HikariDataSource hikariDataSource = new HikariDataSource();
        hikariDataSource.setJdbcUrl("jdbc:mysql://localhost:3306/simpsons");
        hikariDataSource.setUsername("bart");
        hikariDataSource.setPassword("51mp50n");

        PreparedStatement preparedStatement = hikariDataSource.getConnection().prepareStatement("select * from user where name = ?");
        preparedStatement.setString(1, "mark");
        System.out.println("" + preparedStatement.executeQuery());

    }
}
