package io.gitlab.leibnizhu.vertXearch.utils

import org.apache.lucene.document.{FieldType, StringField}
import org.apache.lucene.index.IndexOptions

object FieldTypeFactory {
  private val snaft:FieldType = new StoredNotAnalyzedFieldType
  private val snift:FieldType = new StoredNotIndexedFieldType

  def storedNotAnalyzed: FieldType = snaft
  def storedNotIndexed: FieldType = snift
}

class StoredNotAnalyzedFieldType() extends FieldType(StringField.TYPE_STORED) {
  this.setOmitNorms(false)
}

class StoredNotIndexedFieldType() extends FieldType(StringField.TYPE_STORED) {
  this.setIndexOptions(IndexOptions.NONE)
}