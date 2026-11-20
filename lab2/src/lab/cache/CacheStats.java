package lab.cache;

// Незмінний знімок статистики кешу.
public record CacheStats(long hits, long misses, long evictions, long expirations) {

    public double hitRate() {
        long total = hits + misses;
        return total == 0 ? 1.0 : (double) hits / total;
    }

    @Override
    public String toString() {
        return String.format("hits=%d, misses=%d, hitRate=%.0f%%, evictions=%d, expirations=%d",
                hits, misses, hitRate() * 100, evictions, expirations);
    }
}
