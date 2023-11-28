package org.example;

public class Main {
    public static void main(String[] args) {
        StringBuilder sb = new StringBuilder();
        sb.appendCodePoint(0x1F600); // Grinning face
        sb.appendCodePoint(0x1F601); // Grinning face with big eyes
        sb.appendCodePoint(0x1F602); // Grinning face with tears
        sb.appendCodePoint(0x1F923); // Rolling on the floor laughing
        sb.appendCodePoint(0x1F970); // Smiling face with hearts
        sb.appendCodePoint(0x1F60D); // Smiling face with heart-eyes
        sb.appendCodePoint(0x1F929); // Star-struck
        sb.appendCodePoint(0x1F618); // Face blowing a kiss
        sb.appendCodePoint(0x1F617); // Kissing face
        sb.appendCodePoint(0x263A); // Smiling face
        System.out.println(sb);

        var codePoint = Character.codePointAt("😃", 0);
        var isEmoji = Character.isEmoji(codePoint);
        System.out.println("😃 is an emoji: " + isEmoji);

        int[] surrogates = { 0xD83D, 0xDC7D };
        String alienEmojiString = new String(surrogates, 0, surrogates.length);
        System.out.println(alienEmojiString);
    }
}