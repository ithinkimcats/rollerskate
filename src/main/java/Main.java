package main.java;

import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import main.cmd.CreateRoleCmd;
import main.cmd.DeleteRoleCmd;
import main.cmd.RetroactiveCmd;
import main.utils.LinkEmbedConverterHandler;
import main.utils.VoiceParticipantHandler;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import java.util.List;

public class Main {
    public static String guildID = "194167041685454848";
    static VoiceParticipantHandler voiceParticipantHandler = new VoiceParticipantHandler();
    static LinkEmbedConverterHandler linkEmbedConverterHandler = new LinkEmbedConverterHandler();
    public static void main(String[] args) throws Exception
    {
        EventListener eventListener = new Bot(voiceParticipantHandler, linkEmbedConverterHandler);
        Config config = ConfigFactory.load("config.conf");
        EventWaiter waiter = new EventWaiter();
        CommandClientBuilder builder = new CommandClientBuilder();
        builder.setOwnerId(config.getString("bot.owner"));
        builder.setStatus(OnlineStatus.IDLE);
        builder.setActivity(null);
        builder.setPrefix(config.getString("bot.prefix"));
        builder.forceGuildOnly(guildID);
        builder.addSlashCommand(new CreateRoleCmd(config));
        builder.addSlashCommand(new DeleteRoleCmd(config));
        builder.addSlashCommand(new RetroactiveCmd(config));
        CommandClient commandClient = builder.build();
        JDA jda = JDABuilder.createDefault(config.getString("bot.token")).addEventListeners(eventListener).enableCache(CacheFlag.VOICE_STATE).enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_VOICE_STATES).addEventListeners(commandClient).build();
    }
}