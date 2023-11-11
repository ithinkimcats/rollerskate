package main.cmd;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigObject;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.ArrayList;
import java.util.List;

public class PronounCmd extends SlashCommand {
    private final Config config;
    public PronounCmd(Config config) {
        this.name = "pronoun";
        this.help = "set up pronoun";
        this.config = config;
        ConfigObject pronouns = config.getObject("data.pronounRoles");
        List<OptionData> options = new ArrayList<>();
        options.add(new OptionData(OptionType.STRING, "pronoun", "pronoun to add/remove").setRequired(true));
        for (String s : pronouns.keySet()) {
            options.get(0).addChoice(s, pronouns.get(s).render().replace("\"", ""));
        }
        this.options = options;
    }

    @Override
    protected void execute(SlashCommandEvent event) {

        Guild guild = event.getGuild();
        Role pronoun = guild.getRoleById(event.getOption("pronoun").getAsString());
        Member member = event.getMember();
        if (pronoun == null) {
            event.reply("Error when attempting to lookup role! Contact server staff.").setEphemeral(true).queue();
            return;
        }

        try {
            if (member.getRoles().contains(pronoun)) {
                guild.removeRoleFromMember(UserSnowflake.fromId(member.getIdLong()), pronoun).reason("Removing pronoun role").queue();
                event.reply("Removed pronoun role `" + pronoun.getName() + "` from you.").setEphemeral(true).queue();
            } else {
                guild.addRoleToMember(UserSnowflake.fromId(member.getIdLong()), pronoun).reason("Adding pronoun role").queue();
                event.reply("Assigned pronoun role `" + pronoun.getName() + "` to you.").setEphemeral(true).queue();
            }
        } catch (Exception e) {
            event.reply("Error when attempting role management! Contact server staff.").setEphemeral(true).queue();
            return;
        }
    }
}
