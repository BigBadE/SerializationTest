package software.bigbade.serializationtest;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

@RequiredArgsConstructor
public enum SerializationLists {
    ITEM_META("{@", ItemMeta.class, (builder, value) -> {
        builder.append("{@");
        for (Map.Entry<String, Object> entry : ((ItemMeta) value).serialize().entrySet()) {
            SerializationUtils.serialize(builder, entry.getKey(), entry.getValue());
        }
        builder.append("}");
    }, SerializationUtils::getMeta),
    MAP("{", Map.class, (builder, value) -> {
        builder.append("{");
        for (Map.Entry<String, Object> entry : ((Map<String, Object>) value).entrySet()) {
            SerializationUtils.serialize(builder, entry.getKey(), entry.getValue());
        }
        builder.append("}");
    }, value -> value);

    @Getter
    private final String symbol;
    @Getter
    private final Class<?> clazz;
    private final BiConsumer<StringBuilder, Object> serializeConsumer;
    private final Function<Map<String, Object>, Object> deserializeFunction;

    public void serialize(StringBuilder builder, Object value) {
        serializeConsumer.accept(builder, value);
        builder.append("|");
    }

    public Object deserialize(Map<String, Object> data) {
        return deserializeFunction.apply(data);
    }
}
