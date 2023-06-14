# BlockwartBot

A friendly IRC bot built in ![Java](https://img.shields.io/badge/-Java-red?logo=java&logoColor=white), named Loreley. 

The bot features functionality to fetch website metadata, parse urban dictionary terms, respond with cat kaomojis when 'meow' or 'nya' is used in the chat, send postponed messages and join IRC channels.

__Features__
- Fetch Website Metadata: Given a URL, it fetches and sends the title of the webpage.
- Urban Dictionary API Integration: Parses definitions from Urban Dictionary for a given term.
- Nya-Meow React: Responds with a random cat kaomoji when 'meow' or 'nya' is spotted in a message.
- Postponed Messages: Ability to take .tell commands and sends postponed messages to the intended user when they are active.
- Auto-join IRC Channels: Auto-joins the IRC channels #herz and #deutsch on server herz.moe.
- Russian Roulette: Start a fun game of Russian Roulette by sending a gun emoji (🔫).

__Commands__
- .ud <term>: The bot will respond with the definition of the term from Urban Dictionary.
- .tell <nick> <message>: The bot will store your message and deliver it to the specified user when they are active.

__Usage__
- To use Loreley in your own IRC channels, you will need to set up a `config.properties` file and a PostgreSQL database.

__config.properties__

Create a new file `config.properties` in the project root directory, and include the following lines:
    
    api.key=<your rapidapi urbandicitionary api key here>
    db.url=jdbc:postgresql://localhost:5432/loreleydb
    user=lore
    password=<your database password here>


## PostgreSQL Database Setup

Loreley requires a PostgreSQL database to manage the postponed messages feature. Here's a simple way to set it up:

1. Install PostgreSQL if you haven't already, and start the PostgreSQL service.
2. Connect to PostgreSQL using `psql` or your preferred client.
3. Create a new database with the name `loreleydb` (or any other name, but remember to change `db.url` in `config.properties`).
4. Create a new user `lore` with a password of your choice, and grant this user all privileges on the `loreleydb` database.
5. Create a table named `tell` with the following schema:

    ```
    CREATE TABLE tell (
        sender VARCHAR( 50 ) NOT NULL,
        recipient VARCHAR( 50 ) NOT NULL,
        message VARCHAR ( 500 ) NOT NULL,
        timestamp VARCHAR( 50 ) NOT NULL
    );
    ```

__Dependencies__
- [PircBotX](https://github.com/pircbotx/pircbotx): PircBotX is a multi-threaded, lightweight, feature packed IRC bot library.
- [OkHttp](https://github.com/square/okhttp): HTTP client for Java.
- [Jsoup](https://github.com/jhy/jsoup): Java HTML Parser for real-world HTML.
- [Google Gson](https://github.com/google/gson): A Java serialization/deserialization library to convert Java Objects into JSON and back.
- [PostgreSQL JDBC Driver](https://jdbc.postgresql.org/): JDBC driver to connect to the PostgreSQL database.
  

__Autor and contributors__

[herz.moe](https://herz.moe) & kaitou
