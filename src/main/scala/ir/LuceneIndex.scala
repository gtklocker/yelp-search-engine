package ir

import java.nio.file.Paths
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.index.{DirectoryReader, IndexWriter, IndexWriterConfig}
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.store.FSDirectory

class LuceneIndex(name: String) {
  val homePath = System.getProperty("user.home")
  val indexPath = Paths.get(homePath, s"yelp_${name}_index")
  val directory = FSDirectory.open(indexPath)
  val writerConfig = new IndexWriterConfig(new StandardAnalyzer)
  def writer: IndexWriter = new IndexWriter(directory, writerConfig)
  def searcher: IndexSearcher = new IndexSearcher(DirectoryReader.open(directory))
}
