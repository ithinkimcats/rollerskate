package main.utils;

import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.managers.RoleManager;
import net.dv8tion.jda.api.utils.AttachedFile;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.PieChart;
import org.knowm.xchart.PieChartBuilder;
import org.knowm.xchart.PieSeries;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class PollHandler {

    private static Config config = ConfigFactory.load("config.conf");
    private DatabaseHandler db = new DatabaseHandler();
    private EmbedBuilder eb = new EmbedBuilder();
    public boolean create(Poll poll, SlashCommandEvent event) {
        if (db.getPolls(event.getChannel().getIdLong()) == null)
            return false;

        try {
            String question = event.getOption("question").getAsString();
            String choice1 = event.getOption("choice1").getAsString();
            String choice2 = event.getOption("choice2").getAsString();
            eb.clearFields();
            eb.setTitle(question);
            eb.addField("Choice 1", choice1 + " | 0%", true);
            eb.addField("Choice 2", choice2 + " | 0%", true);
            MessageCreateBuilder mb = new MessageCreateBuilder();
            mb.setEmbeds(eb.build());
            mb.addActionRow(Button.primary("poll-choice1", choice1), Button.primary("poll-choice2", choice2));
            event.getChannel().sendMessage(mb.build()).queue((message) -> {
                poll.setMessage(message.getIdLong());
                poll.setGuild(event.getGuild().getIdLong());
                poll.setChannel(event.getChannel().getIdLong());
                poll.setColor1("#" + event.getOption("color1").getAsString());
                poll.setColor2("#" + event.getOption("color2").getAsString());
                db.savePoll(poll);
                createRoles(event, poll);
            });
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void createRoles(SlashCommandEvent event, Poll poll) {
        try {
            Role r1 = event.getGuild().getRoleById(config.getString("data.pollrole1"));
            Role r2 = event.getGuild().getRoleById(config.getString("data.pollrole2"));
            RoleManager rm1 = r1.getManager();
            RoleManager rm2 = r2.getManager();
            rm1.setName(event.getOption("choice1").getAsString());
            rm2.setName(event.getOption("choice2").getAsString());
            rm1.setColor(Color.decode(poll.getColor1()));
            rm2.setColor(Color.decode(poll.getColor2()));
            rm1.queue();
            rm2.queue();
        } catch (Exception e) {
            e.printStackTrace();
            event.getGuild().getChannelById(TextChannel.class, "287626728828829696").sendMessage("ℹ️ Error when getting poll roles!").queue();
        }
    }

    public void deletePoll(MessageDeleteEvent event) {
        try {
            Poll poll = null;
            for (Poll p : db.getPolls(event.getGuild().getIdLong())) {
                if (p.getMessage() == event.getMessageIdLong()) {
                    poll = p;
                    break;
                }
            }
            if (poll == null) {
                return;
            }
            List<Member> members = new ArrayList<>();
            for (long l : poll.getChoice1Voters()) {
                members.add(event.getGuild().retrieveMemberById(l).complete());
            }
            for (long l : poll.getChoice2Voters()) {
                members.add(event.getGuild().retrieveMemberById(l).complete());
            }
            for (Member m : members) {
                event.getGuild().removeRoleFromMember(m.getUser(), event.getGuild().getRoleById(config.getString("data.pollrole1"))).queue();
                event.getGuild().removeRoleFromMember(m.getUser(), event.getGuild().getRoleById(config.getString("data.pollrole2"))).queue();
            }
            db.deletePoll(poll);
        } catch (Exception e) {
            System.out.print("\n" + "errdel : " + event.getMessageIdLong());
            e.printStackTrace();
        }
    }

    public void editPoll(ButtonInteractionEvent event) {
        Poll poll = db.getPollByMessage(event.getMessageIdLong(), event.getGuild().getIdLong());
        List<Long> voters1 = poll.getChoice1Voters();
        List<Long> voters2 = poll.getChoice2Voters();
        long user = event.getUser().getIdLong();
        String button = event.getButton().getId();
        // voting twice
        if ((voters1.contains(user) && button.equals("poll-choice1")) || (voters2.contains(user) && button.equals("poll-choice2"))) {
            event.reply("You have already voted for this choice.").setEphemeral(true).queue();
            return;
        }
        Role r1 = event.getGuild().getRoleById(config.getString("data.pollrole1"));
        Role r2 = event.getGuild().getRoleById(config.getString("data.pollrole2"));
        // switch vote
        if (voters1.contains(user) && button.equals("poll-choice2")) {
            try {
                voters1.remove(user);
                voters2.add(user);
                poll.setChoice1Voters(voters1);
                poll.setChoice2Voters(voters2);
                db.savePoll(poll);
                event.getGuild().removeRoleFromMember(event.getMember(), r1).reason("Poll role assignment").queue();
                event.getGuild().addRoleToMember(event.getMember(), r2).reason("Poll role assignment").queue();
                event.reply("Switching vote to Choice 2...").setEphemeral(true).queue();
                updateMessage(event);
            } catch (Exception e) {
                e.printStackTrace();
                event.reply("Error switching vote to Choice 2!").setEphemeral(false).queue();
                return;
            }
        }
        if (voters2.contains(user) && button.equals("poll-choice1")) {
            try {
                voters2.remove(user);
                voters1.add(user);
                poll.setChoice1Voters(voters1);
                poll.setChoice2Voters(voters2);
                db.savePoll(poll);
                event.getGuild().removeRoleFromMember(event.getMember(), r2).reason("Poll role assignment").queue();
                event.getGuild().addRoleToMember(event.getMember(), r1).reason("Poll role assignment").queue();
                event.reply("Switching vote to Choice 1...").setEphemeral(true).queue();
                updateMessage(event);
            } catch (Exception e) {
                e.printStackTrace();
                event.reply("Error switching vote to Choice 1!").setEphemeral(false).queue();
                return;
            }
        }
        // no vote
        if (!voters1.contains(user) && !voters2.contains(user)) {
            try {
                if(button.equals("poll-choice1")) {
                    voters1.add(user);
                    poll.setChoice1Voters(voters1);
                    event.deferReply().queue(d -> d.deleteOriginal().queue());
                    event.getGuild().addRoleToMember(event.getMember(), r1).reason("Poll role assignment").queue();
                    db.savePoll(poll);
                    updateMessage(event);
                }
                if (button.equals("poll-choice2")) {
                    voters2.add(user);
                    poll.setChoice2Voters(voters2);
                    event.deferReply().queue(d -> d.deleteOriginal().queue());
                    event.getGuild().addRoleToMember(event.getMember(), r2).reason("Poll role assignment").queue();
                    db.savePoll(poll);
                    updateMessage(event);
                }
            } catch (Exception e) {
                e.printStackTrace();
                event.reply("Error voting!").setEphemeral(false).queue();
                return;
            }
        }
    }

    public void updateMessage(ButtonInteractionEvent event) {
        try {
            Message message = event.getMessage();
            Poll poll = db.getPollByMessage(message.getIdLong(), message.getGuild().getIdLong());
            MessageEmbed embed = event.getMessage().getEmbeds().get(0);
            eb.clearFields();
            eb.setTitle(embed.getTitle());
            float v = poll.getTotalVoters();
            if (v == 0) {
                v =+ 1;
            }
            eb.addField("Choice 1",
                    embed.getFields().get(0).getValue().substring(0, embed.getFields().get(0).getValue().indexOf("|"))
                            + " | " + String.format("%.0f%%", (poll.getChoice1Voters().size() / v * 100.0)) + " (" + poll.getChoice1Voters().size() + ")", false);
            eb.addField("Choice 2",
                    embed.getFields().get(1).getValue().substring(0, embed.getFields().get(1).getValue().indexOf("|"))
                            + " | " + String.format("%.0f%%", (poll.getChoice2Voters().size() / v * 100.0)) + " (" + poll.getChoice2Voters().size() + ")", false);
            message.editMessageEmbeds(eb.build()).queue();
            message.editMessageAttachments(AttachedFile.fromData(createPieChart(poll))).queue();
        } catch (Exception e) {
            e.printStackTrace();
            event.getGuild().getChannelById(TextChannel.class, "287626728828829696").sendMessage("ℹ️ Error when updating poll message!").queue();
        }
    }

    public File createPieChart(Poll poll) throws IOException {
        BufferedImage b = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
        BufferedImage filter = ImageIO.read(new File("resources/pie.png"));
        Graphics2D g = b.createGraphics();
        PieChartBuilder p = new PieChartBuilder();
        p.height(256).width(256);
        PieChart chart = p.build();

        chart.getStyler().setPlotContentSize(1);
        chart.getStyler().setCircular(true);
        chart.getStyler().setLabelsVisible(false);
        chart.getStyler().setLegendVisible(false);
        chart.getStyler().setPlotBorderVisible(false);
        chart.getStyler().setChartTitleBoxVisible(false);
        chart.getStyler().setChartPadding(0);
        chart.getStyler().setDefaultSeriesRenderStyle(PieSeries.PieSeriesRenderStyle.Pie);
        chart.getStyler().setChartBackgroundColor(new Color(0, 0, 0, 0));
        chart.getStyler().setPlotBackgroundColor(new Color(0, 0, 0, 0));
        chart.getStyler().setSeriesColors(new Color[]{Color.decode(poll.getColor1()), Color.decode(poll.getColor2())});

        chart.addSeries("choice1", poll.getChoice1Voters().size());
        chart.addSeries("choice2", poll.getChoice2Voters().size());

        g.setClip(new Ellipse2D.Float(0, 0, 256, 256));
        g.drawImage(BitmapEncoder.getBufferedImage(chart), 0, 0, null);
        g.drawImage(filter, 0, 0, null);
        g.dispose();

        try {
            Path tempPath = Files.createTempDirectory("_poll");
            File tempFile = new File(tempPath + File.separator + "pie" + poll.getMessage()+".png");
            ImageIO.write(b, "png", tempFile);
            return tempFile;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
