package me.vaperion.inspector.utils;

import lombok.Getter;

import java.util.Map;

@Getter
public class Tuple<A, B> implements Map.Entry<A, B> {
    private final A key;
    private final B value;

    public Tuple(A key, B value) {
        this.key = key;
        this.value = value;
    }

    @Override
    @Deprecated
    public B setValue(B value) {
        throw new UnsupportedOperationException("setvalue");
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof Tuple)){
            return false;
        }

        Tuple<A, B> tuple = (Tuple<A ,B>) other;
        return tuple.getKey() == this.getKey() && tuple.getValue() == this.getValue();
    }
}