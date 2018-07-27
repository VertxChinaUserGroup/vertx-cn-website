# vertXearch
一个基于Vert.X的站内文章搜索引擎,为Vert.X中国论坛提供.

## 架构
第一期:Vert.X+Lucene+HanLP,快速构建  
第二期:考虑使用Vert.X改造Lucene,提高性能

## 使用
### 编译
本项目支持使用`Maven`或`Gradle`进行构建,任选一个就可以
```bash
# 使用Maven
mvn clean package
# 也可以使用Gradle,默认任务是清空,测试并打fat包
gradle
```

### 修改配置文件
将`src/main/resources/config.json`复制到任意地方(以`/path/to/config.json`为例),修改其配置:
```json
{
  /*索引存放位置*/
  "indexPath":"/Users/leibnizhu/Desktop/Index",
  /*文章原始文件位置*/
  "articlePath":"/Users/leibnizhu/Desktop/Data",
  /*从articlePath更新索引的时间间隔,单位:秒*/
  "refreshIndexPerSecond": 10,
  /*服务端口*/
  "serverPort": 80833,
  /*返回的关键词前置标签,默认红色字体*/
  "keywordPreTag": "<font color='red'>",
  /*返回的关键词后置标签,要和前置标签闭合*/
  "keywordPostTag":"</font>"
}
```

### 启动
注意:使用`Maven`和`Gradle`的打包路径不一样,因此命令不一样
```bash
# 如果使用Maven:
java -jar target/vertXearch-0.0.1-fat.jar /path/to/config.json
# 如果使用Gradle:
java -jar target/lib/vertXearch-0.0.1-fat.jar /path/to/config.json
```
提供了一个简单的查询页面: [http://localhost:8083/static/](http://localhost:8083/static/)

## 接口
### 查询接口
- 地址: http://&lt;ip&gt;:&lt;port&gt;/q/&lt;keyword&gt;/[maxLength]
- 参数: keyword=关键词,必填;maxLength=查询结果的最大个数,可选,默认值是`Constants.MAX_SEARCH`
- 返回格式: JSON
- 返回值实例:

1. 正常返回:  
(有搜索结果):
```json
{
    "status": "success",
    "cost": 29, /*查询耗时,单位毫秒*/
    "results": [
        {
            "id": "7",
            "title": "<font color='red'>clojure</font>:hellovert.x(vertx-lang-clojure发布)",
            "author": "对对对",
            "content": "......上实现<font color='red'>clojure</font>语言......：vert.x上的<font color='red'>clojure</font>语言支持（项目已开源）​......"
        },
        /*........其他搜索结果........*/
        {
            "id": "8",
            "title": "关于vert.x的冷知识",
            "author": "ABC",
            "content": "......，详细见：etagsoc2018<font color='red'>clojure</font>的符号是......"
        }
    ]
}
```

或:(无搜索结果)
```json
{
    "status": "success",
    "cost": 29, /*查询耗时,单位毫秒*/
    "results": []
}
```

2. 错误返回:
```json
{
    "status": "error",
    "message": "后台发生爆炸事故"
}
```