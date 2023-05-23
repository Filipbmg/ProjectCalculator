package project.model;

public class Project {
    private int projectId;
    private String projectName;
    private int ownerId;

    public Project(int projectId, String projectName, int ownerId) {
        this.projectId = projectId;
        this.projectName = projectName;
        this.ownerId = ownerId;
    }

    public int getProjectId() {
        return projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }
}
