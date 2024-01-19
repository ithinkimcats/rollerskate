package main.cmd;

import com.jagrosh.jdautilities.command.MessageContextMenu;
import com.jagrosh.jdautilities.command.MessageContextMenuEvent;
import com.jagrosh.jdautilities.command.UserContextMenuEvent;
import main.utils.PollHandler;
import net.dv8tion.jda.api.Permission;

public class PollDetachContextMenu extends MessageContextMenu {

    private final PollHandler p;
    public PollDetachContextMenu(PollHandler p) {
        this.name = "Detach Poll from Message";
        this.userPermissions = new Permission[]{Permission.MANAGE_SERVER};
        this.p = p;
    }

    @Override
    protected void execute(MessageContextMenuEvent event) {
        if (event.getTarget().getAuthor() != event.getJDA().getSelfUser()) {
            event.respond("This only works on messages from " + event.getJDA().getSelfUser().getName());
            return;
        }
        event.deferReply(true).queue();
        try {
            p.deletePoll(event);
        } catch (Exception e) {
            event.getHook().editOriginal("Error detacthing poll from message. Is it already detached?").queue();
            e.printStackTrace();
            return;
        }
        event.getHook().editOriginal("Successfully detached poll from message.").queue();
    }
}
