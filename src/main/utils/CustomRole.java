import jakarta.persistence.*;

@Entity
@Table(name = "roles")
public class CustomRole {

    @Id
    @Column(name = "user")
    private long user;

    @Column(name = "role")
    private long role;

    public void setUser(long user) {
        this.user = user;
    }

    public void setRole(long role) {
        this.role = role;
    }

    public long getRole() {
        return role;
    }

    public long getUser() {
        return user;
    }
}
