package main.cmd;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.typesafe.config.Config;
import main.utils.Poll;
import main.utils.PollHandler;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.managers.RoleManager;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class PollCmd extends SlashCommand {
    private final Config config;
    private final PollHandler ph;
    public PollCmd(Config config, PollHandler ph) {
        this.name = "poll";
        this.help = "set up poll";
        this.config = config;
        this.ph = ph;
        List<OptionData> options = new ArrayList<>();
        options.add(new OptionData(OptionType.STRING, "question", "poll question").setRequired(true).setMaxLength(256).setMinLength(2));
        options.add(new OptionData(OptionType.STRING, "choice1", "choice1").setRequired(true).setMaxLength(32).setMinLength(2));
        options.add(new OptionData(OptionType.STRING, "choice2", "choice2").setRequired(true).setMaxLength(32).setMinLength(2));
        options.add(new OptionData(OptionType.STRING, "color1", "color1").setRequired(true).setMaxLength(32).setMinLength(2));
        options.add(new OptionData(OptionType.STRING, "color2", "color2").setRequired(true).setMaxLength(32).setMinLength(2));
        this.options = options;
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        if (!event.getMember().hasPermission(Permission.MANAGE_SERVER)) {
            event.reply("You cannot use this command.").setEphemeral(true).queue();
            return;
        }

        Poll poll = new Poll();
        event.deferReply(true).queue(m -> m.editOriginal(ph.create(poll, event) ? "Successfully created poll" : "Error creating poll").queue());
    }
}
