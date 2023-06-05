# BlockwartBot

A friendly IRC bot built in Java, named Loreley.

[https://img.shields.io/badge/-Java-red?logo=java&logoColor=white]

The bot features functionality to fetch website metadata, parse urban dictionary terms, respond with cat kaomojis when 'meow' or 'nya' is used in the chat, send postponed messages and join IRC channels.

__Features
- Fetch Website Metadata: Given a URL, it fetches and sends the title of the webpage.
- Urban Dictionary API Integration: Parses definitions from Urban Dictionary for a given term.
- Nya-Meow React: Responds with a random cat kaomoji when 'meow' or 'nya' is spotted in a message.
- Postponed Messages: Ability to take .tell commands and sends postponed messages to the intended user when they are active.
- Auto-join IRC Channels: Auto-joins the IRC channel #herz on server herz.moe.

__Usage
- Add a config.properties file with the appropriate API keys for urban dictionary functionality

__Commands
.ud <term>: The bot will respond with the definition of the term from Urban Dictionary.
.tell <nick> <message>: The bot will store your message and deliver it to the specified user when they are active.
  
__Dependencies
- PircBotX: A PircBotX is a multi-threaded, lightweight, feature packed IRC bot library. Link: [https://github.com/pircbotx/pircbotx]
- OkHttp: HTTP client for Java. Link: [https://github.com/square/okhttp]
- Jsoup: Java HTML Parser for real-world HTML. Link: [https://github.com/jhy/jsoup]
- Google Gson: A Java serialization/deserialization library to convert Java Objects into JSON and back. Link: [https://github.com/google/gson]
  

Author
moe.herz
