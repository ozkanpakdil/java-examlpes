package io.github.ozkanpakdil;

import lombok.Data;

@Data
public class Item {

    private final String data;

    public Item(String data) {
        this.data = data;
    }
}
