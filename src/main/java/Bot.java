package main.java;

import main.utils.VoiceParticipantHandler;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class Bot extends ListenerAdapter implements EventListener {

    VoiceParticipantHandler voiceParticipantHandler;
    public Bot(VoiceParticipantHandler voiceParticipantHandler) {
        this.voiceParticipantHandler = voiceParticipantHandler;
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        voiceParticipantHandler.checkVoiceOnStartup(event);
    }

    @Override
    public void onGuildVoiceUpdate(@NotNull GuildVoiceUpdateEvent event) {
        voiceParticipantHandler.manageParticipants(event);
    }
}
