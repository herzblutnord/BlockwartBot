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
import java.time.format.DateTimeFormatter;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import okhttp3.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class BlockwartBot extends ListenerAdapter {

    // Defined constants
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm")
            .withZone(ZoneId.of("Europe/Berlin"));
    private static final int MAX_UNSENT_MESSAGES = 5;
    private static final int MAX_RECEIVED_MESSAGES = 10;
    private final String[] catKaomojis = {"^._.^", "/ᐠ｡▿｡ᐟ\\*ᵖᵘʳʳ*", "(=^-ω-^=)", "(=｀ェ´=)",
            "（Φ ω Φ）", "(˵Φ ω Φ˵)", "/ᐠ｡ꞈ｡ᐟ\\", "=^o.o^=", "/ᐠ_ ꞈ _ᐟ\\ɴʏᴀ~", "/ᐠ - ˕ -マ Ⳋ", "ฅ^•ﻌ•^ฅ", "ᓚᘏᗢ", "≽ܫ≼"};

    //Properties object to hold configuration properties
    private Properties properties;

    // Hashmaps to manage messages
    private final Map<String, LinkedList<String>> unsentMessages = new HashMap<>();
    private final Map<String, Integer> messagesToReceive = new HashMap<>();

    // Constructor to initialize properties
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

        String botName = "LoreleyWIP";

        // Configure the bot
        Configuration configuration = new Configuration.Builder()
                .setName(botName)
                .addServer("herz.moe", 6697)
                .addAutoJoinChannel("#herz")
                .addAutoJoinChannel("#deutsch")
                .addListener(new BlockwartBot())
                .setSocketFactory(SSLSocketFactory.getDefault()) // Enable SSL
                .buildConfiguration();

        // Start the bot
        try (PircBotX bot = new PircBotX(configuration)) {
            bot.startBot();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    // Method to fetch website metadata
    public String fetchWebsiteMetadata(String url) {
        try {
            new URI(url);
            Document doc = Jsoup.connect(url).get();
            return doc.title();
        } catch (URISyntaxException exception) {
            return "Invalid URL";
        } catch (IOException e) {
            return "Error connecting to URL: ";
        }
    }

    // Method to handle general message events
    @Override
    public void onGenericMessage(GenericMessageEvent event) {

        // Regular expression pattern to identify URLs in the message
        Pattern urlPattern = Pattern.compile("(https?://[\\w.-]+\\.[\\w.-]+[\\w./?=&#%-]*)", Pattern.CASE_INSENSITIVE);

        Matcher matcher = urlPattern.matcher(event.getMessage());

        while (matcher.find()) {
            String url = matcher.group();

            // Trimming trailing text (if any) from the URL
            int spaceIndex = url.indexOf(' ');
            if (spaceIndex != -1) {
                url = url.substring(0, spaceIndex);
        }

            // Skip non-HTML files
            String[] skippedExtensions = {".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp", ".mp4", ".mp3", ".wav", ".ogg", ".flac", ".mkv", ".avi", ".flv"};
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

        // Method to search Urban Dictionary for a term
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

        //nya-meow react
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
    //urban dictionary api
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

    // Greeting on bot joining
    @Override
    public void onJoin(JoinEvent event) {
        User user = event.getUser();
        if (user != null && user.getNick().equals("Loreley")) {
            event.getChannel().send().message("Here I am, Loreley your friendly IRC bot! (Version 0.4)");
        }
    }

    // Method to handle message events
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
                String timestamp = ZonedDateTime.now().format(TIME_FORMATTER);

                // Check if the recipient is the sender themselves or the bot
                if (recipient.equalsIgnoreCase(sender)) {
                    event.respond("Aww, talking to yourself? How pityful...");
                    return;
                } else if (recipient.equalsIgnoreCase(event.getBot().getNick())) {
                    event.respond("I am right here, baka!");
                    return;
                }

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
