package com.lab.foundation.id;

import java.util.UUID;

public final class Identifiers {
    private Identifiers() { }
    public static Identifier random() { return new Identifier(UUID.randomUUID().toString()); }
}
