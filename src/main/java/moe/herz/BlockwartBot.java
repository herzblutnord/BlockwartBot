package moe.herz;

import org.pircbotx.Configuration;
import org.pircbotx.PircBotX;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.types.GenericMessageEvent;
import org.pircbotx.hooks.events.JoinEvent;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.User;

import java.util.regex.Pattern;
import java.util.Random;
import java.util.Properties;
import java.io.FileInputStream;
import java.util.*;
import java.io.IOException;
import javax.net.ssl.SSLSocketFactory;
import java.util.regex.Matcher;
import java.net.URISyntaxException;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import okhttp3.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class BlockwartBot extends ListenerAdapter {

    private Properties properties; // Move properties declaration here
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private final String[] catKaomojis = {"^._.^", "/ᐠ｡▿｡ᐟ\\*ᵖᵘʳʳ*", "(=^-ω-^=)", "(=｀ェ´=)", "（Φ ω Φ）", "(˵Φ ω Φ˵)", "/ᐠ｡ꞈ｡ᐟ\\", "=^o.o^=", "/ᐠ_ ꞈ _ᐟ\\ɴʏᴀ~"};
    private static final int MAX_UNSENT_MESSAGES = 5;
    private static final int MAX_RECEIVED_MESSAGES = 10;

    private final Map<String, LinkedList<String>> unsentMessages = new HashMap<>();
    private final Map<String, Integer> messagesToReceive = new HashMap<>();

    public BlockwartBot() {
        try (FileInputStream in = new FileInputStream("./config.properties")) {
            properties = new Properties();
            properties.load(in);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);  // Exit if the properties file cannot be loaded
        }
    }

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

    public String fetchWebsiteMetadata(String url) {
        try {
            new URI(url);
            Document doc = Jsoup.connect(url).get();
            return doc.title();
        } catch (URISyntaxException exception) {
            return "Invalid URL: " + url;
        } catch (IOException e) {
            return "Error connecting to URL: " + url;
        }
    }

    @Override
    public void onGenericMessage(GenericMessageEvent event) {

        // Regular expression pattern to identify URLs in the message
        Pattern urlPattern = Pattern.compile("(https?://)?([\\da-z.-]+)\\.([a-z.]{2,6})([/\\w .-]*)*/?", Pattern.CASE_INSENSITIVE);

        Matcher matcher = urlPattern.matcher(event.getMessage());

        while (matcher.find()) {
            String url = matcher.group();
            if (!url.startsWith("http")) {
                url = "http://" + url; // Add http protocol if not present
            }

            // Skip non-HTML files
            String[] skippedExtensions = {".jpg",".jpeg", ".png", ".gif", ".bmp", ".webp", ".mp4", ".mp3", ".wav", ".ogg", ".flac", ".mkv", ".avi", ".flv"};
            boolean skip = false;
            for (String extension : skippedExtensions) {
                if (url.toLowerCase().endsWith(extension)) {
                    skip = true;
                    break;
                }
            }

            if (!skip) {
                String metadata = fetchWebsiteMetadata(url);
                if (event instanceof MessageEvent messageEvent) {
                    messageEvent.getBot().sendIRC().message(messageEvent.getChannel().getName(), metadata);
                }
            }
        }

        if (event.getMessage().startsWith(".ud")) {
            String[] parts = event.getMessage().split(" ", 2);
            if (parts.length == 2) {
                String term = parts[1];
                List<String> definitions = searchUrbanDictionary(term);
                int count = 0;
                for (String definition : definitions) {
                    if (count < 4) {
                        event.respond(definition);
                    } else {
                        break;
                    }
                    count++;
                }
                if (definitions.size() > 4) {
                    event.respond("... [message truncated due to length]");
                }
            }
        }

        Pattern p = Pattern.compile("(?i)^nya.*|meow");
        Matcher m = p.matcher(event.getMessage());
        if (m.find()) {
            Random rand = new Random();
            int index = rand.nextInt(catKaomojis.length);
            if (event instanceof MessageEvent messageEvent) {
                messageEvent.getBot().sendIRC().message(messageEvent.getChannel().getName(), catKaomojis[index]);
            }
            // Send message directly without username
        }
    }

    private List<String> searchUrbanDictionary(String term) {
        String apiKey = properties.getProperty("api.key");
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
                    String definition = jsonElement.getAsJsonObject().get("list").getAsJsonArray().get(0).getAsJsonObject().get("definition").getAsString();

                    List<String> definitionParts = new ArrayList<>();
                    String[] parts = definition.split("\\r?\\n");
                    Collections.addAll(definitionParts, parts);
                    return definitionParts;
                } else {
                    return Collections.singletonList("No definition found for " + term);
                }
            } else {
                return Collections.singletonList("Error: Response body is null");
            }
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.singletonList("Error connecting to Urban Dictionary API.");
        }
    }

    @Override
    public void onJoin(JoinEvent event) {
        User user = event.getUser();
        if (user != null && user.getNick().equals("Loreley")) {
            event.getChannel().send().message("Here I am, Loreley your friendly IRC bot! (Version 0.3)");
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
                String timestamp = LocalDateTime.now().format(TIME_FORMATTER);

                // Check if recipient has too many messages to receive
                if (messagesToReceive.getOrDefault(recipient, 0) >= MAX_RECEIVED_MESSAGES) {
                    event.respond("This user has too many messages to receive.");
                    return;
                }

                // Check if sender has too many pending messages for the recipient
                LinkedList<String> recipientMessages = unsentMessages.getOrDefault(recipient, new LinkedList<>());
                if (recipientMessages.stream().filter(m -> m.startsWith(sender + ":")).count() >= MAX_UNSENT_MESSAGES) {
                    event.respond("You have too many pending messages for this user.");
                    return;
                }

                // Add the message to the recipient's queue of unsent messages
                unsentMessages.computeIfAbsent(recipient, k -> new LinkedList<>()).add(sender + " (" + timestamp + "): " + message);
                messagesToReceive.put(recipient, messagesToReceive.getOrDefault(recipient, 0) + 1);

                // Add confirmation message
                event.getChannel().send().message("Your message will be delivered the next time " + recipient + " is here!");
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