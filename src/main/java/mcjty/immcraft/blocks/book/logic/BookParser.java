package mcjty.immcraft.blocks.book.logic;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import mcjty.immcraft.ImmersiveCraft;
import org.apache.logging.log4j.Level;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BookParser {

    private List<BookSection> parseSections(File file) {
        FileInputStream inputstream;
        try {
            inputstream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            ImmersiveCraft.logger.log(Level.ERROR, "Error reading file: " + file.getName());
            return Collections.emptyList();
        }
        return parseSections(file.getName(), inputstream);
    }

    private List<BookSection> parseSections(String name, InputStream inputstream) {
        BufferedReader br;
        try {
            br = new BufferedReader(new InputStreamReader(inputstream, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            ImmersiveCraft.logger.log(Level.ERROR, "Error reading file: " + name);
            return Collections.emptyList();
        }

        List<BookSection> sections = new ArrayList<>();

        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(br);
        for (JsonElement entry : element.getAsJsonArray()) {
            JsonObject object = entry.getAsJsonObject();
            JsonElement sectionElement = object.get("section");
            if (sectionElement == null) {
                ImmersiveCraft.logger.log(Level.ERROR, "Missing section name in: " + name);
                return Collections.emptyList();
            }
            BookSection section = new BookSection(sectionElement.getAsString());
            JsonElement textElement = object.get("text");
            if (textElement != null) {
                for (JsonElement textChild : textElement.getAsJsonArray()) {
                    String string = textChild.getAsString();
                    if (string.startsWith("#i:")) {
                        // Item
                    } else {
                        section.addElement(new BookElementText(string));
                    }
                }
            }
        }

        return sections;
    }


    public List<BookPage> parse(String text, int width, int height) {
        InputStream inputstream = ImmersiveCraft.class.getResourceAsStream("/assets/immcraft/text/examplebook.json");
        List<BookSection> sections = parseSections("builtin", inputstream);
//        File file = new File(directory.getPath() + File.separator + "rftools", "dimlets.json");

        List<BookPage> pages = new ArrayList<>();

        return pages;
    }
}
