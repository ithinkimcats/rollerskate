package main.java;

import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigObject;
import com.typesafe.config.ConfigValue;
import main.cmd.*;
import main.utils.LinkEmbedConverterHandler;
import main.utils.Poll;
import main.utils.PollHandler;
import main.utils.VoiceParticipantHandler;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import java.util.List;

public class Main {
    public static Config config = ConfigFactory.load("config.conf");
    public static String guildID = config.getString("data.guildid");
    public static String voiceRoleID = config.getString("data.voiceroleid");
    public static String modID = config.getString("data.modid");
    static VoiceParticipantHandler voiceParticipantHandler = new VoiceParticipantHandler();
    static LinkEmbedConverterHandler linkEmbedConverterHandler = new LinkEmbedConverterHandler();
    static PollHandler pollHandler = new PollHandler();
    public static void main(String[] args) throws Exception
    {
        EventListener eventListener = new Bot(voiceParticipantHandler, linkEmbedConverterHandler, pollHandler);
        EventWaiter waiter = new EventWaiter();
        CommandClientBuilder builder = new CommandClientBuilder();
        builder.setOwnerId(config.getString("bot.owner"));
        builder.setStatus(OnlineStatus.IDLE);
        builder.setActivity(null);
        builder.setPrefix(config.getString("bot.prefix"));
        builder.addSlashCommand(new CreateRoleCmd(config));
        builder.addSlashCommand(new DeleteRoleCmd(config));
        builder.addSlashCommand(new RetroactiveCmd(config));
        builder.addSlashCommand(new PronounCmd(config));
        builder.addSlashCommand(new PollCmd(config, pollHandler));
        builder.addContextMenu(new PollDetachContextMenu(pollHandler));
        CommandClient commandClient = builder.build();
        JDA jda = JDABuilder.createDefault(config.getString("bot.token")).addEventListeners(eventListener).enableCache(CacheFlag.VOICE_STATE).enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_VOICE_STATES).addEventListeners(commandClient).build();
    }
}