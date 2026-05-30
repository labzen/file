//package cn.labzen.file.converter.impl;
//
//import cn.labzen.file.annotation.DataConverter;
//import cn.labzen.file.converter.Converter;
//import cn.labzen.file.converter.importable.ImportableConverter;
//import cn.labzen.file.converter.util.DatePatternCache;
//import cn.labzen.file.exception.DataConvertException;
//
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//import java.util.List;
//
///**
// * 日期转换器（仅导入）
// * <p>
// * 将字符串转换为 LocalDateTime
// *
// * @author labzen
// */
//@DataConverter(name = Converter.LOCAL_DATETIME_NAME, priority = Converter.LOCAL_DATETIME_PRIORITY)
//public class LocalDateTimeConverter extends BasedDateConverter implements ImportableConverter<LocalDateTime> {
//
//  // ── 导入 ──
//
//  @Override
//  public boolean supportsImport(Class<?> targetType) {
//    return LocalDateTime.class.isAssignableFrom(targetType);
//  }
//
//  @Override
//  public LocalDateTime doConvertForImport(Object input, List<Object> arguments) throws DataConvertException {
//    if (input == null) {
//      return null;
//    }
//
//    String value = input.toString().trim();
//    String pattern = resolvePattern(arguments);
//
//    try {
//      DateTimeFormatter formatter = DatePatternCache.getDateTimeFormatter(pattern);
//      return LocalDateTime.parse(value, formatter);
//    } catch (Exception e) {
//      throw new DataConvertException("日期转换失败：[{}]", value);
//    }
//  }
//}
