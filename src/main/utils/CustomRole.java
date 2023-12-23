package main.utils;
import jakarta.persistence.*;

@Entity
@Table(name = "roles")
public class CustomRole {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(name = "user")
    private long user;

    @Column(name = "role")
    private long role;

    @Column(name = "guild")
    private long guild;

    public void setUser(long user) {
        this.user = user;
    }

    public void setRole(long role) {
        this.role = role;
    }

    public void setGuild(long guild) {
        this.guild = guild;
    }

    public long getRole() {
        return role;
    }

    public long getUser() {
        return user;
    }

    public long getGuild() {
        return guild;
    }
}
