package hy.tmc.core.configuration;

import com.google.common.base.Optional;
import hy.tmc.core.domain.Course;
import hy.tmc.core.exceptions.ProtocolException;
import hy.tmc.core.zipping.DefaultRootDetector;
import hy.tmc.core.zipping.ProjectRootFinder;
import hy.tmc.core.zipping.RootFinder;
import java.io.IOException;

/**
 * This class will be initialized when Auth is successful. Use this to get data of user.
 */
public final class ClientData {

    private static int PID;
    private static String USERNAME = "";
    private static String PASSWORD = "";
    private static RootFinder rootFinder;

    private ClientData() {
    }

    /**
     * Sets the data for current user.
     *
     * @param username of the current user
     * @param password of the current user
     */
    public synchronized static void setUserData(String username, String password) {
        if (username == null) {
            USERNAME = ""; 
        }
        if (password == null) {
            PASSWORD = "";
        }
        USERNAME = username;
        PASSWORD = password;
    }

    private synchronized static RootFinder getProjectRootFinder() {
        if (rootFinder == null) {
            rootFinder = new ProjectRootFinder(new DefaultRootDetector());
        }
        return rootFinder;
    }

    /**
     * Overrides the rootfinder which is used by getCurrentCourse-method.
     * If nothing is set, the DefaultRootDetector will be used.
     */
    public synchronized static void setProjectRootFinder(RootFinder rootFinder) {
        ClientData.rootFinder = rootFinder;
    }

    /**
     * Finds the current course from the path using RootFinder-class.
     * If no RootFinder is set while using this command, the DefaultRootDetector will be used.
     * @param currentPath path to navigate through.
     * @return optional which includes the course if found.
     */
    public synchronized static Optional<Course> getCurrentCourse(String currentPath) throws ProtocolException, IOException {
        if (!userDataExists()) {
            throw new ProtocolException("Not logged in.");
        }
        return getProjectRootFinder().getCurrentCourse(currentPath);
    }

    public synchronized static boolean userDataExists() {
        return !(USERNAME.isEmpty() || PASSWORD.isEmpty());
    }

    public synchronized static void clearUserData() {
        USERNAME = "";
        PASSWORD = "";
    }

    public synchronized static String getFormattedUserData() {
        return USERNAME + ":" + PASSWORD;
    }

    public synchronized static void logOutCurrentUser() {
        USERNAME = "";
        PASSWORD = "";
    }

    public synchronized static int getPid() {
        return PID;
    }

    public synchronized static void setPid(int pid) {
        ClientData.PID = pid;
    }

    public synchronized static String getUsername() {
        return USERNAME;
    }

    public synchronized static String getPassword() {
        return PASSWORD;
    }
}
