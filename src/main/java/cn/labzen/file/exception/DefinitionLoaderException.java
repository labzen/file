package cn.labzen.file.exception;

import cn.labzen.meta.exception.LabzenRuntimeException;
import lombok.Getter;

/**
 * 配置文件加载异常
 * <p>
 * 当 YAML 配置文件加载、解析或校验失败时抛出此异常
 *
 * @author labzen
 */
public class DefinitionLoaderException extends LabzenRuntimeException {

  public DefinitionLoaderException(String message, Object... args) {
    super(message, args);
  }
}
