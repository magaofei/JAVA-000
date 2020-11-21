package com.magaofei;

/**
 * @author mark
 * @date 2020/11/21
 */
public class StudentFactory {

    public Student getStudent(String s) {
        return new Student();
    }
}
