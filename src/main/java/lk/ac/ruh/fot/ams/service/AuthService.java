package lk.ac.ruh.fot.ams.service;

import lk.ac.ruh.fot.ams.dao.UserDAO;
import lk.ac.ruh.fot.ams.exception.AuthenticationException;
import lk.ac.ruh.fot.ams.exception.DatabaseException;
import lk.ac.ruh.fot.ams.model.User;
import lk.ac.ruh.fot.ams.util.PasswordUtil;

/** [COMMON PART] Login logic. */
public class AuthService {

    private final UserDAO userDAO = new UserDAO();

    /**
     * @return the logged-in user (an Admin, Lecturer, TechnicalOfficer or Undergraduate object)
     * @throws AuthenticationException wrong username/password or inactive account
     * @throws DatabaseException       database problem
     */
    public User login(String username, String password) throws AuthenticationException, DatabaseException {
        if (username == null || username.isBlank() || password == null || password.isEmpty()) {
            throw new AuthenticationException("Please enter both username and password.");
        }
        User user = userDAO.findByCredentials(username.trim(), PasswordUtil.hash(password));
        if (user == null) {
            throw new AuthenticationException("Invalid username or password.");
        }
        if (!user.isActive()) {
            throw new AuthenticationException("This account is inactive. Please contact the administrator.");
        }
        return user;
    }
}
