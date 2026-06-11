package com.misyakuji.ai.tools;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BusinessToolsTest {

    private BusinessTools businessTools;

    @BeforeEach
    void setUp() {
        businessTools = new BusinessTools();
    }

    @Test
    @DisplayName("天气查询 - 返回包含城市名和气温的结果")
    void getWeather_shouldReturnWeatherInfo() {
        String result = businessTools.getWeather("北京");

        assertNotNull(result);
        assertTrue(result.contains("北京"));
        assertTrue(result.contains("°C"));
    }

    @Test
    @DisplayName("天气查询 - 不同城市返回不同结果")
    void getWeather_differentCities() {
        String beijing = businessTools.getWeather("北京");
        String shanghai = businessTools.getWeather("上海");

        assertNotNull(beijing);
        assertNotNull(shanghai);
        assertTrue(beijing.contains("北京"));
        assertTrue(shanghai.contains("上海"));
    }

    @Test
    @DisplayName("获取当前时间 - 返回非空时间字符串")
    void getCurrentDateTime_shouldReturnDateTime() {
        String result = businessTools.getCurrentDateTime();

        assertNotNull(result);
        assertFalse(result.isBlank());
        assertTrue(result.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}"),
                "时间格式应为 yyyy-MM-dd HH:mm:ss, 实际: " + result);
    }

    @Test
    @DisplayName("数学计算 - 加法")
    void calculate_addition() {
        String result = businessTools.calculate("2+3");
        assertNotNull(result);
        assertTrue(result.contains("5.0"));
        assertTrue(result.contains("="));
    }

    @Test
    @DisplayName("数学计算 - 乘法")
    void calculate_multiplication() {
        String result = businessTools.calculate("2*3");
        assertNotNull(result);
        assertTrue(result.contains("6.0"));
        assertTrue(result.contains("="));
    }

    @Test
    @DisplayName("数学计算 - 复合运算")
    void calculate_complexExpression() {
        String result = businessTools.calculate("(2+3)*4");
        assertNotNull(result);
        assertTrue(result.contains("20.0"));
    }

    @Test
    @DisplayName("数学计算 - 带括号的表达式")
    void calculate_withParentheses() {
        String result = businessTools.calculate("(10-2)*3");
        assertNotNull(result);
        assertTrue(result.contains("24.0"));
    }

    @Test
    @DisplayName("数学计算 - 无效表达式返回错误信息")
    void calculate_invalidExpression() {
        String result = businessTools.calculate("abc");

        assertNotNull(result);
        assertTrue(result.contains("无效的数学表达式"));
    }

    @Test
    @DisplayName("数学计算 - 除法运算")
    void calculate_division() {
        String result = businessTools.calculate("10/2");
        assertNotNull(result);
        assertTrue(result.contains("5.0"));
        assertTrue(result.contains("="));
    }
}
