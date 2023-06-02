import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.typesafe.config.Config;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.ArrayList;
import java.util.List;

public class RetroactiveCmd extends SlashCommand {

    Config config;
    DatabaseHandler database = new DatabaseHandler();
    public RetroactiveCmd(Config config) {
        this.name = "assign";
        this.help = "assign role to user, for backwards compatibility";
        options.add(new OptionData(OptionType.USER, "user", "user").setRequired(true));
        options.add(new OptionData(OptionType.ROLE, "role", "role").setRequired(true));
    }

    public void execute(SlashCommandEvent event) {
        if (!event.getMember().hasPermission(Permission.MANAGE_ROLES)) {
            event.reply("You do not have the Manage Roles permission.").queue();
            return;
        }
        if (!event.getGuild().getSelfMember().hasPermission(Permission.MANAGE_ROLES)) {
            event.reply("I do not have the Manage Roles permission.").queue();
            return;
        }
        if (!event.getGuild().getSelfMember().canInteract(event.getOption("role").getAsRole())) {
            event.reply("I cannot interact with that role. I can only edit roles lower than my highest role.").queue();
            return;
        }
        if (event.getOption("role").getAsRole().isManaged() || event.getOption("role").getAsRole().getIdLong() == event.getGuild().getIdLong()) {
            event.reply("This role cannot be assigned to a user.").queue();
            return;
        }
        CustomRole role = new CustomRole();
        role.setUser(event.getOption("user").getAsUser().getIdLong());
        role.setRole(event.getOption("role").getAsRole().getIdLong());
        try {
            database.saveRole(role);
        } catch (Exception e) {
            event.reply("Database error!").queue();
        }
        event.reply("Assigned role `" + event.getOption("role").getAsRole().getName() + "` to user `" + event.getOption("user").getAsUser().getName() + "`.").queue();

    }
}
