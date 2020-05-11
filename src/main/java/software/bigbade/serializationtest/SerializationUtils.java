package software.bigbade.serializationtest;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nonnull;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringTokenizer;

/**
 * Handles serialization for the entire plugin.
 * Format:
 * [ Start of item
 * [#; Start of item with slot
 * #|[ Start of array with length
 * = Between the key and value
 * ; Marks end of value
 * | Marks beginning/end of non-list value
 * {} Marks beginning/end of map
 * { Marks beginning of ItemMeta
 * ] Marks end of item
 * < Marks beginning of list
 * & Seperates lines of list
 * > Marks end of list
 *
 */
public final class SerializationUtils {

    /**
     * Random strings to replace reserved characters with
     */
    public static final String AND_REPLACEMENT = "\uD83E\uDED8";
    public static final String END_CHAR_REPLACEMENT = "\uD83E\uDED9";

    private SerializationUtils() { }

    private static final Class<?> serializeClass = ReflectionUtils.getClass("org.bukkit.craftbukkit.v1_15_R1.inventory.CraftMetaItem$SerializableMeta");
    private static final Method serializeMeta = ReflectionUtils.getMethod("deserialize", serializeClass, Map.class);

    /**
     * Serializes location
     */
    public static String serializeLocation(Location location) {
        return location.getWorld().getName() + "|" + new String(DoubleUtils.doubleToBytes(location.getX())) + "|" + new String(DoubleUtils.doubleToBytes(location.getY())) + "|" + new String(DoubleUtils.doubleToBytes(location.getZ()));
    }

    /**
     * Deserializes location
     */
    public static Location deserializationLocation(String data) {
        StringTokenizer tokenizer = new StringTokenizer(data, "|");
        return new Location(Bukkit.getWorld(tokenizer.nextToken()), DoubleUtils.bytesToDouble(tokenizer.nextToken().getBytes()), DoubleUtils.bytesToDouble(tokenizer.nextToken().getBytes()), DoubleUtils.bytesToDouble(tokenizer.nextToken().getBytes()));
    }


    /**
     * Deserializes given data
     *
     * @param data Serialized data
     * @return Map of slot numbers and itemstacks
     */
    public static Map<Integer, ItemStack> deserializeItemMap(@Nonnull String data) {
        Map<Integer, ItemStack> found = new HashMap<>();
        StringTokenizer tokenizer = new StringTokenizer(data, "[");
        while (tokenizer.hasMoreTokens()) {
            String current = tokenizer.nextToken();
            current = current.replaceFirst("\\[", "");
            int slot = Integer.parseInt(current.split(";", 2)[0]);
            current = current.split(";", 2)[1];
            Map<String, Object> map = deserializeMap(current);
            found.put(slot, ItemStack.deserialize(map));
        }
        return found;
    }

    /**
     * Deserializes inventory
     *
     * @param inventory Inventory to put items in
     * @param data      Serialized data
     */
    public static void deserializeInventory(Inventory inventory, @Nonnull String data) {
        StringTokenizer tokenizer = new StringTokenizer(data, "[");
        while (tokenizer.hasMoreTokens()) {
            String current = tokenizer.nextToken();
            int slot = Integer.parseInt(current.split(";", 2)[0]);
            current = current.split(";", 2)[1];
            Map<String, Object> map = deserializeMap(current);
            ItemStack item = ItemStack.deserialize(map);
            inventory.setItem(slot, item);
        }
    }

    /**
     * Deserializes given data
     *
     * @param data Serialized data
     * @return List of items
     */
    public static List<ItemStack> deserializeList(String data) {
        if(data == null || data.isEmpty()) {
            return Collections.emptyList();
        }

        List<ItemStack> found = new ArrayList<>();
        StringTokenizer tokenizer = new StringTokenizer(data, "[");
        while (tokenizer.hasMoreTokens()) {
            String current = tokenizer.nextToken();
            current = current.replaceFirst("\\[", "");
            Map<String, Object> map = deserializeMap(current);
            found.add(ItemStack.deserialize(map));
        }
        return found;
    }

    /**
     * Deserializes given data
     *
     * @param data Serialized data
     * @return Array of items
     */
    public static ItemStack[] deserializeArray(@Nonnull String data) {
        if (data.isEmpty()) {
            return new ItemStack[0];
        }

        StringTokenizer splitData = new StringTokenizer(data, "?");
        ItemStack[] found = new ItemStack[Integer.parseInt(splitData.nextToken())];
        if(!splitData.hasMoreTokens())
            return found;
        int i = 0;
        StringTokenizer tokenizer = new StringTokenizer(splitData.nextToken(), "[");
        while (tokenizer.hasMoreTokens()) {
            String current = tokenizer.nextToken();
            current = current.replaceFirst("\\[", "");
            Map<String, Object> map = deserializeMap(current);
            found[i] = ItemStack.deserialize(map);
            i++;
        }
        return found;
    }

    public static ItemStack deserializeItem(String data) {
        if(data == null || data.isEmpty())
            return null;
        return ItemStack.deserialize(deserializeMap(data));
    }

    /**
     * Takes a serialized map and deserializes it
     *
     * @param current Serialized map
     * @return Deserialized version
     */
    public static Map<String, Object> deserializeMap(String current) {
        StringTokenizer tokenizer = new StringTokenizer(current, ";");
        return checkObject(tokenizer);
    }

