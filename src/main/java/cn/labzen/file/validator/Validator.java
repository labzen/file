package cn.labzen.file.validator;

import jakarta.annotation.Nonnull;

import java.util.List;

/**
 * 校验器接口
 * <p>
 * 在导入管线中执行校验。校验器分为即时执行（逐行）和延后执行（全量完成后）两种模式。
 *
 * @author labzen
 */
public interface Validator {

  String RANGE_LENGTH_NAME = "range-length";
  int RANGE_LENGTH_PRIORITY = 10;
  String DEPENDS_ON_NAME = "depends-on";
  int DEPENDS_ON_PRIORITY = 90;

  String REQUIRE_NAME = "require";
  int REQUIRE_PRIORITY = 100;
  String RANGE_NUMERIC_NAME = "range-numeric";
  int RANGE_NUMERIC_PRIORITY = 110;
  String RANGE_DATE_NAME = "range-date";
  int RANGE_DATE_PRIORITY = 120;
  String UNIQUE_NAME = "unique";
  int UNIQUE_PRIORITY = 190;

  /**
   * 执行校验
   *
   * @param context   校验上下文
   * @param arguments 校验参数（来自YAML配置）
   * @return 校验结果，null表示通过
   */
  ValidateResult validate(@Nonnull ValidateContext<?> context, @Nonnull List<Object> arguments);
}
