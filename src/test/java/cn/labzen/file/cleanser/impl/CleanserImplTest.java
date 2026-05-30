package cn.labzen.file.cleanser.impl;

import cn.labzen.file.cleanser.ChainableCleanserExecutor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 清理器实现类的单元测试
 */
class CleanserImplTest {

  // ── TrimCleanser ──

  @Test
  void trimCleanser_basic() {
    TrimCleanser cleanser = new TrimCleanser();
    assertEquals("hello", cleanser.cleanse("  hello  "));
  }

  @Test
  void trimCleanser_fullWidthSpace() {
    TrimCleanser cleanser = new TrimCleanser();
    assertEquals("hello", cleanser.cleanse("\u3000hello\u3000"));
  }

  // ── StripInvisibleCleanser ──

  @Test
  void stripInvisibleCleanser_zeroWidthSpace() {
    StripInvisibleCleanser cleanser = new StripInvisibleCleanser();
    assertEquals("hello", cleanser.cleanse("hel\u200Blo"));
  }

  @Test
  void stripInvisibleCleanser_bom() {
    StripInvisibleCleanser cleanser = new StripInvisibleCleanser();
    assertEquals("hello", cleanser.cleanse("\uFEFFhello"));
  }

  @Test
  void stripInvisibleCleanser_noInvisible() {
    StripInvisibleCleanser cleanser = new StripInvisibleCleanser();
    assertEquals("hello world", cleanser.cleanse("hello world"));
  }

  // ── NormalizeWhitespaceCleanser ──

  @Test
  void normalizeWhitespaceCleanser_multipleSpaces() {
    NormalizeWhitespaceCleanser cleanser = new NormalizeWhitespaceCleanser();
    assertEquals("hello world", cleanser.cleanse("hello   world"));
  }

  @Test
  void normalizeWhitespaceCleanser_fullWidthSpace() {
    NormalizeWhitespaceCleanser cleanser = new NormalizeWhitespaceCleanser();
    assertEquals("hello world", cleanser.cleanse("hello\u3000world"));
  }

  // ── NormalizeLineEndingCleanser ──

  @Test
  void normalizeLineEndingCleanser_crlf() {
    NormalizeLineEndingCleanser cleanser = new NormalizeLineEndingCleanser();
    assertEquals("hello\nworld", cleanser.cleanse("hello\r\nworld"));
  }

  @Test
  void normalizeLineEndingCleanser_cr() {
    NormalizeLineEndingCleanser cleanser = new NormalizeLineEndingCleanser();
    assertEquals("hello\nworld", cleanser.cleanse("hello\rworld"));
  }

  // ── StripHtmlCleanser ──

  @Test
  void stripHtmlCleanser_basic() {
    StripHtmlCleanser cleanser = new StripHtmlCleanser();
    assertEquals("hello world", cleanser.cleanse("<b>hello</b> <i>world</i>"));
  }

  @Test
  void stripHtmlCleanser_noHtml() {
    StripHtmlCleanser cleanser = new StripHtmlCleanser();
    assertEquals("hello world", cleanser.cleanse("hello world"));
  }

  // ── NormalizeQuotesCleanser ──

  @Test
  void normalizeQuotesCleanser_curlyQuotes() {
    NormalizeQuotesCleanser cleanser = new NormalizeQuotesCleanser();
    assertEquals("\"hello\"", cleanser.cleanse("\u201Chello\u201D"));
  }

  @Test
  void normalizeQuotesCleanser_singleCurlyQuotes() {
    NormalizeQuotesCleanser cleanser = new NormalizeQuotesCleanser();
    assertEquals("'hello'", cleanser.cleanse("\u2018hello\u2019"));
  }

  // ── CollapseDigitsCleanser ──

  @Test
  void collapseDigitsCleanser_fullWidthDigits() {
    CollapseDigitsCleanser cleanser = new CollapseDigitsCleanser();
    assertEquals("123", cleanser.cleanse("\uFF11\uFF12\uFF13"));
  }

  @Test
  void collapseDigitsCleanser_mixed() {
    CollapseDigitsCleanser cleanser = new CollapseDigitsCleanser();
    assertEquals("a1b2", cleanser.cleanse("a\uFF11b\uFF12"));
  }

  // ── CollapseLettersCleanser ──

  @Test
  void collapseLettersCleanser_fullWidthUpper() {
    CollapseLettersCleanser cleanser = new CollapseLettersCleanser();
    assertEquals("ABC", cleanser.cleanse("\uFF21\uFF22\uFF23"));
  }

  @Test
  void collapseLettersCleanser_fullWidthLower() {
    CollapseLettersCleanser cleanser = new CollapseLettersCleanser();
    assertEquals("abc", cleanser.cleanse("\uFF41\uFF42\uFF43"));
  }

  // ── ChainableCleanserExecutor ──

  @Test
  void chainableCleanserExecutor_multipleCleansers() {
    ChainableCleanserExecutor executor = new ChainableCleanserExecutor(
      java.util.List.of("trim", "strip-invisible"));
    String result = executor.execute("  hel\u200Blo  ");
    assertEquals("hello", result);
  }

  @Test
  void chainableCleanserExecutor_nullInput() {
    ChainableCleanserExecutor executor = new ChainableCleanserExecutor(
      java.util.List.of("trim"));
    assertNull(executor.execute(null));
  }
}
