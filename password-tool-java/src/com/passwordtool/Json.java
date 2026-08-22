package com.passwordtool;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 极简 JSON 工具（生成 + 解析），零依赖。
 * 仅支持本项目 API 所需的类型：对象 / 数组 / 字符串 / 数字 / 布尔 / null。
 */
public final class Json {

    // ========================== 生成 ==========================

    /** 把对象序列化为 JSON 字符串（支持 Map/List/String/Number/Boolean/null） */
    public static String stringify(Object value) {
        StringBuilder sb = new StringBuilder();
        writeValue(sb, value);
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private static void writeValue(StringBuilder sb, Object v) {
        if (v == null) {
            sb.append("null");
        } else if (v instanceof String) {
            writeString(sb, (String) v);
        } else if (v instanceof Boolean) {
            sb.append(v);
        } else if (v instanceof Double || v instanceof Float) {
            double d = ((Number) v).doubleValue();
            if (d == Math.floor(d) && !Double.isInfinite(d) && Math.abs(d) < 1e15) {
                sb.append((long) d);
            } else {
                sb.append(d);
            }
        } else if (v instanceof Number) {
            sb.append(v);
        } else if (v instanceof Map) {
            sb.append('{');
            boolean first = true;
            for (Map.Entry<?, ?> e : ((Map<?, ?>) v).entrySet()) {
                if (!first) sb.append(',');
                first = false;
                writeString(sb, String.valueOf(e.getKey()));
                sb.append(':');
                writeValue(sb, e.getValue());
            }
            sb.append('}');
        } else if (v instanceof Iterable) {
            sb.append('[');
            boolean first = true;
            for (Object item : (Iterable<?>) v) {
                if (!first) sb.append(',');
                first = false;
                writeValue(sb, item);
            }
            sb.append(']');
        } else {
            writeString(sb, v.toString());
        }
    }

    private static void writeString(StringBuilder sb, String s) {
        sb.append('"');
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"':  sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        sb.append('"');
    }

    // ========================== 解析 ==========================

    private final String src;
    private int pos;

    private Json(String src) { this.src = src; }

    /** 把 JSON 字符串解析为 Map / List / String / Double / Boolean / null */
    public static Object parse(String text) {
        Json p = new Json(text);
        p.skipWs();
        Object v = p.parseValue();
        p.skipWs();
        return v;
    }

    private Object parseValue() {
        skipWs();
        if (pos >= src.length()) throw new IllegalArgumentException("JSON 意外结束");
        char c = src.charAt(pos);
        switch (c) {
            case '{': return parseObject();
            case '[': return parseArray();
            case '"': return parseString();
            case 't': expect("true");  return Boolean.TRUE;
            case 'f': expect("false"); return Boolean.FALSE;
            case 'n': expect("null");  return null;
            default:  return parseNumber();
        }
    }

    private Map<String, Object> parseObject() {
        Map<String, Object> map = new LinkedHashMap<>();
        pos++; // {
        skipWs();
        if (peek() == '}') { pos++; return map; }
        while (true) {
            skipWs();
            String key = parseString();
            skipWs();
            expectChar(':');
            map.put(key, parseValue());
            skipWs();
            char c = peek();
            if (c == ',') { pos++; continue; }
            if (c == '}') { pos++; return map; }
            throw new IllegalArgumentException("JSON 对象语法错误 @ " + pos);
        }
    }

    private List<Object> parseArray() {
        List<Object> list = new ArrayList<>();
        pos++; // [
        skipWs();
        if (peek() == ']') { pos++; return list; }
        while (true) {
            list.add(parseValue());
            skipWs();
            char c = peek();
            if (c == ',') { pos++; continue; }
            if (c == ']') { pos++; return list; }
            throw new IllegalArgumentException("JSON 数组语法错误 @ " + pos);
        }
    }

    private String parseString() {
        if (peek() != '"') throw new IllegalArgumentException("期望字符串 @ " + pos);
        pos++;
        StringBuilder sb = new StringBuilder();
        while (pos < src.length()) {
            char c = src.charAt(pos++);
            if (c == '"') return sb.toString();
            if (c == '\\') {
                if (pos >= src.length()) break;
                char e = src.charAt(pos++);
                switch (e) {
                    case '"':  sb.append('"'); break;
                    case '\\': sb.append('\\'); break;
                    case '/':  sb.append('/'); break;
                    case 'n':  sb.append('\n'); break;
                    case 'r':  sb.append('\r'); break;
                    case 't':  sb.append('\t'); break;
                    case 'b':  sb.append('\b'); break;
                    case 'f':  sb.append('\f'); break;
                    case 'u':
                        if (pos + 4 <= src.length()) {
                            sb.append((char) Integer.parseInt(src.substring(pos, pos + 4), 16));
                            pos += 4;
                        }
                        break;
                    default: sb.append(e);
                }
            } else {
                sb.append(c);
            }
        }
        throw new IllegalArgumentException("JSON 字符串未闭合");
    }

    private Double parseNumber() {
        int start = pos;
        while (pos < src.length()) {
            char c = src.charAt(pos);
            if ((c >= '0' && c <= '9') || c == '-' || c == '+' || c == '.' || c == 'e' || c == 'E') {
                pos++;
            } else {
                break;
            }
        }
        return Double.parseDouble(src.substring(start, pos));
    }

    private void expect(String word) {
        if (!src.startsWith(word, pos)) throw new IllegalArgumentException("JSON 关键字错误 @ " + pos);
        pos += word.length();
    }

    private void expectChar(char c) {
        if (peek() != c) throw new IllegalArgumentException("期望 '" + c + "' @ " + pos);
        pos++;
    }

    private char peek() {
        if (pos >= src.length()) return '\0';
        return src.charAt(pos);
    }

    private void skipWs() {
        while (pos < src.length()) {
            char c = src.charAt(pos);
            if (c == ' ' || c == '\t' || c == '\n' || c == '\r') pos++;
            else break;
        }
    }

    // ========================== 便捷访问 ==========================

    /** 从 Map 中取字符串（缺省返回 defaultValue） */
    public static String getString(Map<String, Object> obj, String key, String defaultValue) {
        Object v = obj.get(key);
        return v == null ? defaultValue : String.valueOf(v);
    }

    /** 从 Map 中取整数（缺省返回 defaultValue） */
    public static int getInt(Map<String, Object> obj, String key, int defaultValue) {
        Object v = obj.get(key);
        if (v == null) return defaultValue;
        if (v instanceof Number) return ((Number) v).intValue();
        try {
            return (int) Math.round(Double.parseDouble(String.valueOf(v)));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /** 从 Map 中取布尔（缺省返回 defaultValue） */
    public static boolean getBool(Map<String, Object> obj, String key, boolean defaultValue) {
        Object v = obj.get(key);
        if (v == null) return defaultValue;
        if (v instanceof Boolean) return (Boolean) v;
        return Boolean.parseBoolean(String.valueOf(v));
    }
}
