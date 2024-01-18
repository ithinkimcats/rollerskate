package main.java;

import main.utils.LinkEmbedConverterHandler;
import main.utils.PollHandler;
import main.utils.VoiceParticipantHandler;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class Bot extends ListenerAdapter implements EventListener {

    VoiceParticipantHandler voiceParticipantHandler;
    LinkEmbedConverterHandler linkEmbedConverterHandler;
    PollHandler pollHandler;
    public Bot(VoiceParticipantHandler voiceParticipantHandler, LinkEmbedConverterHandler linkEmbedConverterHandler, PollHandler pollHandler) {
        this.voiceParticipantHandler = voiceParticipantHandler;
        this.linkEmbedConverterHandler = linkEmbedConverterHandler;
        this.pollHandler = pollHandler;
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        voiceParticipantHandler.checkVoiceOnStartup(event);
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (!event.getMessage().getChannel().canTalk())
            return;
        if (event.getMember() == null || event.getAuthor().isBot() || event.getAuthor().isSystem())
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

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        if (event.getButton().getId().startsWith("poll-")) {
            pollHandler.editPoll(event);
        }
    }

    @Override
    public void onMessageDelete(@NotNull MessageDeleteEvent event) {
        pollHandler.deletePoll(event);
    }
}
