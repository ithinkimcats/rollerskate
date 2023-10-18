package main.java;

import main.utils.LinkEmbedConverterHandler;
import main.utils.VoiceParticipantHandler;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class Bot extends ListenerAdapter implements EventListener {

    VoiceParticipantHandler voiceParticipantHandler;
    LinkEmbedConverterHandler linkEmbedConverterHandler;
    public Bot(VoiceParticipantHandler voiceParticipantHandler, LinkEmbedConverterHandler linkEmbedConverterHandler) {
        this.voiceParticipantHandler = voiceParticipantHandler;
        this.linkEmbedConverterHandler = linkEmbedConverterHandler;
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        voiceParticipantHandler.checkVoiceOnStartup(event);
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {

        if (event.getMember().getUser().isBot())
            return;
        if (event.getMessage().getContentRaw().contains("https") || event.getMessage().getContentRaw().contains("http"))
            linkEmbedConverterHandler.handleMessageEvent(event);
    }

    @Override
    public void onGuildVoiceUpdate(@NotNull GuildVoiceUpdateEvent event) {
        if (event.getMember().getUser().isBot())
            return;
        voiceParticipantHandler.manageParticipants(event);
    }
}
