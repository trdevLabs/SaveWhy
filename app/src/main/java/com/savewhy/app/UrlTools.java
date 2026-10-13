package com.savewhy.app;

import java.net.URI;
import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Shared text is untrusted. Keep meaningful query parameters, discard known tracking. */
public final class UrlTools {
    private static final Pattern LINK = Pattern.compile("https?://[^\\s<>\"\\p{Cc}]+", Pattern.CASE_INSENSITIVE);
    private UrlTools() {}

    public static String extract(String shared) {
        if (shared == null) return "";
        Matcher matcher = LINK.matcher(shared);
        while (matcher.find()) {
            String candidate = matcher.group().replaceAll("[.,;!]+$", "");
            while (candidate.endsWith(")") && count(candidate, ')') > count(candidate, '('))
                candidate = candidate.substring(0, candidate.length() - 1);
            try { return normalize(candidate); } catch (IllegalArgumentException ignored) {}
        }
        return "";
    }

    private static int count(String value, char ch) {
        int count = 0; for (int i = 0; i < value.length(); i++) if (value.charAt(i) == ch) count++;
        return count;
    }

    public static String normalize(String value) {
        if (value == null || value.length() > 4096) throw new IllegalArgumentException("Bağlantı çok uzun.");
        try {
            URI uri = new URI(value.trim());
            String scheme = uri.getScheme(), host = uri.getHost();
            if (scheme == null || !(scheme.equalsIgnoreCase("https") || scheme.equalsIgnoreCase("http"))
                    || host == null || host.isEmpty() || uri.getRawUserInfo() != null
                    || uri.getPort() > 65535) throw new IllegalArgumentException("Geçerli bir http/https bağlantısı gir.");
            scheme = scheme.toLowerCase(Locale.ROOT); host = host.toLowerCase(Locale.ROOT);
            int port = uri.getPort();
            if ((scheme.equals("https") && port == 443) || (scheme.equals("http") && port == 80)) port = -1;
            ArrayList<String> parameters = new ArrayList<>();
            boolean instagram = matches(host, "instagram.com");
            boolean youtube = matches(host, "youtube.com") || host.equals("youtu.be");
            if (uri.getRawQuery() != null) for (String part : uri.getRawQuery().split("&")) {
                String key = part.split("=", 2)[0].toLowerCase(Locale.ROOT);
                if (!key.startsWith("utm_") && !(instagram && (key.equals("igsh") || key.equals("igshid")))
                        && !(youtube && key.equals("si")) && !key.equals("fbclid")) parameters.add(part);
            }
            String path = uri.getRawPath();
            String normalized = scheme + "://" + host + (port < 0 ? "" : ":" + port)
                    + (path == null || path.isEmpty() ? "/" : path)
                    + (parameters.isEmpty() ? "" : "?" + String.join("&", parameters));
            // Preserve fragments: some sites use them as meaningful content routes.
            if (uri.getRawFragment() != null) normalized += "#" + uri.getRawFragment();
            return normalized;
        } catch (java.net.URISyntaxException e) {
            throw new IllegalArgumentException("Geçerli bir http/https bağlantısı gir.");
        }
    }

    public static String source(String url) {
        String host = URI.create(url).getHost().toLowerCase(Locale.ROOT);
        if (matches(host, "instagram.com")) return "Instagram";
        if (matches(host, "youtube.com") || matches(host, "youtu.be")) return "YouTube";
        if (matches(host, "tiktok.com")) return "TikTok";
        if (matches(host, "facebook.com") || matches(host, "fb.watch") || matches(host, "fb.com")) return "Facebook";
        if (matches(host, "x.com") || matches(host, "twitter.com")) return "X";
        if (matches(host, "pinterest.com") || matches(host, "pin.it")) return "Pinterest";
        if (matches(host, "reddit.com") || matches(host, "redd.it")) return "Reddit";
        if (matches(host, "whatsapp.com") || matches(host, "wa.me")) return "WhatsApp";
        if (matches(host, "telegram.org") || matches(host, "t.me")) return "Telegram";
        if (matches(host, "threads.net") || matches(host, "threads.com")) return "Threads";
        if (matches(host, "twitch.tv")) return "Twitch";
        if (matches(host, "linkedin.com") || matches(host, "lnkd.in")) return "LinkedIn";
        return host.startsWith("www.") ? host.substring(4) : host;
    }
    private static boolean matches(String host, String domain) { return host.equals(domain) || host.endsWith("." + domain); }
}
