package com.f3cinema.app.service.payment;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.UUID;

/**
 * MoMo test payment service (create QR/link + query status).
 */
public final class MomoPaymentService {

    private static final String PRIMARY = "momo.properties";
    private static final String FALLBACK = "momo.properties.example";

    private final String partnerCode;
    private final String accessKey;
    private final String secretKey;
    private final String createUrl;
    private final String queryUrl;
    private final String redirectUrl;
    private final String ipnUrl;
    private final String requestType;
    private final String lang;
    private final int autoCheckTimeoutSeconds;
    private final int autoCheckPollSeconds;

    private final HttpClient http = HttpClient.newHttpClient();

    private MomoPaymentService(String partnerCode, String accessKey, String secretKey,
                               String createUrl, String queryUrl,
                               String redirectUrl, String ipnUrl,
                               String requestType, String lang,
                               int autoCheckTimeoutSeconds, int autoCheckPollSeconds) {
        this.partnerCode = partnerCode;
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.createUrl = createUrl;
        this.queryUrl = queryUrl;
        this.redirectUrl = redirectUrl;
        this.ipnUrl = ipnUrl;
        this.requestType = requestType;
        this.lang = lang;
        this.autoCheckTimeoutSeconds = autoCheckTimeoutSeconds;
        this.autoCheckPollSeconds = autoCheckPollSeconds;
    }

    public static MomoPaymentService loadFromClasspath() {
        Properties p = new Properties();
        try (InputStream in = openStream()) {
            if (in == null) {
                throw new IllegalStateException("Thieu momo.properties/momo.properties.example");
            }
            p.load(in);
        } catch (IOException e) {
            throw new IllegalStateException("Khong doc duoc cau hinh MoMo", e);
        }

        return new MomoPaymentService(
                p.getProperty("momo_partnerCode", "").trim(),
                p.getProperty("momo_accessKey", "").trim(),
                p.getProperty("momo_secretKey", "").trim(),
                p.getProperty("momo_createUrl", "https://test-payment.momo.vn/v2/gateway/api/create").trim(),
                p.getProperty("momo_queryUrl", "https://test-payment.momo.vn/v2/gateway/api/query").trim(),
                p.getProperty("momo_redirectUrl", "https://example.com/momo-return").trim(),
                p.getProperty("momo_ipnUrl", "https://example.com/momo-ipn").trim(),
                p.getProperty("momo_requestType", "captureWallet").trim(),
                p.getProperty("momo_lang", "vi").trim(),
                Integer.parseInt(p.getProperty("momo_autoCheckTimeoutSeconds", "180").trim()),
                Integer.parseInt(p.getProperty("momo_autoCheckPollSeconds", "3").trim())
        );
    }

    private static InputStream openStream() {
        ClassLoader cl = MomoPaymentService.class.getClassLoader();
        InputStream in = cl.getResourceAsStream(PRIMARY);
        return in != null ? in : cl.getResourceAsStream(FALLBACK);
    }

    public boolean hasCredentials() {
        return !partnerCode.isBlank() && !accessKey.isBlank() && !secretKey.isBlank();
    }

    public int autoCheckTimeoutSeconds() {
        return autoCheckTimeoutSeconds;
    }

    public int autoCheckPollSeconds() {
        return autoCheckPollSeconds;
    }

