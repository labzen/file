//package cn.labzen.file.converter.impl;
//
//import cn.labzen.file.annotation.DataConverter;
//import cn.labzen.file.converter.Converter;
//import cn.labzen.file.converter.importable.ImportableConverter;
//import cn.labzen.file.converter.util.DatePatternCache;
//import cn.labzen.file.exception.DataConvertException;
//
//import java.time.LocalDate;
//import java.time.format.DateTimeFormatter;
//import java.util.List;
//
///**
// * 日期转换器（仅导入）
// * <p>
// * 将字符串转换为 LocalDate
// *
// * @author labzen
// */
//@DataConverter(name = Converter.LOCAL_DATE_NAME, priority = Converter.LOCAL_DATE_PRIORITY)
//public class LocalDateConverter extends BasedDateConverter implements ImportableConverter<LocalDate> {
//
//  // ── 导入 ──
//
//  @Override
//  public boolean supportsImport(Class<?> targetType) {
//    return LocalDate.class.isAssignableFrom(targetType);
//  }
//
//  @Override
//  public LocalDate doConvertForImport(Object input, List<Object> arguments) throws DataConvertException {
//    if (input == null) {
//      return null;
//    }
//
//    String value = input.toString().trim();
//    String pattern = resolvePattern(arguments);
//
//    try {
//      DateTimeFormatter formatter = DatePatternCache.getDateTimeFormatter(pattern);
//      return LocalDate.parse(value, formatter);
//    } catch (Exception e) {
//      throw new DataConvertException("日期转换失败：[{}]", value);
//    }
//  }
//}
