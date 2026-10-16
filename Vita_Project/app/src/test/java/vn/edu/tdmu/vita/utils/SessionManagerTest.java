package vn.edu.tdmu.vita.utils;

import org.junit.Test;
import static org.junit.Assert.*;

public class SessionManagerTest {
    @Test
    public void notificationOwner_mustMatchPositiveActiveUser() {
        assertTrue(SessionManager.matchesUser(3, 3));
        assertFalse(SessionManager.matchesUser(3, 4));
        assertFalse(SessionManager.matchesUser(3, -1));
        assertFalse(SessionManager.matchesUser(-1, -1));
    }
}
