package lk.ac.ruh.fot.ams.admin.dao;

import java.util.List;

import lk.ac.ruh.fot.ams.dao.BaseDAO;
import lk.ac.ruh.fot.ams.dao.CrudDAO;
import lk.ac.ruh.fot.ams.dao.NoticeDAO;
import lk.ac.ruh.fot.ams.exception.DatabaseException;
import lk.ac.ruh.fot.ams.model.Notice;

/**
 * [M1 - ADMIN MODULE] Create / edit / delete notices. The common NoticeBoardPanel
 * (used by Lecturer, TO and Undergraduate) shows the ACTIVE ones.
 *
 * OOP: Inheritance (BaseDAO), Abstraction/Generics (CrudDAO&lt;Notice&gt;);
 *      re-uses the common mapper NoticeDAO::mapNotice (method reference).
 */
public class AdminNoticeDAO extends BaseDAO implements CrudDAO<Notice> {

    @Override
    public List<Notice> findAll() throws DatabaseException {
        return queryList(NoticeDAO.BASE_SELECT + "ORDER BY n.created_at DESC, n.notice_id DESC", NoticeDAO::mapNotice);
    }

    @Override
    public Notice findById(int id) throws DatabaseException {
        return queryOne(NoticeDAO.BASE_SELECT + "WHERE n.notice_id = ?", NoticeDAO::mapNotice, id);
    }

    @Override
    public int insert(Notice n) throws DatabaseException {
        return executeInsert("INSERT INTO notices (title, description, created_by, target_role, status) VALUES (?, ?, ?, ?, ?)",
                n.getTitle(), n.getDescription(), n.getCreatedBy(), n.getTargetRole(), n.getStatus());
    }

    @Override
    public void update(Notice n) throws DatabaseException {
        executeUpdate("UPDATE notices SET title = ?, description = ?, target_role = ?, status = ? WHERE notice_id = ?",
                n.getTitle(), n.getDescription(), n.getTargetRole(), n.getStatus(), n.getNoticeId());
    }

    @Override
    public void delete(int id) throws DatabaseException {
        executeUpdate("DELETE FROM notices WHERE notice_id = ?", id);
    }

    /** ACTIVE <-> INACTIVE. */
    public String toggleStatus(Notice n) throws DatabaseException {
        String status = "ACTIVE".equals(n.getStatus()) ? "INACTIVE" : "ACTIVE";
        executeUpdate("UPDATE notices SET status = ? WHERE notice_id = ?", status, n.getNoticeId());
        return status;
    }

    public List<Notice> findRecent(int limit) throws DatabaseException {
        return queryList(NoticeDAO.BASE_SELECT + "ORDER BY n.created_at DESC LIMIT ?", NoticeDAO::mapNotice, limit);
    }
}
