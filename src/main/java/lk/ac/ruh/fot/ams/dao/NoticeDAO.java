package lk.ac.ruh.fot.ams.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import lk.ac.ruh.fot.ams.exception.DatabaseException;
import lk.ac.ruh.fot.ams.model.Notice;
import lk.ac.ruh.fot.ams.model.enums.NoticeTarget;
import lk.ac.ruh.fot.ams.model.enums.Role;

/** [COMMON PART] Reads notices (every role can see notices). */
public class NoticeDAO extends BaseDAO {

    public static final String BASE_SELECT =
            "SELECT n.*, p.full_name AS created_by_name FROM notices n "
            + "LEFT JOIN user_profiles p ON p.user_id = n.created_by ";

    public List<Notice> findAll() throws DatabaseException {
        return queryList(BASE_SELECT + "ORDER BY n.created_at DESC", NoticeDAO::mapNotice);
    }

    /** Active notices meant for everyone or for the given role. */
    public List<Notice> findActiveForRole(Role role) throws DatabaseException {
        if (role == Role.ADMIN) {
            return queryList(BASE_SELECT + "WHERE n.status = 'ACTIVE' ORDER BY n.created_at DESC", NoticeDAO::mapNotice);
        }
        return queryList(BASE_SELECT + "WHERE n.status = 'ACTIVE' AND (n.target_role = 'ALL' OR n.target_role = ?) "
                + "ORDER BY n.created_at DESC", NoticeDAO::mapNotice, role.name());
    }

    public static Notice mapNotice(ResultSet rs) throws SQLException {
        Notice n = new Notice();
        n.setNoticeId(rs.getInt("notice_id"));
        n.setTitle(rs.getString("title"));
        n.setDescription(rs.getString("description"));
        n.setCreatedBy(getInteger(rs, "created_by"));
        n.setCreatedByName(rs.getString("created_by_name"));
        n.setTargetRole(NoticeTarget.valueOf(rs.getString("target_role")));
        n.setCreatedAt(getDateTime(rs, "created_at"));
        n.setStatus(rs.getString("status"));
        return n;
    }
}
