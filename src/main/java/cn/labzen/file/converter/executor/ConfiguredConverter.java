package cn.labzen.file.converter.executor;

import cn.labzen.file.converter.Converter;

import java.util.List;

record ConfiguredConverter<T extends Converter>(ConverterInstance<T> instance, List<Object> arguments) {
}
