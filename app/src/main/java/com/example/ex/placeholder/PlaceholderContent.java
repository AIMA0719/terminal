package com.example.ex.placeholder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class PlaceholderContent {

    /**
     * An array of sample (placeholder) items.
     */
    public static final List<Customer2> ITEMS = new ArrayList<Customer2>();

    /**
     * A map of sample (placeholder) items, by ID.
     */
    public static final Map<String, Customer2> ITEM_MAP = new HashMap<String, Customer2>();

    private static final int COUNT = 25;

    static {
        // Add some sample items.
        for (int i = 1; i <= COUNT; i++) {
            addItem(createPlaceholderItem(i));
        }
    }

    private static void addItem(Customer2 item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    private static Customer2 createPlaceholderItem(int position) {
        return new Customer2(String.valueOf(position), "Item " + position, makeDetails(position));
    }

    private static String makeDetails(int position) {
        StringBuilder builder = new StringBuilder();
        builder.append("Details about Item: ").append(position);
        for (int i = 0; i < position; i++) {
            builder.append("\nMore details information here.");
        }
        return builder.toString();
    }

    /**
     * A placeholder item representing a piece of content.
     */
    public static class Customer2 {
        public final String id;
        public final String address;
        public final String details;

        public Customer2(String id, String content, String details) {
            this.id = id;
            this.address = content;
            this.details = details;
        }

        @Override
        public String toString() {
            return address;
        }
    }
}