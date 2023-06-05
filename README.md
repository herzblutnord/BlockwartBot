# BlockwartBot

A friendly IRC bot built in Java, named Loreley.

![Java](https://img.shields.io/badge/-Java-red?logo=java&logoColor=white)

The bot features functionality to fetch website metadata, parse urban dictionary terms, respond with cat kaomojis when 'meow' or 'nya' is used in the chat, send postponed messages and join IRC channels.

__Features__
- Fetch Website Metadata: Given a URL, it fetches and sends the title of the webpage.
- Urban Dictionary API Integration: Parses definitions from Urban Dictionary for a given term.
- Nya-Meow React: Responds with a random cat kaomoji when 'meow' or 'nya' is spotted in a message.
- Postponed Messages: Ability to take .tell commands and sends postponed messages to the intended user when they are active.
- Auto-join IRC Channels: Auto-joins the IRC channel #herz on server herz.moe.

__Usage__
- Add a config.properties file with the appropriate API keys for urban dictionary functionality

__Commands__
.ud <term>: The bot will respond with the definition of the term from Urban Dictionary.
.tell <nick> <message>: The bot will store your message and deliver it to the specified user when they are active.
  
__Dependencies__
- [PircBotX](https://github.com/pircbotx/pircbotx): PircBotX is a multi-threaded, lightweight, feature packed IRC bot library.
- [OkHttp](https://github.com/square/okhttp): HTTP client for Java.
- [Jsoup](https://github.com/jhy/jsoup): Java HTML Parser for real-world HTML.
- [Google Gson](https://github.com/google/gson): A Java serialization/deserialization library to convert Java Objects into JSON and back.
  

__Author__
  
moe.herz
