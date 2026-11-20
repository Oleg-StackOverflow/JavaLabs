package io.tags;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 Завдання 4: частота тегів на сторінці.
 Рахуються ВІДКРИВАЮЧІ (і самозакриті) теги: <div ...>, <br/>, <img ...>.
 Не рахуються: закриваючі </div>, коментарі <!-- -->, <!DOCTYPE>, а також "теги"
 всередині <script> і <style> (там код, а не розмітка: "if (a<b)" чи "'<div>'" у JS).
 */
public final class TagCounter {

    private static final Pattern COMMENT = Pattern.compile("<!--.*?-->", Pattern.DOTALL);
    private static final Pattern RAW_TEXT =
            Pattern.compile("(<(script|style)\\b[^>]*>).*?</\\2\\s*>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
    private static final Pattern TAG = Pattern.compile("<([a-zA-Z][a-zA-Z0-9:-]*)");

    private TagCounter() {
    }

    public static TagStatistics count(String html, String source) {
        String text = COMMENT.matcher(html).replaceAll(" ");
        text = RAW_TEXT.matcher(text).replaceAll("$1");   // лишаємо сам <script ...>, вміст викидаємо

        Map<String, Integer> counts = new HashMap<>();
        Matcher m = TAG.matcher(text);
        while (m.find()) {
            String name = m.group(1).toLowerCase(Locale.ROOT); // <DIV> і <div> - один тег
            counts.merge(name, 1, Integer::sum);
        }
        return new TagStatistics(source, counts, LocalDateTime.now());
    }

    // Завантаження сторінки. Кодування береться з заголовка Content-Type (за замовчуванням UTF-8)
    public static String download(String url) throws IOException, InterruptedException {
        URI uri = URI.create(url);
        if (uri.getScheme() == null || !(uri.getScheme().equals("http") || uri.getScheme().equals("https"))) {
            throw new IllegalArgumentException("Потрібна адреса http:// або https://");
        }
        HttpClient client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        HttpRequest request = HttpRequest.newBuilder(uri)
                .timeout(Duration.ofSeconds(20))
                .header("User-Agent", "Mozilla/5.0 (lab task 5)")
                .GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() / 100 != 2) {
            throw new IOException("Сервер відповів кодом " + response.statusCode());
        }
        return response.body();
    }
}
