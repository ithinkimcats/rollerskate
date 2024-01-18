package main.utils;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "poll")
public class Poll {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(name = "message")
    public long message;

    @Column(name = "channel")
    public long channel;

    @Column(name = "guild")
    public long guild;

    @Column(name = "color1")
    public String color1;

    @Column(name = "color2")
    public String color2;

    @ElementCollection(targetClass = Long.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "choice1voters", joinColumns = @JoinColumn(name = "poll_id"))
    @Column(name = "voters", nullable = false)
    public List<Long> choice1Voters = new ArrayList<>();

    @ElementCollection(targetClass = Long.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "choice2voters", joinColumns = @JoinColumn(name = "poll_id"))
    @Column(name = "voters", nullable = false)
    public List<Long> choice2Voters = new ArrayList<>();

    public List<Long> getChoice1Voters() {
        return choice1Voters;
    }

    public List<Long> getChoice2Voters() {
        return choice2Voters;
    }

    public int getTotalVoters() {
        return choice1Voters.size() + choice2Voters.size();
    }

    public void setChoice1Voters(List<Long> choice1Voters) {
        this.choice1Voters = choice1Voters;
    }

    public void setChoice2Voters(List<Long> choice2Voters) {
        this.choice2Voters = choice2Voters;
    }

    public void setMessage(long message) {
        this.message = message;
    }

    public long getMessage() {
        return message;
    }

    public long getGuild() {
        return guild;
    }

    public void setGuild(long guild) {
        this.guild = guild;
    }

    public long getChannel() {
        return channel;
    }

    public void setChannel(long channel) {
        this.channel = channel;
    }

    public void setColor1(String color1) {
        this.color1 = color1;
    }

    public void setColor2(String color2) {
        this.color2 = color2;
    }

    public String getColor1() {
        return color1;
    }

    public String getColor2() {
        return color2;
    }
}
