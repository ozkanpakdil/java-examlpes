package io.github.ozkanpakdil;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import java.util.*;

public class Main {
    private static final int MAX_FILENAME_LEN = 256;
    private static final int DISPLAY_LIMIT = 50;

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.out.println("Usage: java -cp ... io.github.ozkanpakdil.Main <path_to_bpf_so>");
            return;
        }

        String bpfObjPath = args[0];
        LibBpf lib = LibBpf.INSTANCE;

        Pointer obj = lib.bpf_object__open(bpfObjPath);
        if (obj == null) {
            System.err.println("Failed to open BPF object");
            return;
        }

        if (lib.bpf_object__load(obj) < 0) {
            System.err.println("Failed to load BPF object");
            return;
        }

        // Attach probes
        String[] progs = {"vfs_read", "vfs_write"};
        for (String progName : progs) {
            Pointer prog = lib.bpf_object__find_program_by_name(obj, progName);
            if (prog == null) {
                System.err.println("Failed to find program " + progName);
                continue;
            }
            if (lib.bpf_program__attach(prog) == null) {
                System.err.println("Failed to attach program " + progName);
            }
        }

        int fd = lib.bpf_object__find_map_fd_by_name(obj, "file_map");
        if (fd < 0) {
            System.err.println("Failed to find map file_map");
            return;
        }

        System.out.println("Observing File I/O Heatmap... Press Ctrl+C to stop.");

        HeatmapTUI tui = new HeatmapTUI();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                tui.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }));

        long lastUpdate = 0;
        while (true) {
            if (!tui.pollInput()) {
                break;
            }

            long now = System.currentTimeMillis();
            if (now - lastUpdate > 2000) {
                Map<String, FileStats> stats = collectStats(lib, fd);
                tui.updateData(stats);
                lastUpdate = now;
            }
            Thread.sleep(50);
        }
        tui.stop();
        System.exit(0);
    }

    private static Map<String, FileStats> collectStats(LibBpf lib, int fd) {
        Map<String, FileStats> statsMap = new HashMap<>();
        Pointer curKey = null;
        Pointer nextKey = new Memory(MAX_FILENAME_LEN);

        while (lib.bpf_map_get_next_key(fd, curKey, nextKey) == 0) {
            byte[] keyBytes = nextKey.getByteArray(0, MAX_FILENAME_LEN);
            int end = 0;
            while (end < keyBytes.length && keyBytes[end] != 0) end++;
            String filename = new String(keyBytes, 0, end);

            Pointer value = new Memory(32); // 4 * u64
            if (lib.bpf_map_lookup_elem(fd, nextKey, value) == 0) {
                FileStats stats = new FileStats();
                stats.reads = value.getLong(0);
                stats.writes = value.getLong(8);
                stats.readBytes = value.getLong(16);
                stats.writeBytes = value.getLong(24);
                statsMap.put(filename, stats);
            }

            if (curKey == null) curKey = new Memory(MAX_FILENAME_LEN);
            curKey.write(0, keyBytes, 0, MAX_FILENAME_LEN);
        }
        return statsMap;
    }

    private static void printHeatmap(Map<String, FileStats> statsMap) {
        System.out.print("\033[H\033[2J");
        System.out.flush();
        System.out.println(String.format("%-50s %10s %10s %15s %15s", "FILENAME", "READS", "WRITES", "R-BYTES", "W-BYTES"));
        System.out.println("-".repeat(110));

        statsMap.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue().readBytes + b.getValue().writeBytes,
                                             a.getValue().readBytes + a.getValue().writeBytes))
                .limit(DISPLAY_LIMIT)
                .forEach(e -> {
                    FileStats s = e.getValue();
                    System.out.println(String.format("%-50s %10d %10d %15d %15d",
                            e.getKey(), s.reads, s.writes, s.readBytes, s.writeBytes));
                });
    }

    static class FileStats {
        long reads;
        long writes;
        long readBytes;
        long writeBytes;
    }
}
