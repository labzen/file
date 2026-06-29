## 最终实现以下文件格式的导出：
- Excel
- CSV
- PDF (带水印)
- Markdown
- HTML
- Txt
- XML
- JSON
- YAML

----

- [x] 剔除掉resource下的默认字体文件，改为配置项（当前jar包太大了）
- [ ] PDF文件导出带水印
- [x] Excel文件导出实现改为使用Apache的Fesod
- [x] 加一个布尔类型转换器
- [x] 加载schema文件后，日志打印内容完善一下
- [ ] 导出PDF，列多的情况下，排版有些乱，最后几列显示不出来
- [x] 想个办法，支持i18n，配合paragon的界面语言选择使用
- [x] 导入功能
- [x] I18nStoreProvider 接口命名过于中性，重构名称
- [ ] json schema 文件中，对于filename和title，应该归入导出的配置中；相应的Bean中，应该处于 文件scope 的exporting中
- [ ] 导出带上（签名、日期）
- [ ] 导入的成功数据集合(ImportResult.getData())，带上序号信息
- [ ] 在导入的配置里，加上非空开关，遇到null值，给一个默认值
- [ ] 如果存在mapping，那么mock.json中对应的列的样例数据应该是mapping转换前的还是之后的??因为是导入模板，应该是转换后的。file应该不需要去处理
- [ ] 导入模板Excel中显示的mock.json数据应该也进行i18n文案转换