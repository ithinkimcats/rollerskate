package main.utils;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class VoiceParticipantHandler {
    long roleID = 1143788097021165619L;

    public void addOrRemoveRole(GuildVoiceUpdateEvent event) {
        Guild guild = event.getGuild();
        Role voiceRole = guild.getRoleById(roleID);

        if (event.getChannelJoined() != null && event.getChannelJoined().getId().equals("221721509087936523"))
            return;

        if (voiceRole == null) {
            guild.getChannelById(TextChannel.class, "287626728828829696").sendMessage("ℹ️ Voice role missing! Searching for another role to assign temporarily!").queue();
            for (Role r : guild.getRoles()) {
                if (r.getName().equalsIgnoreCase("in voice"))
                        voiceRole = r;
            }
        }

        if (voiceRole == null) {
            guild.getChannelById(TextChannel.class, "287626728828829696").sendMessage("ℹ️ No roles named \"In Voice\". Cannot assign role!").queue();
            return;
        }
        if (event.getNewValue() != null) {
            guild.addRoleToMember(UserSnowflake.fromId(event.getMember().getIdLong()), voiceRole).reason("Adding voice role").queue();
        } else {
            guild.removeRoleFromMember(UserSnowflake.fromId(event.getMember().getIdLong()), voiceRole).reason("Removing voice role").queue();
        }
    }
}
