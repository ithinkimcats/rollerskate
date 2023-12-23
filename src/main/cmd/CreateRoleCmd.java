package main.cmd;

import com.jagrosh.jdautilities.command.CooldownScope;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.typesafe.config.Config;
import jakarta.persistence.Embeddable;
import main.java.Main;
import main.utils.CustomRole;
import main.utils.DatabaseHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.managers.GuildManager;
import net.dv8tion.jda.api.managers.RoleManager;
import net.dv8tion.jda.internal.handle.GuildSetupController;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class CreateRoleCmd extends SlashCommand {

    private final Config config;
    private DatabaseHandler database;

    public CreateRoleCmd(Config config) {
        this.config = config;
        this.name = "role";
        this.help = "create role, or edit if it already exists";
        this.database = new DatabaseHandler();
        this.cooldown = 10;
        this.cooldownScope = CooldownScope.USER;
        List<OptionData> options = new ArrayList<>();
        options.add(new OptionData(OptionType.STRING, "name", "role name").setRequired(false).setMaxLength(32).setMinLength(2));
        options.add(new OptionData(OptionType.STRING, "color", "hex color").setRequired(false).setMaxLength(32).setMinLength(2));
        this.options = options;
    }

    public void execute(SlashCommandEvent event) {
        event.deferReply().setEphemeral(false).queue();
        if(!event.getGuild().getSelfMember().hasPermission(Permission.MANAGE_ROLES)) {
            event.getHook().editOriginal("I do not have the Manage Roles permission. Role cannot be created. Contact server staff.").queue();
            return;
        }
        if (event.getGuild().getRoles().size() == 250) {
            event.getHook().editOriginal("Maximum role limit reached. Contact server staff.").queue();
            return;
        }
        OptionMapping name = event.getOption("name");
        OptionMapping color = event.getOption("color");
        String nameString = null;
        String colorString = null;
        if (name != null) {
            nameString = name.getAsString();
        }
        if (color != null) {
            colorString = color.getAsString();
        }

        String roleName = null;
        List<String> bannedNames = config.getStringList("data.blockedRoleNames");
        if (nameString != null) {
            for (String s : bannedNames) {
                if (nameString.contains(s) || nameString.contains(event.getGuild().getName())) {
                    event.getHook().editOriginal("Role name invalid. Try again.").queue();
                    return;
                }
            }
            roleName = nameString;
        }
        Color roleColor = null;
        if (colorString != null) {
            try {
                if (!event.getOption("color").getAsString().contains("#"))
                    roleColor = Color.decode("#" + event.getOption("color").getAsString());
            } catch (Exception e) {
                event.getHook().editOriginal("Color could not be parsed. Color must be in HEX format.").queue();
                return;
            }
        }

        CustomRole role = new CustomRole();
        Role createdRole = null;
        for (CustomRole r : database.getRoles(event.getGuild().getIdLong())) {
            if (event.getUser().getIdLong() == r.getUser()) {
                if (event.getGuild().getRoleById(r.getRole()) == null) {
                    event.getHook().editOriginal("Unable to find role with ID: `" + r.getRole() + "`").queue();
                    return;
                } else {
                    if (!event.getGuild().getSelfMember().canInteract(event.getGuild().getRoleById(r.getRole()))) {
                        event.getHook().editOriginal("I cannot interact with that role. I can only edit roles lower than my highest role.").queue();
                        return;
                    }
                }
                RoleManager rm = event.getGuild().getRoleById(r.getRole()).getManager();
                if (roleName == null && roleColor == null) {
                    EmbedBuilder eb = new EmbedBuilder();
                    eb.setColor(rm.getRole().getColor());
                    eb.setTitle("Role Information");
                    eb.addField("Role Name", rm.getRole().getName(), false);
                    eb.addField("Role Color", String.format("#%06X", (0xFFFFFF & rm.getRole().getColorRaw())), false);
                    event.getHook().editOriginalEmbeds(eb.build()).queue();
                    return;
                }
                if (roleName == null) {
                    roleName = rm.getRole().getName();
                }
                if (roleColor == null) {
                    roleColor = rm.getRole().getColor();
                }
                rm.setName(roleName);
                rm.setColor(roleColor);
                rm.complete();
                event.getHook().editOriginal("Role edited!").queue();
                return;
            }
        }
        if (roleName != null) {
            try {
                if (roleColor == null) {
                    roleColor = Color.gray;
                }
                createdRole = event.getGuild().createRole().setName(roleName).setColor(roleColor).setPermissions(0L).complete();
            } catch (Exception e) {
                event.getHook().editOriginal("Error creating role!").queue();
            }
        } else {
            event.getHook().editOriginal("Role name missing!").queue();
        }

        if (createdRole != null) {
            role.setUser(event.getUser().getIdLong());
            role.setRole(createdRole.getIdLong());
            role.setGuild(event.getGuild().getIdLong());
            database.saveRole(role);
            event.getHook().editOriginal("Role created under name `" + createdRole.getName() + "`!").queue();
        } else {
            return;
        }

        CustomRole r = database.getRoleByUser(event.getUser().getIdLong(), event.getGuild().getIdLong());
        Role newRole = event.getGuild().getRoleById(r.getRole());
        if (event.getGuild().getId().equals(Main.guildID))
            event.getGuild().modifyRolePositions(true).selectPosition(newRole).moveTo(39).queue();
        try {
            event.getGuild().addRoleToMember(event.getMember(), newRole).reason("Automatic assignment of custom role.").queue();
        } catch (Exception e) {
            event.getChannel().sendMessage("Error assigning created role. (" + newRole.getName() + ")") .queue();
        }
    }
}
