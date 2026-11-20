package io.tags;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;

/*
 Результат підрахунку тегів. Serializable зі стандартною серіалізацією -
 зберігається у файл через ObjectOutputStream (вимоги 2 і 3).
 */
public class TagStatistics implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String source;
    private final LocalDateTime createdAt;
    private final TreeMap<String, Integer> counts;   // TreeMap - одразу в лексикографічному порядку

    public TagStatistics(String source, Map<String, Integer> counts, LocalDateTime createdAt) {
        this.source = Objects.requireNonNull(source);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.counts = new TreeMap<>(counts);
    }

    // а) теги в лексикографічному порядку
    public List<Map.Entry<String, Integer>> byName() {
        return new ArrayList<>(counts.entrySet());
    }

    // б) за зростанням частоти; однакова частота - за назвою, щоб порядок був стабільним
    public List<Map.Entry<String, Integer>> byFrequency() {
        List<Map.Entry<String, Integer>> list = new ArrayList<>(counts.entrySet());
        list.sort(Map.Entry.<String, Integer>comparingByValue().thenComparing(Map.Entry.comparingByKey()));
        return list;
    }

    public int count(String tag) {
        return counts.getOrDefault(tag.toLowerCase(Locale.ROOT), 0);
    }

    public int totalTags() {
        int sum = 0;
        for (int n : counts.values()) sum += n;
        return sum;
    }

    public int distinctTags() {
        return counts.size();
    }

    public String getSource() {
        return source;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public static String format(List<Map.Entry<String, Integer>> rows) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Integer> e : rows) {
            sb.append(String.format("  %-12s %5d%n", e.getKey(), e.getValue()));
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TagStatistics t)) return false;
        return source.equals(t.source) && createdAt.equals(t.createdAt) && counts.equals(t.counts);
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, createdAt, counts);
    }

    @Override
    public String toString() {
        return String.format("%s: %d тегів, %d різних (%s)", source, totalTags(), distinctTags(),
                createdAt.format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")));
    }
}
