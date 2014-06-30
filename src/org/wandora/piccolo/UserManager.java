package org.wandora.piccolo;

import javax.servlet.ServletRequest;

/**
 *
 * @author olli
 */


public interface UserManager {
    public User getUser(ServletRequest request);
    public void updateUser(User user);
}
