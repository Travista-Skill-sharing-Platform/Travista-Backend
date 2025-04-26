package backend.model;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.HashSet;
import java.util.Set;

@Document(collection = "user")
public class UserModel {
    @Id
    @GeneratedValue
    private String id;
    private String fullname;
    private String email;
    private String password;
    private String phone;
    private Set<String> followedUsers = new HashSet<>();
    private String bio; // Added bio field

    public UserModel() {

    }

    public UserModel(String id, String fullname, String email, String password, String phone) {
        this.id = id;
        this.fullname = fullname;
        this.email = email;
        this.password = password;
        this.phone = phone;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Set<String> getFollowedUsers() {
        return followedUsers;
    }

    public void setFollowedUsers(Set<String> followedUsers) {
        this.followedUsers = followedUsers;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }
}
