package ir

import org.apache.lucene.analysis.core.SimpleAnalyzer
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.document.{Document, TextField, StringField, Field}
import org.apache.lucene.index.{DirectoryReader, IndexWriter, IndexWriterConfig, Term}
import org.apache.lucene.search.{IndexSearcher, Query, TermQuery, TopDocs}
import org.apache.lucene.store.RAMDirectory

object Lucene {
  val directory = new RAMDirectory
  val writerConfig = new IndexWriterConfig(new SimpleAnalyzer)
  val writer = new IndexWriter(directory, writerConfig)
}
