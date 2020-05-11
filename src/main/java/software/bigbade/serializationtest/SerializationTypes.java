package software.bigbade.serializationtest;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

@RequiredArgsConstructor
public enum SerializationTypes {
    NUMBER("@", Integer::parseInt, StringBuilder::append, Number.class),
    COLLECTION("<", value -> {
        List<String> list = new ArrayList<>();
        for (String line : value.substring(2).split("&"))
            list.add(line.replace(SerializationUtils.END_CHAR_REPLACEMENT, ";").replace(SerializationUtils.AND_REPLACEMENT, "&"));
        return list;
    }, (builder, value) -> {
        Collection<?> collection = (Collection<?>) value;
        for (Object object : collection) {
            builder.append(object.toString().replace(">", SerializationUtils.END_CHAR_REPLACEMENT).replace("&", SerializationUtils.AND_REPLACEMENT)).append("&");
        }
        builder.insert(builder.length()-1, ">");
    }, Iterable.class),
    BOOLEAN("&", value -> Boolean.parseBoolean(value) ? 1 : 0, (builder, value) -> {
        if ((boolean) value) {
            builder.append(1);
        } else {
            builder.append(0);
        }
    }, Boolean.class),
    DEFAULT("", value -> value.replace(SerializationUtils.END_CHAR_REPLACEMENT, ">"), (builder, value) -> builder.append(value.toString().replace(";", SerializationUtils.END_CHAR_REPLACEMENT)), Object.class);

    @Getter
    private final String symbol;
    @Getter
    private final Function<String, Object> cast;

    private final BiConsumer<StringBuilder, Object> serialize;
    @Getter
    private final Class<?> clazz;

    public void serialize(StringBuilder builder, Object object) {
        serialize.accept(builder, object);
        builder.append("|;");
    }
}
