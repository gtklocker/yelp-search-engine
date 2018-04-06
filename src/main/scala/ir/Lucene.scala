package ir

import java.nio.file.Paths

import org.apache.lucene.analysis.core.SimpleAnalyzer
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.document.{Document, TextField, StringField, Field}
import org.apache.lucene.index.{DirectoryReader, IndexWriter, IndexWriterConfig, Term}
import org.apache.lucene.search.{IndexSearcher, Query, TermQuery, TopDocs}
import org.apache.lucene.store.FSDirectory

object Lucene {
  val homePath = System.getProperty("user.home")
  val indexPath = Paths.get(homePath, "yelp_index")
  val directory = FSDirectory.open(indexPath)
  val writerConfig = new IndexWriterConfig(new SimpleAnalyzer)
}
