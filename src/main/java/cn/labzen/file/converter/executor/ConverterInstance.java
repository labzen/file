package cn.labzen.file.converter.executor;

import cn.labzen.file.converter.Converter;

record ConverterInstance<T extends Converter>(T converter, int priority) {
}
