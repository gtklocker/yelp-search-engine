package example

import java.sql.{Connection,DriverManager}

import org.apache.lucene.analysis.core.SimpleAnalyzer
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.document.{Document, TextField, StringField, Field}
import org.apache.lucene.index.{DirectoryReader, IndexWriter, IndexWriterConfig, Term}
import org.apache.lucene.search.{IndexSearcher, Query, TermQuery, TopDocs}
import org.apache.lucene.store.RAMDirectory

object Hello extends App {
  val directory = new RAMDirectory
  val writerConfig = new IndexWriterConfig(new SimpleAnalyzer)
  val writer = new IndexWriter(directory, writerConfig)

  val doc = new Document
  doc.add(new TextField("tweet", "the weather is really sunny today in GLasGoWw", Field.Store.YES))
  doc.add(new StringField("author", "gtklocker", Field.Store.YES))
  writer.addDocument(doc)

  println(s"${writer.numDocs} document(s) indexed!")
  writer.close

  val reader = DirectoryReader.open(directory)
  val searcher = new IndexSearcher(reader)

  val searchTerm = "sunny"
  println(s"Searching for ${searchTerm}...")
  val query = new TermQuery(new Term("tweet", searchTerm))
  val results = searcher.search(query, 10)

  println(s"${results.totalHits} result(s).")

  val topResult = searcher.doc(results.scoreDocs(0).doc)
  val tweet = topResult.getField("tweet").stringValue
  val author = topResult.getField("author").stringValue
  println(s"Top result: @${author}: ${tweet}.")
}
