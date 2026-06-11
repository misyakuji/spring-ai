package com.misyakuji.ai.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class BusinessTools {

    @Tool(description = "根据城市名查询当前天气信息")
    public String getWeather(@ToolParam(description = "城市名，如：北京、上海") String city) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int temp = random.nextInt(-5, 35);
        String[] conditions = {"晴", "多云", "阴", "小雨", "大雨", "雪", "雾"};
        String condition = conditions[random.nextInt(conditions.length)];
        int humidity = random.nextInt(30, 95);
        int windLevel = random.nextInt(1, 7);

        return String.format("%s 当前天气：%s，气温 %d°C，湿度 %d%%，%d 级风",
                city, condition, temp, humidity, windLevel);
    }

    @Tool(description = "获取当前日期和时间")
    public String getCurrentDateTime() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    @Tool(description = "执行简单数学计算")
    public String calculate(@ToolParam(description = "数学表达式，如 2+3*4") String expression) {
        try {
            String sanitized = expression.replaceAll("[^0-9+\\-*/().\\s]", "");
            if (sanitized.isEmpty()) {
                return "无效的数学表达式: " + expression;
            }
            double result = new ExpressionParser(sanitized).parse();
            return expression + " = " + result;
        } catch (Exception e) {
            return "计算错误: " + e.getMessage();
        }
    }

    private static class ExpressionParser {
        private final String expr;
        private int pos = -1;
        private int ch;

        ExpressionParser(String expr) {
            this.expr = expr;
        }

        double parse() {
            nextChar();
            double x = parseExpression();
            if (pos < expr.length()) throw new RuntimeException("意外字符: " + (char) ch);
            return x;
        }

        private void nextChar() {
            ch = (++pos < expr.length()) ? expr.charAt(pos) : -1;
        }

        private boolean eat(int charToEat) {
            while (ch == ' ') nextChar();
            if (ch == charToEat) { nextChar(); return true; }
            return false;
        }

        private double parseExpression() {
            double x = parseTerm();
            for (;;) {
                if (eat('+')) x += parseTerm();
                else if (eat('-')) x -= parseTerm();
                else return x;
            }
        }

        private double parseTerm() {
            double x = parseFactor();
            for (;;) {
                if (eat('*')) x *= parseFactor();
                else if (eat('/')) x /= parseFactor();
                else return x;
            }
        }

        private double parseFactor() {
            if (eat('+')) return parseFactor();
            if (eat('-')) return -parseFactor();

            double x;
            int startPos = this.pos;
            if (eat('(')) {
                x = parseExpression();
                eat(')');
            } else if ((ch >= '0' && ch <= '9') || ch == '.') {
                while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                x = Double.parseDouble(expr.substring(startPos, this.pos));
            } else {
                throw new RuntimeException("意外字符: " + (char) ch);
            }

            if (eat('^')) x = Math.pow(x, parseFactor());
            return x;
        }
    }
}
