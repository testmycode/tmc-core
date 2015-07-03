
package hy.tmc.core.configuration;

import hy.tmc.core.domain.Course;

public interface TmcSettings {
    public String getServerAddress();
    public String getPassword();
    public String getUsername();
    public boolean userDataExists();
    public Course getCurrentCourse();
}
