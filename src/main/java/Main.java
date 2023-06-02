import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;

import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception
    {
        Config config = ConfigFactory.load("config.conf");
        EventWaiter waiter = new EventWaiter();
        CommandClientBuilder builder = new CommandClientBuilder();
        builder.setOwnerId(config.getString("bot.owner"));
        builder.setStatus(OnlineStatus.IDLE);
        builder.setActivity(null);
        builder.setPrefix(config.getString("bot.prefix"));
        builder.forceGuildOnly("1113986949972242462");
        builder.addSlashCommand(new CreateRoleCmd(config));
        builder.addSlashCommand(new DeleteRoleCmd(config));
        builder.addSlashCommand(new RetroactiveCmd(config));
        CommandClient commandClient = builder.build();
        JDA jda = JDABuilder.createDefault(config.getString("bot.token")).addEventListeners(commandClient).build();
    }
}