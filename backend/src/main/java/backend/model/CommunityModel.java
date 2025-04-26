package backend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "communities")
public class CommunityModel {
    @Id
    private String id;

    private String name;

    private List<NoticeModel> notices = new ArrayList<>();

    private @DBRef List<UserModel> users = new ArrayList<>();

    private String ownerId; // New field to store the owner's user ID

    public CommunityModel() {
    }

    public CommunityModel(String id, String name, List<NoticeModel> notices, List<UserModel> users) {
        this.id = id;
        this.name = name;
        this.notices = notices;
        this.users = users;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<NoticeModel> getNotices() {
        return notices;
    }

    public void setNotices(List<NoticeModel> notices) {
        this.notices = notices;
    }

    public List<UserModel> getUsers() {
        return users;
    }

    public void setUsers(List<UserModel> users) {
        this.users = users;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }
}
