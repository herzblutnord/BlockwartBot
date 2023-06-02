package moe.herz;

import org.pircbotx.Configuration;
import org.pircbotx.PircBotX;
import okhttp3.*;

import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.types.GenericMessageEvent;
import org.pircbotx.hooks.events.JoinEvent;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.User;

import java.util.*;
import java.io.IOException;
import javax.net.ssl.SSLSocketFactory;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;


public class BlockwartBot extends ListenerAdapter {

    private static final int MAX_UNSENT_MESSAGES = 5;
    private static final int MAX_RECEIVED_MESSAGES = 10;

    private final Map<String, LinkedList<String>> unsentMessages = new HashMap<>();
    private final Map<String, Integer> messagesToReceive = new HashMap<>();

    public static void main(String[] args) {

        String botName = "Loreley";

        // Configure the bot
        Configuration configuration = new Configuration.Builder()
                .setName(botName)
                .addServer("herz.moe", 6667)
                .addAutoJoinChannel("#herz")
                .addListener(new BlockwartBot())
                .setSocketFactory(SSLSocketFactory.getDefault()) // Enable SSL
                .buildConfiguration();

        // Connect and start the bot
        try (PircBotX bot = new PircBotX(configuration)) {
            bot.startBot();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onGenericMessage(GenericMessageEvent event) {

        if (event.getMessage().startsWith(".ud")) {
            String[] parts = event.getMessage().split(" ", 2);
            if (parts.length == 2) {
                String term = parts[1];
                String definition = "";
                try {
                    definition = searchUrbanDictionary(term);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                event.respond(definition);
            }
        }
    }


    private String searchUrbanDictionary(String term) throws IOException {
        String apiKey = "***REMOVED***";
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://mashape-community-urban-dictionary.p.rapidapi.com/define?term=" + term)
                .get()
                .addHeader("x-rapidapi-host", "mashape-community-urban-dictionary.p.rapidapi.com")
                .addHeader("x-rapidapi-key", apiKey)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.body() != null) {
                String jsonData = response.body().string();
                JsonElement jsonElement = JsonParser.parseString(jsonData);
                if (jsonElement.getAsJsonObject().get("list").getAsJsonArray().size() > 0) {
                    return jsonElement.getAsJsonObject().get("list").getAsJsonArray().get(0).getAsJsonObject().get("definition").getAsString();
                } else {
                    return "No definition found for " + term;
                }
            } else {
                return "Error: Response body is null";
            }
        }
    }

    @Override
    public void onJoin(JoinEvent event) {
        User user = event.getUser();
        if (user != null && user.getNick().equals("Loreley")) {
            event.getChannel().send().message("Here I am, Loreley your friendly IRC bot!");
        } else {
            event.getChannel().send().message("Welcome to herz.moe, have fun, be comfy and don't be a baka >.<");
        }
    }

    @Override
    public void onMessage(MessageEvent event) {
        User user = event.getUser();
        if (user == null) {
            return;
        }

        String messageText = event.getMessage();
        String sender = user.getNick();

        // If the message is a .tell command, handle it accordingly
        if (messageText.startsWith(".tell")) {
            String[] parts = messageText.split(" ", 3); // Split to get .tell, recipient, and message
            if (parts.length != 3) {
                event.respond("Invalid .tell command. Usage: .tell <nick> <message>");
            } else {
                String recipient = parts[1];
                String message = parts[2];

                // Check if recipient has too many messages to receive
                if (messagesToReceive.getOrDefault(recipient, 0) >= MAX_RECEIVED_MESSAGES) {
                    event.respond("This user has too many messages to receive.");
                    return;
                }
                // Check if sender has too many pending messages
                if (unsentMessages.containsKey(sender) && unsentMessages.get(sender).size() >= MAX_UNSENT_MESSAGES) {
                    event.respond("You have too many pending messages.");
                    return;
                }

                // Add the message to the recipient's queue of unsent messages
                unsentMessages.computeIfAbsent(recipient, k -> new LinkedList<>()).add(sender + ": " + message);
                messagesToReceive.put(recipient, messagesToReceive.getOrDefault(recipient, 0) + 1);

                // Add confirmation message
                event.getChannel().send().message("Your message will be delivered the next time " + recipient + " is online!");
            }
        }
        // If it's a regular message, check if there are any postponed messages for the user
        else {
            // Send the postponed messages in the desired format
            if (unsentMessages.containsKey(sender)) {
                LinkedList<String> messages = unsentMessages.get(sender);
                int messageCount = messages.size();
                int sentCount = 0;

                event.getChannel().send().message(sender + ", you have postponed messages: ");
                for (String message : messages) {
                    if(sentCount < 3){
                        event.getChannel().send().message(message);
                    } else {
                        // Send the remaining messages as DM.
                        event.getBot().sendIRC().message(user.getNick(), message);
                    }
                    sentCount++;
                }
                if(messageCount > 3){
                    event.getChannel().send().message("The remaining messages were sent via DM");
                }

                // Once messages are sent, clear them from the list
                unsentMessages.remove(sender);
                messagesToReceive.put(sender, 0); // Reset

            }
        }
    }
}