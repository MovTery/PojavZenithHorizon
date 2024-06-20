package com.movtery.pojavzh.utils.stringutils;

import androidx.annotation.NonNull;

/**
 * A utility class to join strings with a specified delimiter, prefix, and suffix.
 * This class provides a way to construct a concatenated string from multiple elements,
 * with optional prefix and suffix.
 */
public class SimpleStringJoiner {
    private final String delimiter;
    private final String prefix;
    private final String suffix;
    private final StringBuilder value = new StringBuilder();

    public SimpleStringJoiner(String delimiter) {
        this(delimiter, null, null);
    }

    /**
     * Constructs a new {@code SimpleStringJoiner} instance with the specified delimiter, prefix, and suffix.
     * The resulting string will have the specified prefix at the beginning and the specified suffix at the end.
     *
     * @param delimiter the delimiter to use between joined elements, must not be null
     * @param prefix the prefix to be added before the joined string, may be null
     * @param suffix the suffix to be added after the joined string, may be null
     */
    public SimpleStringJoiner(String delimiter, String prefix, String suffix) {
        this.delimiter = delimiter != null ? delimiter : "";
        this.prefix = prefix != null ? prefix : "";
        this.suffix = suffix != null ? suffix : "";
    }

    public void join(String newElement) {
        if (this.value.length() == 0) {
            this.value.append(newElement);
        } else {
            this.value.append(this.delimiter).append(newElement);
        }
    }

    public void reset() {
        this.value.setLength(0);
    }

    public String getValue() {
        return this.prefix + this.value + this.suffix;
    }

    @NonNull
    @Override
    public String toString() {
        return getValue();
    }
}