    public MomoInitResponse createTestQr(BigDecimal amount, String orderInfo, String orderId) {
        if (!hasCredentials()) {
            throw new IllegalStateException("Thiếu MoMo credentials. Điền momo.properties trước.");
        }

        String requestId = UUID.randomUUID().toString();
        String extraData = "";
        String amountText = String.valueOf(amount.longValue());
        String rawSignature = "accessKey=" + accessKey
                + "&amount=" + amountText
                + "&extraData=" + extraData
                + "&ipnUrl=" + ipnUrl
                + "&orderId=" + orderId
                + "&orderInfo=" + orderInfo
                + "&partnerCode=" + partnerCode
                + "&redirectUrl=" + redirectUrl
                + "&requestId=" + requestId
                + "&requestType=" + requestType;
        String signature = hmacSha256(secretKey, rawSignature);

        String body = "{" +
                "\"partnerCode\":\"" + esc(partnerCode) + "\"," +
                "\"requestId\":\"" + esc(requestId) + "\"," +
                "\"amount\":\"" + esc(amountText) + "\"," +
                "\"orderId\":\"" + esc(orderId) + "\"," +
                "\"orderInfo\":\"" + esc(orderInfo) + "\"," +
                "\"redirectUrl\":\"" + esc(redirectUrl) + "\"," +
                "\"ipnUrl\":\"" + esc(ipnUrl) + "\"," +
                "\"lang\":\"" + esc(lang) + "\"," +
                "\"extraData\":\"\"," +
                "\"requestType\":\"" + esc(requestType) + "\"," +
                "\"signature\":\"" + esc(signature) + "\"" +
                "}";

        HttpRequest req = HttpRequest.newBuilder(URI.create(createUrl))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        try {
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() / 100 != 2) {
                throw new IllegalStateException("MoMo create failed HTTP " + resp.statusCode());
            }
            String txt = resp.body();
            int resultCode = parseIntField(txt, "resultCode", -1);
            String message = parseStringField(txt, "message");
            if (resultCode != 0) {
                throw new IllegalStateException("MoMo create lỗi: " + resultCode + " - " + message);
            }
            String payUrl = parseStringField(txt, "payUrl");
            String deeplink = parseStringField(txt, "deeplink");
            String qrCodeUrl = parseStringField(txt, "qrCodeUrl");
            return new MomoInitResponse(orderId, requestId, payUrl, deeplink, qrCodeUrl, txt);
        } catch (Exception e) {
            throw new IllegalStateException("Không tạo được thanh toán MoMo: " + e.getMessage(), e);
        }
    }

    public MomoQueryResult queryStatus(String orderId, String requestId) {
        if (!hasCredentials()) {
            return new MomoQueryResult(false, null, -1, "missing credentials");
        }
        String reqId = requestId != null && !requestId.isBlank() ? requestId : UUID.randomUUID().toString();
        String rawSignature = "accessKey=" + accessKey
                + "&orderId=" + orderId
                + "&partnerCode=" + partnerCode
                + "&requestId=" + reqId;
        String signature = hmacSha256(secretKey, rawSignature);

        String body = "{" +
                "\"partnerCode\":\"" + esc(partnerCode) + "\"," +
                "\"requestId\":\"" + esc(reqId) + "\"," +
                "\"orderId\":\"" + esc(orderId) + "\"," +
                "\"lang\":\"" + esc(lang) + "\"," +
                "\"signature\":\"" + esc(signature) + "\"" +
                "}";

        HttpRequest req = HttpRequest.newBuilder(URI.create(queryUrl))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        try {
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() / 100 != 2) {
                return new MomoQueryResult(false, null, resp.statusCode(), "http error");
            }
            String txt = resp.body();
            int resultCode = parseIntField(txt, "resultCode", -1);
            String transId = parseStringField(txt, "transId");
            String message = parseStringField(txt, "message");
            boolean paid = resultCode == 0;
            return new MomoQueryResult(paid, transId, resultCode, message);
        } catch (Exception e) {
            return new MomoQueryResult(false, null, -1, e.getMessage());
        }
    }

    private static String parseStringField(String json, String field) {
        String key = "\"" + field + "\"";
        int i = json.indexOf(key);
        if (i < 0) return null;
        int colon = json.indexOf(':', i + key.length());
        if (colon < 0) return null;
        int q1 = json.indexOf('"', colon + 1);
        if (q1 < 0) return null;
        int q2 = json.indexOf('"', q1 + 1);
        if (q2 < 0) return null;
        return json.substring(q1 + 1, q2);
    }

    private static int parseIntField(String json, String field, int defaultValue) {
        String key = "\"" + field + "\"";
        int i = json.indexOf(key);
        if (i < 0) return defaultValue;
        int colon = json.indexOf(':', i + key.length());
        if (colon < 0) return defaultValue;
        int j = colon + 1;
        while (j < json.length() && Character.isWhitespace(json.charAt(j))) j++;
        int k = j;
        if (k < json.length() && json.charAt(k) == '-') k++;
        while (k < json.length() && Character.isDigit(json.charAt(k))) k++;
        if (k <= j) return defaultValue;
        try {
            return Integer.parseInt(json.substring(j, k));
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    private static String hmacSha256(String key, String data) {
        try {
            Mac hmac = Mac.getInstance("HmacSHA256");
            hmac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] bytes = hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("HMAC SHA256 fail", e);
        }
    }

    private static String esc(String s) {
        return (s == null ? "" : s).replace("\\", "\\\\").replace("\"", "\\\"");
    }

    public record MomoInitResponse(String orderId, String requestId, String payUrl, String deeplink,
                                   String qrCodeUrl, String raw) {
        public String bestQrUrl() {
            // MoMo test co the tra ve deeplink momo://... thay vi URL anh QR.
            // Neu khong phai http(s), ta render QR qua quickchart.
            String src = firstNonBlank(qrCodeUrl, deeplink, payUrl);
            if (src == null) return null;
            String lower = src.toLowerCase();
            if (lower.startsWith("http://") || lower.startsWith("https://")) {
                return src;
            }
            return "https://quickchart.io/qr?size=300&text="
                    + java.net.URLEncoder.encode(src, StandardCharsets.UTF_8);
        }

        private static String firstNonBlank(String... values) {
            for (String v : values) {
                if (v != null && !v.isBlank()) {
                    return v;
                }
            }
            return null;
        }
    }

    public record MomoQueryResult(boolean paid, String transactionId, int resultCode, String message) {}
}
