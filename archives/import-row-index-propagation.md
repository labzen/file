# 导入数据 Excel 行号向下游传递 — 实现总结

> 实现日期：2026-06-30  
> 实现人：AI Agent  
> 来源文档：`C:\Working\labzen\paragon\.archives\todo\import-row-index-propagation.md`

---

## 一、问题回顾

导入流程中，Excel 行号（`sequence`）在 `ImportProcessor.process()` 构建 `ImportResult` 时丢失：

```
ProceedRow(sequence="3", instance=bean)
    ↓
ImportResult.data = List<T>       ← ❌ 只剩 bean，行号丢失
    ↓
下游 saveWithErrorTracking() 用 `i + 1` 近似行号 → 不准确
```

## 二、实现方案

引入 `PositionedData<T>` record 包装类，在 `ImportResult` 中绑定行号：

```
ProceedRow(sequence="3", instance=bean)
    ↓
PositionedData(sequence="3", payload=bean)
    ↓
ImportResult.data = List<PositionedData<T>>   ← ✅ 行号保留
```

## 三、变更文件

### 3.1 新增：`PositionedData.java`

```
src/main/java/cn/labzen/file/format/core/reader/process/PositionedData.java
```

单字段包装 record：
- `sequence`（String）— 原始文件行号
- `payload`（T）— 领域 Bean（失败行为 null）

### 3.2 修改：`ImportResult.java`

| 变更项 | 之前 | 之后 |
|--------|------|------|
| `data` 字段类型 | `List<T>` | `List<PositionedData<T>>` |
| `getData()` 返回 | `List<T>` | `List<PositionedData<T>>` |
| 新增方法 | — | `getPayloads()` → `List<T>` |

`getPayloads()` 过滤掉 null（失败行），与旧版 `getData()` 语义一致，提供向下兼容。

### 3.3 修改：`ImportProcessor.java`

`process()` 方法第 82 行：

```java
// 之前
List<T> proceedBeans = proceedRows.stream().map(ProceedRow::instance).toList();

// 之后
List<PositionedData<T>> positionedData = proceedRows.stream()
    .map(row -> new PositionedData<>(row.sequence(), row.instance()))
    .toList();
```

### 3.4 影响范围

- **内部调用方**：零 — 项目中无任何内部调用 `ImportResult.getData()` 或 `ImportResult.data` 的代码
- **测试**：不涉及数据字段访问，无需修改
- **外部调用方**：`getData()` 返回类型变更（编译期 breaking），通过 `getPayloads()` 平滑迁移

## 四、调用方迁移指南

```java
// 之前（只取 bean 列表）
List<D> beans = result.getData();

// 之后（只取 bean 列表，不关心行号）
List<D> beans = result.getPayloads();

// 之后（需要行号信息）
List<PositionedData<D>> rows = result.getData();
for (PositionedData<D> row : rows) {
    if (row.payload() != null) {
        try {
            doSaveForImport(row.payload());
        } catch (Exception ex) {
            errors.add(new Error(row.sequence(), "保存失败"));
        }
    }
}
```

## 五、关键设计决策

| 决策 | 结论 |
|------|------|
| 命名 | `PositionedData` — 明确表达"带位置的数据" |
| 包位置 | `cn.labzen.file.format.core.reader.process` — 与 ImportResult/ImportProcessor 同包 |
| 向后兼容 | 提供 `getPayloads()` 方法，返回纯 Bean 列表 |
| 失败行处理 | payload 为 null，序列号保留（下游可跳过） |
| Map 快速查找 | 不需要 — 顺序遍历已满足需求 |

## 六、对 paragon 项目的影响

`FileHandleRealmTemplate.saveWithErrorTracking()` 中的 `i + 1` 行号问题得到解决：

```java
// 之前
for (int i = 0; i < data.size(); i++) {
    D domain = data.get(i);
    try {
        doSaveForImport(domain);
    } catch (Exception ex) {
        errors.add(new Error(String.valueOf(i + 1), ERROR_WHEN_SAVING_MESSAGES)); // ❌ 行号不准
    }
}

// 之后
for (PositionedData<D> row : result.getData()) {
    try {
        doSaveForImport(row.payload());
    } catch (Exception ex) {
        errors.add(new Error(row.sequence(), ERROR_WHEN_SAVING_MESSAGES)); // ✅ 精确行号
    }
}
```
