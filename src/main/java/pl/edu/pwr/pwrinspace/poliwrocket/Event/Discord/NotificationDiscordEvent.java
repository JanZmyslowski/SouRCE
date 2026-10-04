package pl.edu.pwr.pwrinspace.poliwrocket.Event.Discord;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.Configuration.Configuration;
import pl.edu.pwr.pwrinspace.poliwrocket.Event.NotificationEvent;
import pl.edu.pwr.pwrinspace.poliwrocket.Service.Notification.NotificationFormatService;

public class NotificationDiscordEvent extends NotificationEvent {

    protected String channelName = Configuration.getInstance().DISCORD_CHANNEL_NAME;

    public NotificationDiscordEvent(NotificationFormatService notificationFormatService) {
        super(notificationFormatService);
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {

        if (!event.getAuthor().isBot()) {
            String messageReceived = event.getMessage().getContentRaw();
            var message = notification.getFormattedMessage(messageReceived);
            if (message instanceof EmbedBuilder) {
                TextChannel ch = this.getChannel(event);
                if (ch != null) ch.sendMessageEmbeds(((EmbedBuilder) message).build()).queue();
            } else if (message instanceof String) {
                TextChannel channel = this.getChannel(event);
                if (channel != null) {
                    String eventChannelName = null;
                    if (event.isFromGuild()) {
                        try {
                            eventChannelName = event.getChannel().asTextChannel().getName();
                        } catch (IllegalStateException ignored) {
                        }
                    }
                    if (!((String) message).contains("Error") || (eventChannelName != null && eventChannelName.equals(channelName))) {
                        channel.sendMessage(((String) message)).queue();
                    }
                }
            }
        }
    }

    protected TextChannel getChannel(@NotNull MessageReceivedEvent event) {

        if (!channelName.equals("") && event.isFromGuild()) {
            var channels = event.getGuild().getTextChannelsByName(channelName, true);
            if (!channels.isEmpty()) {
                return channels.get(0);
            }
        }
        if (event.isFromGuild()) {
            try {
                return event.getChannel().asTextChannel();
            } catch (IllegalStateException e) {
                return null;
            }
        }
        return null;
    }
}