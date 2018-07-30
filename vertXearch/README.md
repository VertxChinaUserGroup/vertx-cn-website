# vertXearch
一个基于Vert.X的站内文章搜索引擎,为Vert.X中国论坛提供.  

## 架构
第一期:Vert.X+Lucene+HanLP,快速构建  
第二期:考虑使用Vert.X改造Lucene,提高性能  
目前项目提供了两个Verticle:  
- `io.gitlab.leibnizhu.vertXearch.verticle.HttpSearchVerticle`: 提供http查询接口, 默认监听8083端口
- `io.gitlab.leibnizhu.vertXearch.verticle.EventbusSearchVerticle`: 提供Eventbus查询接口, 默认监听地址为`"search"`

默认的打包配置里使用http查询接口的Verricle.  

## 使用
### 编译打包
本项目支持使用`Maven`和`Gradle`进行构建,任选一个就可以.  
使用`Maven`进行构建:  
```bash
# 使用Maven
mvn clean package
```
使用`Gradle`进行构建,默认任务是清空,测试并打jar包
```bash
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
  "serverPort": 8083,
  /*EventBus监听地址*/
  "eventbusAddress": "search",
  /*返回的关键词前置标签,默认红色字体*/
  "keywordPreTag": "<font color='red'>",
  /*返回的关键词后置标签,要和前置标签闭合*/
  "keywordPostTag":"</font>"
}
```

### 启动
默认配置的main类是用于启动http查询接口的,如果要启动eventbus查询接口的,请自行deploy.  
注意:使用`Maven`和`Gradle`的打包路径不一样,因此命令不一样.  
如果使用Maven:
```bash
java -jar target/vertXearch-0.0.1-fat.jar /path/to/config.json
```
如果使用Gradle:
```bash
java -jar target/lib/vertXearch-0.0.1-fat.jar /path/to/config.json
```
提供了一个简单的查询页面: [http://localhost:8083/static/](http://localhost:8083/static/)

## 接口
### HTTP查询接口
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

### Eventbus查询接口
- 地址: 默认是`"search"`, 可以通过配置文件修改,详见`修改配置文件`小节
- 请求格式: 接收`JsonObject`对象,格式如下:  

```json
{
  "method" : "search",
  "content": {
    "keyword": "搜索关键词",
    "length": "搜索结果最大条数,正整数,选填"
  }
}
```
**注1**: 以上json各个key可以在`EventbusRequestUtil`对象中进行修改.  
**注2**: 可以通过`EventbusRequestUtil`对象的以下方法快速构建请求json对象(由于是单例对象,在Java和Scala中都可以直接通过类名调用以下方法):  
```scala
def searchRequest(keyword: String): JsonObject
def searchRequest(keyword: String, length: Int): JsonObject
```
- 返回格式: JSON
- 返回值实例: 与HTTP接口返回格式一致