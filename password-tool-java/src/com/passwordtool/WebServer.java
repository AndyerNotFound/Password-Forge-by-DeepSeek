package com.passwordtool;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * 密码工坊 · 内置 Web 服务器（纯 JDK，零依赖）
 * <p>
 * 接口与 Python Flask 版完全兼容，可直接复用原有前端页面。
 * 静态资源目录: ./web/ （index.html + static/）
 * <p>
 * 用法: java -cp out com.passwordtool.WebServer [端口]
 */
public final class WebServer {

    private static final String VERSION = "4.3";

    private WebServer() {}

    public static void main(String[] args) throws IOException {
        int port = 8080;
        if (args.length > 0 && !args[0].equals("web")) {
            try { port = Integer.parseInt(args[0]); } catch (NumberFormatException ignored) {}
        }
        String envPort = System.getenv("PORT");
        if (envPort != null && !envPort.isEmpty()) {
            try { port = Integer.parseInt(envPort); } catch (NumberFormatException ignored) {}
        }

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", new RootHandler());
        server.createContext("/api/", new ApiHandler());
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();

        System.out.println();
        System.out.println("==================================================");
        System.out.println("  🔐 密码工坊 · Java 版 v" + VERSION + " Web 服务已启动");
        System.out.println("  手机浏览器打开:  http://127.0.0.1:" + port);
        System.out.println("  (局域网设备可用: http://<本机IP>:" + port + ")");
        System.out.println("  按 Ctrl+C 停止服务");
        System.out.println("==================================================");
        System.out.println();
    }

    // ========================== 静态资源 ==========================

    private static final class RootHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            String path = ex.getRequestURI().getPath();
            if (path.equals("/")) path = "/index.html";

