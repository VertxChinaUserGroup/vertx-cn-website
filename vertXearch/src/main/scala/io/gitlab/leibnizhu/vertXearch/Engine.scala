package io.gitlab.leibnizhu.vertXearch

import io.vertx.core.{AsyncResult, Handler}

trait Engine {
  /**
    * 对源目录下所有可用文件进行索引构建
    *
    */
  def createIndex(): Unit

  /**
    * 删除所有索引
    */
  def cleanAllIndex():Unit

  /**
    * 对源目录下所有可用文件进行索引更新
    *
    */
  def refreshIndex(): Unit

  /**
    * 启动文章更新定时器
    *
    * @param interval 定时间隔
    */
  def startRefreshTimer(interval: Long):Unit

  /**
    * 按指定关键词进行查找
    *
    * @param searchQuery 查找关键词
    * @param callback 查询成功后的回调方法, 处理内容为 匹配的文档,按相关度降序
    */
  def search(searchQuery: String, length: Int, callback: Handler[AsyncResult[List[Article]]]): Unit

  /**
    * 关闭搜索引擎
    */
  def stop(callback: Handler[AsyncResult[Unit]]): Unit
}
