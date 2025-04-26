package backend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "notices")
public class NoticeModel {
    @Id
    private String id;

    private String title;
    private String content;
    private String userId; // New field to store user ID

    public NoticeModel() {
    }

    public NoticeModel(String id, String title, String content) {
        this.id = id;
        this.title = title;
        this.content = content;
    }

    public NoticeModel(String id, String title, String content, String userId) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.userId = userId; // Initialize userId
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