            File base = findWebDir();
            if (base == null) {
                sendText(ex, 500, "{\"error\":\"未找到 web 目录（请确认项目目录下有 web/index.html）\"}");
                return;
            }
            File f = new File(base, path);
            // 防止路径穿越
            if (!f.getCanonicalPath().startsWith(base.getCanonicalPath()) || !f.isFile()) {
                sendText(ex, 404, "{\"error\":\"页面不存在 (404)\"}");
                return;
            }
            byte[] data;
            try (InputStream in = new FileInputStream(f)) {
                data = readAll(in);
            }
            ex.getResponseHeaders().set("Content-Type", contentType(f.getName()));
            ex.sendResponseHeaders(200, data.length);
            try (OutputStream os = ex.getResponseBody()) {
                os.write(data);
            }
        }
    }

    // ========================== API ==========================

    @SuppressWarnings("unchecked")
    private static final class ApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            String method = ex.getRequestMethod();
            String path = ex.getRequestURI().getPath();
            try {
                // 路径不存在 → 404；路径存在但方法不允许 → 405（与 Flask 版行为一致）
                if (!isKnownApi(path)) {
                    sendText(ex, 404, "{\"error\":\"接口不存在 (404)，请确认正在运行最新版服务\"}");
                    return;
                }
                if (!method.equals("POST") && !path.equals("/api/health")) {
                    sendText(ex, 405, "{\"error\":\"请求方法不允许 (405)\"}");
                    return;
                }
                if (path.equals("/api/health")) {
                    Map<String, Object> resp = new LinkedHashMap<>();
                    resp.put("status", "ok");
                    resp.put("version", VERSION);
                    sendJson(ex, 200, resp);
                    return;
                }
                Map<String, Object> body = parseBody(ex);

                if (path.equals("/api/generate-password")) {
                    int length = Math.max(8, Math.min(128, Json.getInt(body, "length", 16)));
                    String pw = PasswordLogic.generateRandomPassword(
                        length,
                        Json.getBool(body, "use_upper", true),
                        Json.getBool(body, "use_lower", true),
                        Json.getBool(body, "use_digits", true),
                        Json.getBool(body, "use_special", true),
                        Json.getBool(body, "avoid_ambiguous", false));
                    double entropy = PasswordLogic.calculateEntropy(pw);
                    PasswordLogic.Strength st = PasswordLogic.strengthLabel(entropy);
                    Map<String, Object> resp = new LinkedHashMap<>();
                    resp.put("password", pw);
                    resp.put("length", length);
                    resp.put("entropy", entropy);
                    resp.put("strength_label", st.label);
                    resp.put("strength_color", st.color);
                    sendJson(ex, 200, resp);
                    return;
                }

                if (path.equals("/api/generate-passphrase")) {
                    int count = Math.max(4, Math.min(48, Json.getInt(body, "count", 6)));
                    String separator = Json.getString(body, "separator", "-");
                    boolean includeNumber = Json.getBool(body, "include_number", false);
                    boolean titleCase = Json.getBool(body, "title_case", false);
                    List<String> phrases = new java.util.ArrayList<>();
                    for (int i = 0; i < 5; i++) {
                        phrases.add(PasswordLogic.generatePassphrase(count, separator, titleCase, includeNumber));
                    }
                    double entropy = PasswordLogic.calculateEntropy(phrases.get(0));
                    PasswordLogic.Strength st = PasswordLogic.strengthLabel(entropy);
                    Map<String, Object> resp = new LinkedHashMap<>();
                    resp.put("passphrases", phrases);
                    resp.put("word_count", count);
                    resp.put("entropy", entropy);
                    resp.put("strength_label", st.label);
                    resp.put("strength_color", st.color);
                    sendJson(ex, 200, resp);
                    return;
                }

                if (path.equals("/api/analyze")) {
                    String password = Json.getString(body, "password", "");
                    if (password.isEmpty()) {
                        sendText(ex, 400, "{\"error\":\"密码不能为空\"}");
                        return;
                    }
                    PasswordLogic.Analysis a = PasswordLogic.analyze(password);
                    Map<String, Object> crackTimes = new LinkedHashMap<>();
                    for (PasswordLogic.CrackRow r : a.crackRows) {
                        crackTimes.put(r.key, r.seconds);
                    }
                    Map<String, Object> resp = new LinkedHashMap<>();
                    resp.put("password", password);
                    resp.put("length", a.length);
                    resp.put("entropy", a.entropy);
                    resp.put("strength_label", a.strength.label);
                    resp.put("strength_color", a.strength.color);
                    resp.put("common_password", a.commonPassword);
                    resp.put("crack_times", crackTimes);
                    sendJson(ex, 200, resp);
                    return;
                }

                if (path.equals("/api/hash")) {
                    String password = Json.getString(body, "password", "");
                    if (password.isEmpty()) {
                        sendText(ex, 400, "{\"error\":\"密码不能为空\"}");
                        return;
                    }
                    Map<String, Object> resp = new LinkedHashMap<>();
                    resp.put("password", password);
                    resp.put("hashes", PasswordLogic.calculateHashes(password));
                    sendJson(ex, 200, resp);
                    return;
                }

                sendText(ex, 404, "{\"error\":\"接口不存在 (404)，请确认正在运行最新版服务\"}");
            } catch (Exception e) {
                sendText(ex, 400, "{\"error\":\"请求解析失败: " + JsonString.escape(e.getMessage()) + "\"}");
            }
        }

        private static boolean isKnownApi(String path) {
            return path.equals("/api/health")
                || path.equals("/api/generate-password")
                || path.equals("/api/generate-passphrase")
                || path.equals("/api/analyze")
                || path.equals("/api/hash");
        }
    }

    // ========================== 工具 ==========================

    @SuppressWarnings("unchecked")
    private static Map<String, Object> parseBody(HttpExchange ex) throws IOException {
        String raw;
        try (InputStream in = ex.getRequestBody()) {
            raw = new String(readAll(in), StandardCharsets.UTF_8);
        }
        Object v = Json.parse(raw == null || raw.isEmpty() ? "{}" : raw);
        if (v instanceof Map) return (Map<String, Object>) v;
        return new LinkedHashMap<>();
    }

    private static void sendJson(HttpExchange ex, int code, Object body) throws IOException {
        String json = Json.stringify(body);
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        sendText(ex, code, json);
    }

    private static void sendText(HttpExchange ex, int code, String text) throws IOException {
        byte[] data = text.getBytes(StandardCharsets.UTF_8);
        if (ex.getResponseHeaders().getFirst("Content-Type") == null) {
            ex.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        }
        ex.sendResponseHeaders(code, data.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(data);
        }
    }

    private static byte[] readAll(InputStream in) throws IOException {
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int n;
        while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
        return out.toByteArray();
    }

    private static File findWebDir() {
        String dir = System.getenv("WEB_DIR");
        if (dir != null && !dir.isEmpty() && new File(dir).isDirectory()) return new File(dir);
        // 依次尝试: 工作目录 ./web、程序所在目录 ../web、当前目录下的 web
        File[] candidates = {
            new File("web"),
            new File("password-tool-java/web"),
        };
        for (File c : candidates) {
            if (c.isDirectory() && new File(c, "index.html").isFile()) return c;
        }
        try {
            File codeSource = new File(WebServer.class.getProtectionDomain()
                .getCodeSource().getLocation().toURI());
            File jarDir = codeSource.isDirectory() ? codeSource : codeSource.getParentFile();
            File near = new File(jarDir, "web");
            if (near.isDirectory() && new File(near, "index.html").isFile()) return near;
            File parent = new File(jarDir.getParentFile(), "web");
            if (parent.isDirectory() && new File(parent, "index.html").isFile()) return parent;
        } catch (Exception ignored) {
        }
        return null;
    }

    private static String contentType(String name) {
        String lower = name.toLowerCase();
        if (lower.endsWith(".html")) return "text/html; charset=utf-8";
        if (lower.endsWith(".css"))  return "text/css; charset=utf-8";
        if (lower.endsWith(".js"))   return "application/javascript; charset=utf-8";
        if (lower.endsWith(".svg"))  return "image/svg+xml";
        if (lower.endsWith(".png"))  return "image/png";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".ico"))  return "image/x-icon";
        if (lower.endsWith(".json")) return "application/json; charset=utf-8";
        if (lower.endsWith(".woff2")) return "font/woff2";
        return "application/octet-stream";
    }

    /** 供错误信息使用的字符串转义辅助 */
    private static final class JsonString {
        static String escape(String s) {
            if (s == null) return "";
            StringBuilder sb = new StringBuilder();
            for (char c : s.toCharArray()) {
                if (c == '"' || c == '\\') sb.append('\\');
                sb.append(c);
            }
            return sb.toString();
        }
    }
}