    private static Map<String, Object> checkObject(StringTokenizer tokenizer) {
        Map<String, Object> data = new HashMap<>();
        while (tokenizer.hasMoreTokens()) {
            String pair = tokenizer.nextToken();
            if(pair.charAt(0) == ']' || pair.charAt(0) == '}') {
                break;
            }
            String[] split = pair.split("=", 2);
            if(split[1].isEmpty()) {
                break;
            }
            if(split[1].charAt(0) == '{') {
                for(SerializationLists list : SerializationLists.values()) {
                    if(split[1].startsWith(list.getSymbol())) {
                        Map<String, Object> objects = checkObject(tokenizer);
                        String[] firstKey = split[1].substring(list.getSymbol().length()).split("=");
                        objects.put(firstKey[0], deserializeValue(firstKey[1]));
                        Object object = list.deserialize(objects);
                        data.put(split[0], object);
                        break;
                    }
                }
            } else {
                data.put(split[0], deserializeValue(split[1]));
            }
        }
        return data;
    }

    private static Object deserializeValue(String serializedValue) {
        //Default NBT Tag (?)
        if(serializedValue.equals("@")) {
            return "H4sIAAAAAAAAAONiYGBkYCsuSSzJTGZkAAAcAb5RDgAAAA==";
        }
        StringTokenizer tokenizer = new StringTokenizer(serializedValue, "|");
        String foundSymbol = tokenizer.nextToken();
        for (SerializationTypes types : SerializationTypes.values()) {
            if (foundSymbol.equals(types.getSymbol())) {
                return types.getCast().apply(tokenizer.nextToken());
            }
        }
        return foundSymbol;
    }

    /**
     * Deserializes an item meta using reflection
     *
     * @param data Serialized item meta
     * @return deserialized item meta
     */
    public static ItemMeta getMeta(Map<String, Object> data) {
        return (ItemMeta) ReflectionUtils.callMethod(serializeMeta, data);
    }

    /**
     * Serializes list of items
     *
     * @param data list of items
     * @return Serialized list
     */
    public static String serializeList(List<ItemStack> data) {
        StringBuilder builder = new StringBuilder();
        for (ItemStack item : data) {
            builder.append("[");
            for (Map.Entry<String, Object> itemEntry : item.serialize().entrySet()) {
                serialize(builder, itemEntry.getKey(), itemEntry.getValue());
            }
            builder.append("]");
        }
        return builder.toString();
    }

    /**
     * Serializes item
     *
     * @param item Item to serialize
     * @return Serialized item
     */
    public static String serializeItem(ItemStack item) {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, Object> itemEntry : item.serialize().entrySet()) {
            serialize(builder, itemEntry.getKey(), itemEntry.getValue());
        }
        return builder.toString();
    }

    /**
     * Serializes map of slots and items
     *
     * @param data map of slots and the items in the slot
     * @return Serialized vault
     */
    public static String serializeItemMap(Map<Integer, ItemStack> data) {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<Integer, ItemStack> entry : data.entrySet()) {
            builder.append("[").append(entry.getKey()).append(";");
            for (Map.Entry<String, Object> itemEntry : entry.getValue().serialize().entrySet()) {
                serialize(builder, itemEntry.getKey(), itemEntry.getValue());
            }
            builder.append("]");
        }
        return builder.toString();
    }

    /**
     * Serializes inventory
     *
     * @param inventory Inventory to serialize
     * @return Serialized inventory
     */
    public static String serializeInventory(Inventory inventory, boolean ignorePokemonSlots) {
        StringBuilder builder = new StringBuilder();
        int start = (ignorePokemonSlots) ? 6 : 0;
        for (int i = start; i < inventory.getSize(); i++) {
            ItemStack item = inventory.getItem(i);
            if(item == null)
                continue;
            builder.append("[").append(i).append(";");
            serializeIterableItem(builder, item);
        }
        return builder.toString();
    }

    private static void serializeIterableItem(StringBuilder builder, ItemStack item) {
        if (item == null)
            return;
        for (Map.Entry<String, Object> itemEntry : item.serialize().entrySet()) {
            serialize(builder, itemEntry.getKey(), itemEntry.getValue());
        }
        builder.append("]");
    }

    /**
     * Serializes item array
     *
     * @param items items to serialize
     * @return Serialized array
     */
    public static String serializeArray(ItemStack[] items) {
        StringBuilder builder = new StringBuilder("" + items.length).append("?");
        for (ItemStack item : items) {
            if (item == null)
                continue;
            builder.append("[");
            serializeIterableItem(builder, item);
        }
        return builder.toString();
    }

    /**
     * serialized key/value pair and adds it to given builder
     *
     * @param builder Where the key/value pair should be witten to
     * @param key     The key
     * @param value   The value
     */
    public static void serialize(StringBuilder builder, String key, Object value) {
        builder.append(key).append("=");
        //Internal key has constant value
        if(key.equals("internal")) {
            builder.append("@;");
            return;
        }

        for (SerializationLists lists : SerializationLists.values()) {
            if (lists.getClazz().isAssignableFrom(value.getClass())) {
                lists.serialize(builder, value);
                builder.append(";");
                return;
            }
        }

        builder.append("|");
        for (SerializationTypes types : SerializationTypes.values()) {
            if (types.getClazz().isAssignableFrom(value.getClass())) {
                builder.append(types.getSymbol()).append("|");
                types.serialize(builder, value);
                return;
            }
        }
    }
}
