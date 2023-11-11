package main.utils;

import main.java.Main;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;

import java.util.ArrayList;
import java.util.List;
public class VoiceParticipantHandler {

    long guildID = Long.parseLong(Main.guildID);
    long roleID = Long.parseLong(Main.voiceRoleID);
    long modID = Long.parseLong(Main.modID);

    public void checkVoiceOnStartup(ReadyEvent event) {
        Guild guild = event.getJDA().getGuildById(guildID);
        if (guild == null) {
            System.out.print("Guild null!");
            return;
        }
        Role voiceRole = getVoiceRole(event);
        if (voiceRole == null) {
            return;
        }

        List<VoiceChannel> voiceChannelList = guild.getVoiceChannels();
        List<Member> inVoice = new ArrayList<>();

        for (VoiceChannel v : voiceChannelList) {
            if (v.getIdLong() == modID)
                continue;
            for (Member m : v.getMembers()) {
                if (m.getUser().isBot())
                    return;
                inVoice.add(m);
                addRole(event, m.getIdLong(), voiceRole, "[startup]");
            }
        }

        guild.loadMembers().onSuccess(members -> {
            for (Member m : members) {
                if (m.getUser().isBot())
                    continue;
                if (m.getRoles().contains(voiceRole) && !inVoice.contains(m)) {
                    System.out.print(m.getId() + "\n");
                    removeRole(event, m.getIdLong(), voiceRole, "[startup]");
                }
            }
        });
    }

    public Role getVoiceRole(GenericEvent event) {
        Guild guild = event.getJDA().getGuildById(guildID);
        Role voiceRole = guild.getRoleById(roleID);
        if (voiceRole == null) {
            guild.getChannelById(TextChannel.class, "287626728828829696").sendMessage("ℹ️ Voice role missing! Searching for another role to assign temporarily!").queue();
            for (Role r : guild.getRoles()) {
                if (r.getName().equalsIgnoreCase("in voice"))
                    voiceRole = r;
            }
        }

        if (voiceRole == null) {
            guild.getChannelById(TextChannel.class, "287626728828829696").sendMessage("ℹ️ No roles named \"In Voice\". Cannot assign role!").queue();
            return null;
        }
        return voiceRole;
    }

    public void addRole(GenericEvent event, long id, Role role, String reason) {
        Guild guild = event.getJDA().getGuildById(guildID);

        guild.addRoleToMember(UserSnowflake.fromId(id), role).reason("Adding voice role" + " " + reason).queue();
    }

    public void removeRole(GenericEvent event, long id, Role role, String reason) {
        Guild guild = event.getJDA().getGuildById(guildID);

        guild.removeRoleFromMember(UserSnowflake.fromId(id), role).reason("Removing voice role" + " " + reason).queue();
    }

    public void manageParticipants(GuildVoiceUpdateEvent event) {
        Guild guild = event.getGuild();
        Role voiceRole = getVoiceRole(event);

        if (voiceRole == null) {
            return;
        }

        if (guild.getIdLong() != guildID) {
            return;
        }

        if (event.getChannelJoined() != null && event.getChannelJoined().getIdLong() == modID)
            return;

        if (event.getNewValue() != null && event.getNewValue().getIdLong() != modID) {
            addRole(event, event.getMember().getIdLong(), voiceRole, "[voice join]");
        } else {
            removeRole(event, event.getMember().getIdLong(), voiceRole, "[voice leave]");
        }
    }
}
