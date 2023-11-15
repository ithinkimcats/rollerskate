package main.utils;

import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class LinkEmbedConverterHandler {

    Random random = new Random();

    private enum DomainType {
        X("x.com", twitter),
        TWITTER("twitter.com", twitter),

        TIKTOK2("vt.tiktok.com", tiktok),
        TIKTOK("tiktok.com", tiktok),
        INSTAGRAM("instagram.com", instagram);
//        PIXIV("pixiv.net", pixiv);

        private String domain;
        private String[] urls;
        DomainType(String domain, String[] urls) {
            this.domain = domain;
            this.urls = urls;
        }
    }

    private static final String[] twitter = {
            "vxtwitter.com",
            "fixupx.com",
            "twittpr.com"
    };

    private static final String[] tiktok = {
            "vt.vxtiktok.com",
    };

    private static final String[] instagram = {
            "ddinstagram.com"
    };

//    private static final String[] pixiv = {
//            "pixiv.net"
//    };

    public void handleMessageEvent(MessageReceivedEvent event) {
        String convertedLink = convertLink(event.getMessage().getContentRaw());

        if (convertedLink.equals("!")) {
            return;
        }

        MessageChannel mc = event.getChannel();

        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("\n\uD83D\uDD17" + " ").append(convertedLink);

        MessageCreateBuilder messageCreateBuilder = new MessageCreateBuilder();
        messageCreateBuilder.addContent(stringBuilder.toString());
        messageCreateBuilder.setAllowedMentions(null);
        mc.sendMessage(messageCreateBuilder.build()).setMessageReference(event.getMessage()).mentionRepliedUser(false).queue();
    }

    public String convertLink(String string) {
        List<DomainType> foundTypes = new ArrayList<>();

        if (string.contains(DomainType.X.domain)) {
            string = string.replace(DomainType.X.domain, DomainType.TWITTER.domain);
        }

        String[] splitUp = string.trim().split("\\s+");
        List<String> links = new ArrayList<>();
        List<String> validLinks = new ArrayList<>();
        for (String s : splitUp) {
            if (s.contains("http") && s.contains("://")) {
                links.add(s);
            }
        }

        for (String link : links) {
            for (DomainType d : DomainType.values()) {
                if (link.contains("www." + d.domain) || link.contains("/" + d.domain)) {
                    foundTypes.add(d);
                    validLinks.add(link);
                }
            }
        }

        if (validLinks.isEmpty() || foundTypes.isEmpty()) {
            return "!";
        }

        List<String> convertedLinks = new ArrayList<>();
        for (int i = 0; i < validLinks.size(); i++) {
            String link = validLinks.get(i);
            String domain = foundTypes.get(i).domain;
            String url = foundTypes.get(i).urls[random.nextInt(foundTypes.get(i).urls.length)];
            String converted = link.replace(domain, url);
            convertedLinks.add(converted);
        }

        StringBuilder stringBuilder = new StringBuilder();
        for (String s : convertedLinks) {
            stringBuilder.append(s);
            stringBuilder.append(" ");
        }

        return stringBuilder.toString();

        }
    }
