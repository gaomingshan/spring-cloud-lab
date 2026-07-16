package com.lab.foundation.id;

public record Identifier(String value) {
    public Identifier {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Identifier value must not be blank");
    }
}
