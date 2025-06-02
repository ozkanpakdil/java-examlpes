import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SpecialKV {
    ConcurrentHashMap<String, KV> map = new ConcurrentHashMap<>();
    private long expiryWindow;

    public static class KV {
        long timeStamp;
        int value;

        public KV(long timeStamp, int value) {
            this.timeStamp = timeStamp;
            this.value = value;
        }
    }

    public void setExpiryWindow(long expiryWindow) {
        this.expiryWindow = expiryWindow;
    }

    public void put(String key, int value) {
        cleanExpired();
        KV kv = new KV(System.currentTimeMillis(), value);
        map.put(key, kv);
    }

    public int get(String key) {
        return map.get(key).value;
    }

    public double average() {
        if (map.isEmpty()) {
            return 0;
        }
        cleanExpired();
        double sum = 0;
        for (KV kv : map.values()) {
            sum += kv.value;
        }
        return sum / map.size();
    }

    private void cleanExpired() {
        for (Map.Entry<String, KV> kv : map.entrySet()) {
            if (System.currentTimeMillis() - kv.getValue().timeStamp > expiryWindow) {
                map.remove(kv.getKey());
            }
        }
    }

    public static void main(String[] args) throws Exception {
        SpecialKV kv = new SpecialKV();
        kv.setExpiryWindow(1000);
        kv.put("a", 1);
        kv.put("a", 9);
        kv.put("a", 1);
        kv.put("b", 2);
        kv.put("b", 20);
        Thread.sleep(1000);
        kv.put("c", 3);
        kv.put("d", 4);
        System.out.println(kv.average());
    }
}
