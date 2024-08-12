package com.movtery.pojavzh.utils.stringutils;

import static android.content.Context.CLIPBOARD_SERVICE;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.widget.Toast;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {

    public static String insertSpace(Object prefixString, Object... suffixString) {
        return insertSpace(prefixString == null ? null : prefixString.toString(),
                Arrays.stream(suffixString).map(Object::toString).toArray(String[]::new));
    }

    /**
     * 在字符串之间插入空格
     *
     * @param prefixString 第一个字符串
     * @param suffixString 之后的多个字符串
     * @return 返回插入好空格的字符串 "string1 string2 string3"
     */
    public static String insertSpace(String prefixString, String... suffixString) {
        return insertString(" ", prefixString, suffixString);
    }

    public static String insertNewline(Object prefixString, Object... suffixString) {
        return insertNewline(prefixString == null ? null : prefixString.toString(),
                Arrays.stream(suffixString).map(Object::toString).toArray(String[]::new));
    }

    /**
     * 在字符串之间插入换行符
     *
     * @param prefixString 第一个字符串
     * @param suffixString 之后的多个字符串
     * @return 返回插入好换行符的字符串
     */
    public static String insertNewline(String prefixString, String... suffixString) {
        return insertString("\r\n", prefixString, suffixString);
    }

    public static String insertString(String stringToInsert, String prefixString, String... suffixString) {
        StringJoiner stringJoiner = new StringJoiner(stringToInsert);
        if (prefixString != null) {
            stringJoiner.add(prefixString);
        }
        for (String string : suffixString) {
            stringJoiner.add(string);
        }

        return stringJoiner.toString();
    }

    public static String shiftString(String input, ShiftDirection direction, int shiftCount) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        //确保位移个数在字符串长度范围内
        int length = input.length();
        shiftCount = shiftCount % length;
        if (shiftCount == 0) {
            return input;
        }

        switch (direction) {
            case LEFT:
                return input.substring(shiftCount) + input.substring(0, shiftCount);
            case RIGHT:
                return input.substring(length - shiftCount) + input.substring(0, length - shiftCount);
            default:
                throw new IllegalArgumentException("Invalid shift direction: " + direction);
        }
    }

    /**
     * 在一段字符串中提取数字
     */
    public static int[] extractNumbers(String str, int quantity) {
        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(str);

        int[] numbers = new int[quantity];

        int count = 0;
        while (matcher.find() && count < quantity) {
            numbers[count] = Integer.parseInt(matcher.group());
            count++;
        }

        return numbers;
    }

    public static List<Integer> extractNumbers(String str) {
        List<Integer> numbers = new ArrayList<>();
        Matcher matcher = Pattern.compile("\\d+").matcher(str);
        while (matcher.find()) {
            numbers.add(Integer.parseInt(matcher.group()));
        }
        return numbers;
    }

    public static String formattingTime(String time) {
        int T = time.indexOf('T');
        int Z = time.indexOf('Z');
        if (T == -1 || Z == -1) return time;
        return StringUtils.insertSpace(time.substring(0, T), time.substring(T + 1, Z));
    }

    public static String markdownToHtml(String markdown) {
        Parser parser = Parser.builder().build();
        Node document = parser.parse(markdown);
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        return renderer.render(document);
    }

    public static void copyText(String label, String text, Context context) {
        ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE);
        clipboardManager.setPrimaryClip(ClipData.newPlainText(label, text));
        Tools.runOnUiThread(() -> Toast.makeText(context, context.getString(R.string.zh_copied), Toast.LENGTH_SHORT).show());
    }
}
