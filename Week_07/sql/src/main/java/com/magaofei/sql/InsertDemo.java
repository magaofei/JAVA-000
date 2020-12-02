package com.magaofei.sql;

import com.mysql.cj.conf.DatabaseUrlContainer;
import com.mysql.cj.conf.HostInfo;
import com.mysql.cj.jdbc.ConnectionImpl;

import java.sql.*;

/**
 * @author magaofei
 * @date 2020/12/1
 */
public class InsertDemo {

    public static void main(String[] args) {
        insertBatch();
    }

    /**
     * 34582614
     */
    public static void insert() {

        long start = System.currentTimeMillis();
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/shop", "root", "12345678")){
            for (int i = 0; i < 1000000; i++) {

                String sql = "insert into shop.user(username, `name`, email, phone, address, password, create_time, update_time) value(?, ?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setString(1, "mark3");
                preparedStatement.setString(2, "mark");
                preparedStatement.setString(3, "markx#gmail.com");
                preparedStatement.setString(4, "1111111111");
                preparedStatement.setString(5, "asdasdsadsds");
                preparedStatement.setString(6, "123456");
                preparedStatement.setDate(7, new Date(System.currentTimeMillis()));
                preparedStatement.setDate(8, new Date(System.currentTimeMillis()));
                preparedStatement.executeUpdate();

//                int rows = preparedStatement.executeUpdate();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("time = " + String.valueOf(System.currentTimeMillis() - start));

    }

    /**
     * 223802
     */
    public static void insertBatch() {

        long start = System.currentTimeMillis();

        String sql = "insert into shop.user(username, `name`, email, phone, address, password, create_time, update_time) value(?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/shop", "root", "12345678")){
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            for (int i = 0; i < 1000000; i++) {

                preparedStatement.setString(1, "mark3");
                preparedStatement.setString(2, "mark");
                preparedStatement.setString(3, "markx#gmail.com");
                preparedStatement.setString(4, "1111111111");
                preparedStatement.setString(5, "asdasdsadsds");
                preparedStatement.setString(6, "123456");
                preparedStatement.setDate(7, new Date(System.currentTimeMillis()));
                preparedStatement.setDate(8, new Date(System.currentTimeMillis()));
                preparedStatement.addBatch();
//                int rows = preparedStatement.executeUpdate();
            }

            preparedStatement.executeLargeBatch();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("time = " + String.valueOf(System.currentTimeMillis() - start));

    }
}
