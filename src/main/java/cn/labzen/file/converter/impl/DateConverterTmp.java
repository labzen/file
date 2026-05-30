//package cn.labzen.file.converter.impl;
//
//import cn.labzen.file.annotation.DataConverter;
//import cn.labzen.file.converter.Converter;
//import cn.labzen.file.converter.importable.ImportableConverter;
//import cn.labzen.file.exception.DataConvertException;
//
//import java.text.ParseException;
//import java.text.SimpleDateFormat;
//import java.util.Date;
//import java.util.List;
//
///**
// * 日期转换器（仅导入）
// * <p>
// * 将字符串转换为 Date
// *
// * @author labzen
// */
//@DataConverter(name = Converter.DATE_NAME, priority = Converter.DATE_PRIORITY)
//public class DateConverter extends BasedDateConverter implements ImportableConverter<Date> {
//
//  // ── 导入 ──
//
//  @Override
//  public boolean supportsImport(Class<?> targetType) {
//    return Date.class.isAssignableFrom(targetType);
//  }
//
//  @Override
//  public Date doConvertForImport(Object input, List<Object> arguments) throws DataConvertException {
//    if (input == null) {
//      return null;
//    }
//
//    String value = input.toString().trim();
//    String pattern = resolvePattern(arguments);
//
//    try {
//      SimpleDateFormat sdf = new SimpleDateFormat(pattern);
//      return sdf.parse(value);
//    } catch (ParseException e) {
//      throw new DataConvertException("日期格式解析失败：[{}]，期望格式：[{}]", value, pattern);
//    } catch (Exception e) {
//      throw new DataConvertException("日期转换失败：[{}]", value);
//    }
//  }
//}
