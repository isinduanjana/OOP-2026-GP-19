package lk.ac.ruh.fot.ams.dao;

import java.util.List;

import lk.ac.ruh.fot.ams.exception.DatabaseException;

/**
 * [COMMON PART] Generic Create-Read-Update-Delete contract.
 *
 * OOP: Abstraction (interface) + Generics. The admin module (M1) implements
 * this for users, courses, notices and timetables, so one generic CRUD screen
 * can work with any of them (polymorphism).
 *
 * @param <T> entity type
 */
public interface CrudDAO<T> {

    List<T> findAll() throws DatabaseException;

    T findById(int id) throws DatabaseException;

    /** Inserts and returns the new primary key. */
    int insert(T entity) throws DatabaseException;

    void update(T entity) throws DatabaseException;

    void delete(int id) throws DatabaseException;
}
