package io.github.ozkanpakdil;

import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;

import java.io.IOException;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;

public class HeatmapTUI {
    public enum SortOrder {
        READS, WRITES, R_BYTES, W_BYTES, TOTAL
    }

    private Screen screen;
    private Terminal terminal;
    private SortOrder sortOrder = SortOrder.TOTAL;

    public HeatmapTUI() throws IOException {
        terminal = new DefaultTerminalFactory().createTerminal();
        screen = new TerminalScreen(terminal);
        screen.startScreen();
        screen.setCursorPosition(null);
    }

    public SortOrder getSortOrder() {
        return sortOrder;
    }

    public boolean pollInput() throws IOException {
        KeyStroke ks = screen.pollInput();
        if (ks == null) return true;

        if (ks.getKeyType() == KeyType.Character) {
            char c = Character.toLowerCase(ks.getCharacter());
            switch (c) {
                case 'q': return false;
                case '1': sortOrder = SortOrder.READS; break;
                case '2': sortOrder = SortOrder.WRITES; break;
                case '3': sortOrder = SortOrder.R_BYTES; break;
                case '4': sortOrder = SortOrder.W_BYTES; break;
                case '5': sortOrder = SortOrder.TOTAL; break;
            }
        } else if (ks.getKeyType() == KeyType.EOF) {
            return false;
        } else if (ks.isCtrlDown() && ks.getKeyType() == KeyType.Character && ks.getCharacter() == 'c') {
            return false;
        }
        return true;
    }

    public void updateData(Map<String, Main.FileStats> statsMap) throws IOException {
        TextGraphics tg = screen.newTextGraphics();
        screen.clear();

        int width = screen.getTerminalSize().getColumns();
        int height = screen.getTerminalSize().getRows();

        // Header
        tg.enableModifiers(SGR.BOLD);
        tg.putString(0, 0, String.format("%-50s %10s %10s %15s %15s %15s", 
            "FILENAME", "READS", "WRITES", "R-BYTES", "W-BYTES", "TOTAL"));
        tg.disableModifiers(SGR.BOLD);
        tg.putString(0, 1, "-".repeat(width));

        Comparator<Map.Entry<String, Main.FileStats>> comparator = switch (sortOrder) {
            case READS -> (a, b) -> Long.compare(b.getValue().reads, a.getValue().reads);
            case WRITES -> (a, b) -> Long.compare(b.getValue().writes, a.getValue().writes);
            case R_BYTES -> (a, b) -> Long.compare(b.getValue().readBytes, a.getValue().readBytes);
            case W_BYTES -> (a, b) -> Long.compare(b.getValue().writeBytes, a.getValue().writeBytes);
            case TOTAL -> (a, b) -> Long.compare(b.getValue().readBytes + b.getValue().writeBytes,
                                                a.getValue().readBytes + a.getValue().writeBytes);
        };

        var sortedStats = statsMap.entrySet().stream()
                .sorted(comparator)
                .limit(Math.min(50, height - 4))
                .collect(Collectors.toList());

        long maxVal = switch (sortOrder) {
            case READS -> sortedStats.stream().mapToLong(e -> e.getValue().reads).max().orElse(1);
            case WRITES -> sortedStats.stream().mapToLong(e -> e.getValue().writes).max().orElse(1);
            case R_BYTES -> sortedStats.stream().mapToLong(e -> e.getValue().readBytes).max().orElse(1);
            case W_BYTES -> sortedStats.stream().mapToLong(e -> e.getValue().writeBytes).max().orElse(1);
            case TOTAL -> sortedStats.stream().mapToLong(e -> e.getValue().readBytes + e.getValue().writeBytes).max().orElse(1);
        };
        if (maxVal == 0) maxVal = 1;

        int row = 2;
        for (var entry : sortedStats) {
            String filename = entry.getKey();
            Main.FileStats s = entry.getValue();
            long total = s.readBytes + s.writeBytes;

            long currentVal = switch (sortOrder) {
                case READS -> s.reads;
                case WRITES -> s.writes;
                case R_BYTES -> s.readBytes;
                case W_BYTES -> s.writeBytes;
                case TOTAL -> total;
            };

            float intensity = (float) currentVal / maxVal;
            
            // Heatmap color: white to red
            int red = 255;
            int green = (int) (255 * (1 - intensity));
            int blue = (int) (255 * (1 - intensity));
            
            tg.setBackgroundColor(new TextColor.RGB(red, green, blue));
            tg.setForegroundColor(intensity > 0.5 ? TextColor.ANSI.WHITE : TextColor.ANSI.BLACK);

            String line = String.format("%-50s %10d %10d %15d %15d %15d",
                    truncate(filename, 50), s.reads, s.writes, s.readBytes, s.writeBytes, total);
            tg.putString(0, row++, line);
        }

        tg.setBackgroundColor(TextColor.ANSI.DEFAULT);
        tg.setForegroundColor(TextColor.ANSI.DEFAULT);
        tg.putString(0, height - 2, "Sorting by: " + sortOrder + " (Press 1-5 to change)");
        tg.putString(0, height - 1, "Keys: [1]Reads [2]Writes [3]R-Bytes [4]W-Bytes [5]Total | [Q]Quit. Total files: " + statsMap.size());

        screen.refresh();
    }

    private String truncate(String s, int n) {
        if (s.length() <= n) return s;
        return "..." + s.substring(s.length() - n + 3);
    }

    public void stop() throws IOException {
        screen.stopScreen();
    }
}
