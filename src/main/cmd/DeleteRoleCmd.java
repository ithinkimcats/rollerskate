package main.cmd;

import com.jagrosh.jdautilities.command.CooldownScope;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.typesafe.config.Config;
import main.utils.CustomRole;
import main.utils.DatabaseHandler;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.ArrayList;
import java.util.List;

public class DeleteRoleCmd extends SlashCommand {
    private final Config config;
    DatabaseHandler database;
    public DeleteRoleCmd(Config config) {
        this.name = "delete";
        this.config = config;
        this.help = "removes association of role with user";
        this.database = new DatabaseHandler();
        this.cooldown = 10;
        this.cooldownScope = CooldownScope.USER;
        List<OptionData> options = new ArrayList<>();
        options.add(new OptionData(OptionType.USER, "user", "user").setRequired(true));
        options.add(new OptionData(OptionType.BOOLEAN, "delete", "delete role").setRequired(true));
        this.options = options;
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        if (!event.getGuild().getSelfMember().hasPermission(Permission.MANAGE_ROLES)) {
            event.reply("I do not have the Manage Roles permission.").queue();
            return;
        }
        CustomRole role = null;
        try {
            role = database.getRoleByUser(event.getOption("user").getAsUser().getIdLong(), event.getGuild().getIdLong());
        } catch (Exception e) {
            e.printStackTrace();
            event.reply("Database error getting role assigned to `" + event.getOption("user").getAsUser().getId() + "`.").queue();
            return;
        }
        if (role == null) {
            event.reply("No role assigned to `" + event.getOption("user").getAsUser().getId() + "`.").queue();
            return;
        }
        Role custom = event.getGuild().getRoleById(role.getRole());
        if (role.getUser() != event.getUser().getIdLong() || !event.getMember().hasPermission(Permission.MANAGE_ROLES)) {
            event.reply("You do not own this role, or do not have the Manage Roles permission.").queue();
            return;
        }
        if (role == null) {
            event.reply("Role assigned to `" + event.getOption("user").getAsUser().getId() + "` not found.").queue();
        } else {
            database.deleteRole(role);
            if (event.getOption("delete").getAsBoolean()) {
                try {
                    custom.delete().queue();
                } catch (Exception e) {
                    event.reply("Error deleting role! Role may no longer exist on the guild.").queue();
                }
            }
            event.reply("Role assigned to `" + event.getOption("user").getAsUser().getId() + "` successfully removed.").queue();
        }
    }
}
