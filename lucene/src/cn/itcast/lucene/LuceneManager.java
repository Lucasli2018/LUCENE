package cn.itcast.lucene;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.junit.Test;
import org.wltea.analyzer.lucene.IKAnalyzer;

public class LuceneManager {

	@Test
	public void createDump() throws IOException {
		//先指定索引库存放的位置
		String dumpPath = "E:\\it\\java\\source\\Lucene1110res\\temp";
		Directory directory = FSDirectory.open(new File(dumpPath));
		//索引库存放在内存中
		//Directory directory = new RAMDirectory();
		//指定分析器
		StandardAnalyzer standardAnalyzer = new StandardAnalyzer();
		IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_4_10_3, standardAnalyzer);
		//创建Indexwriter对象
		IndexWriter indexWriter = new IndexWriter(directory, config);
		
		//读取歌词并创建Document对象
		File musicDir = new File("E:\\it\\java\\source\\Lucene1110res\\lrc");
		for (File f:musicDir.listFiles()) {
			//判断是否是文件
			if (f.isFile()) {
				//创建Document对象
				Document document = new Document();
				//创建域
				//文件名称
				Field fieldName = new TextField("filename", f.getName(), Store.YES);
				//文件内容
				String contentString = FileUtils.readFileToString(f);
				Field fieldContent = new TextField("content", contentString, Store.YES);
				//文件路径
				Field fieldPath = new StoredField("path", f.getPath());
				//文件 的大小
				Field fieldSize = new LongField("size", FileUtils.sizeOf(f), Store.YES);
				//把域添加到Document中
				document.add(fieldName);
				document.add(fieldContent);
				document.add(fieldPath);
				document.add(fieldSize);
				//把Document写入索引库
				indexWriter.addDocument(document);
			}
		}
		
		//关闭indexwriter
		indexWriter.close();
		System.out.println("创建索引库 ok");
	}
	@Test
	public void queryIndex() throws IOException {
		//先指定索引库存放的位置
		String dumpPath = "E:\\it\\java\\source\\Lucene1110res\\temp";
		Directory directory = FSDirectory.open(new File(dumpPath));
		//创建IndexReader
		IndexReader indexReader = DirectoryReader.open(directory);
		//使用IndexSearcher查询
		IndexSearcher indexSearcher = new IndexSearcher(indexReader);
		//创建一个查询
		Query query = new TermQuery(new Term("id", "14"));
		//执行查询
		TopDocs topDocs = indexSearcher.search(query, 10);
		//取查询结果的总数量
		System.out.println(topDocs.totalHits);
		for (ScoreDoc scoreDoc:topDocs.scoreDocs) {
			Document document = indexSearcher.doc(scoreDoc.doc);
			//从document中取出域的内容
			System.out.println(document.get("filename"));
			System.out.println(document.get("content"));
			System.out.println(document.get("path"));
			System.out.println(document.get("size"));
		}
	}
	
	@Test
	public void testTokenStream() throws IOException {
		//创建一个分析器对象
//		StandardAnalyzer analyzer = new StandardAnalyzer();
//		CJKAnalyzer analyzer = new CJKAnalyzer();
//		SmartChineseAnalyzer analyzer = new SmartChineseAnalyzer();
		IKAnalyzer analyzer = new IKAnalyzer();
		//获得TokenStream
//		TokenStream tokenStream = standardAnalyzer.tokenStream("test", "Will meit away[00:50.00]And then a hero comes along");
//		TokenStream tokenStream = analyzer.tokenStream("test", "Tokenizer是分词器，负责将reader转换为语汇单元即进行分词，Lucene提供了很多的分词器，也可以使用第三方的分词，比如IKAnalyzer一个中文分词器。\n" +
//				"tokenFilter是分词过滤器，传智播客负责对语汇单元进行过滤庖丁解牛不知所云习大大，tokenFilter可以是一个过滤器链儿，Lucene提供了很多的分词器过滤器，比如大小写转换、去除停用词等。");
		TokenStream tokenStream = analyzer.tokenStream("test", "Do it right");
		//查看关键词属性
		CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);
		//偏移量属性
		OffsetAttribute offsetAttribute = tokenStream.addAttribute(OffsetAttribute.class);
		//重置tokenstream
		tokenStream.reset();
		while(tokenStream.incrementToken()) {
			System.out.println("start->" + offsetAttribute.startOffset());
			System.out.println(charTermAttribute);
			System.out.println("end->" + offsetAttribute.endOffset());
		}
		tokenStream.close();
	}
	
	@Test
	public void createIndex() throws IOException {
		//先指定索引库存放的位置
		String dumpPath = "E:\\it\\java\\source\\Lucene1110res\\temp";
		Directory directory = FSDirectory.open(new File(dumpPath));
		//指定分析器
		IKAnalyzer analyzer = new IKAnalyzer();
		IndexWriterConfig config = new IndexWriterConfig(Version.LATEST, analyzer);
		//创建indexwriter对象
		IndexWriter indexWriter = new IndexWriter(directory, config);
		//读取歌词并创建Document对象
		File musicDir = new File("D:\\传智播客\\01.课程\\04.lucene\\01.参考资料\\歌词\\ALL");
		int i = 0;
		for (File f:musicDir.listFiles()) {
			//判断是否是文件
			if (f.isFile()) {
				//创建Document对象
				Document document = new Document();
				//创建域
				Field fieldId = new StringField("id", "" + i++, Store.YES);
				//文件名称
				Field fieldName = new TextField("filename", f.getName(), Store.YES);
				//文件内容
				String contentString = FileUtils.readFileToString(f);
				Field fieldContent = new TextField("content", contentString, Store.YES);
				//文件路径
				Field fieldPath = new StoredField("path", f.getPath());
				//文件 的大小
				Field fieldSize = new LongField("size", FileUtils.sizeOf(f), Store.YES);
				//把域添加到Document中
				document.add(fieldName);
				document.add(fieldContent);
				document.add(fieldPath);
				document.add(fieldSize);
				document.add(fieldId);
				//把Document写入索引库
				indexWriter.addDocument(document);
			}
		}
		
		//关闭indexwriter
		indexWriter.close();
		
	}
	
	private IndexWriter getIndexWriter() {
		//先指定索引库存放的位置
		String dumpPath = "E:\\it\\java\\source\\Lucene1110res\\temp";
		Directory directory;
		IndexWriter indexWriter= null;
		try {
			directory = FSDirectory.open(new File(dumpPath));
			//指定分析器
			IKAnalyzer analyzer = new IKAnalyzer();
			IndexWriterConfig config = new IndexWriterConfig(Version.LATEST, analyzer);
			//创建indexwriter对象
			indexWriter = new IndexWriter(directory, config);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return indexWriter;
	}
	
	/**
	 * 向索引库中添加一个document
	 * <p>Title: addOneDocument</p>
	 * <p>Description: </p>
	 * @throws IOException 
	 */
	@Test
	public void addOneDocument() throws IOException {
		IndexWriter indexWriter = getIndexWriter();
		//向索引库中添加一个文档
		Document document  = new Document();
		//创建域
		Field fieldName = new TextField("fieldname", "我新添加的一个文档的标题" ,Store.YES);
		Field fieldContent = new TextField("content", "这是文档的内容，我就不告诉你是什么", Store.YES);
		Field fieldContent2 = new TextField("content", "使用索引目录和配置管理类创建索引器", Store.YES);
		document.add(fieldName);
		document.add(fieldContent);
		document.add(fieldContent2);
		//添加到索引库中
		indexWriter.addDocument(document);
		indexWriter.close();
//		indexWriter.commit();
	}
	/**
	 * 删除所有文档
	 * <p>Title: deleteIndex</p>
	 * <p>Description: </p>
	 * @throws IOException 
	 */
	@Test
	public void deleteAllIndex() throws IOException {
		IndexWriter indexWriter = getIndexWriter();
		indexWriter.deleteAll();
		indexWriter.commit();
	}
	
	/**
	 * 删除指定文档
	 * <p>Title: deleteIndex</p>
	 * <p>Description: </p>
	 * @throws IOException 
	 */
	@Test
	public void deleteIndex() throws IOException {
		IndexWriter indexWriter = getIndexWriter();
		//创建一个查询
		Query query = new TermQuery(new Term("filename", "张信哲"));
		indexWriter.deleteDocuments(query);
		indexWriter.commit();
	}
	
	private IndexSearcher getIndexSearcher() {
		IndexSearcher indexSearcher = null;
		try {
			//先指定索引库存放的位置
			String dumpPath = "E:\\it\\java\\source\\Lucene1110res\\temp";
			Directory directory = FSDirectory.open(new File(dumpPath));
			//创建IndexReader
			IndexReader indexReader = DirectoryReader.open(directory);
			//使用IndexSearcher查询
			indexSearcher = new IndexSearcher(indexReader);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return indexSearcher;	
	}
	
	private void printResult(IndexSearcher indexSearcher, Query query) {
		try {
			//执行查询
			TopDocs topDocs = indexSearcher.search(query, 10);
			//取查询结果的总数量
			System.out.println(topDocs.totalHits);
			for (ScoreDoc scoreDoc:topDocs.scoreDocs) {
				Document document = indexSearcher.doc(scoreDoc.doc);
				//从document中取出域的内容
				System.out.println(document.get("id"));
				System.out.println(document.get("filename"));
				System.out.println(document.get("content"));
				System.out.println(document.get("path"));
				System.out.println(document.get("size"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * 使用数值范围查询
	 * <p>Title: testNumericRangeQuery</p>
	 * <p>Description: </p>
	 */
	@Test
	public void testNumericRangeQuery() {
		IndexSearcher searcher = getIndexSearcher();
		//创建一个查询
		Query query = NumericRangeQuery.newLongRange("size", 0l, 100l, true, false);
		printResult(searcher, query);
	}
	
	/**
	 * 使用BooleanQuery实现多条件查询
	 */
	@Test
	public void testBooleanQuery() {
		IndexSearcher searcher = getIndexSearcher();
		
		//创建查询
		BooleanQuery query = new BooleanQuery();
		//查询条件
		Query query2 = new TermQuery(new Term("filename", "鸳鸯"));
		Query query3 = new TermQuery(new Term("filename", "黄安"));
		query.add(query2, Occur.MUST);
		query.add(query3, Occur.MUST_NOT);
		
		//打印结果
		printResult(searcher, query);
	}
	
	@Test
	public void testQueryPaser() throws Exception {
		IndexSearcher searcher = getIndexSearcher();
		//创建索引时使用的IKAnalyzer,查询索引时需要匹配。
		IKAnalyzer analyzer = new IKAnalyzer();
		//创建一个QueryParser对象
		//第一个参数：默认搜索的域
		//第二个参数：分析器
		QueryParser queryParser = new QueryParser("content", analyzer);
		//搜索默认域
//		Query query = queryParser.parse("Do it right");
		//搜索指定域
//		Query query = queryParser.parse("filename:鸳鸯");
		//组合条件查询
//		Query query = queryParser.parse("filename:鸳鸯 AND filename:黄安");
//		Query query = queryParser.parse("filename:鸳鸯 NOT filename:黄安");
		Query query = queryParser.parse("filename:鸳鸯 OR filename:黄安");
		//组合条件查询
//		Query query = queryParser.parse("+filename:鸳鸯  +filename:黄安");
//		Query query = queryParser.parse("+filename:鸳鸯  -filename:黄安");
//		Query query = queryParser.parse("filename:鸳鸯 filename:黄安");
		
		//范围查询
		//字符串类型的域
//		Query query = queryParser.parse("id:[2 TO 6]");
		//数值类型的域
//		Query query = queryParser.parse("size:[0 TO 100]");
		//打印结果
		printResult(searcher, query);
		
	}
	
	@Test
	public void testMultiFieldQueryParser() throws Exception {
		
		IndexSearcher searcher = getIndexSearcher();
		//域列表
		String[] fields = {"filename","content"};
		Analyzer analyzer = new IKAnalyzer();
		MultiFieldQueryParser queryParse = new MultiFieldQueryParser(fields, analyzer);
		Query query = queryParse.parse("周杰伦");
		
		//打印结果
		printResult(searcher, query);
	}
	@Test
	public void testBoost() throws IOException {
		//创建索引时设置boost
		IndexWriter indexWriter = getIndexWriter();
		//向索引库中添加一个文档
		Document document  = new Document();
		//创建域
		Field fieldName = new TextField("filename", "新鸳鸯蝴蝶梦洒落的空间法拉克撒娇的弗利萨进度洒落的减肥拉斯克奖的福利卡时间的福利卡检索33333" ,Store.YES);
//		fieldName.setBoost(10.0f);
		Field fieldContent = new TextField("content", "这是文档的内容，鸳鸯我就不告诉你是什么", Store.YES);
		Field fieldContent2 = new TextField("content", "使用索引目录和配置管理类创建索引器", Store.YES);
		document.add(fieldName);
		document.add(fieldContent);
		document.add(fieldContent2);
		//添加到索引库中
		indexWriter.addDocument(document);
		indexWriter.close();
	}
	@Test
	public void testBoostQuery() throws ParseException {
		IndexSearcher searcher = getIndexSearcher();
		//域列表
		String[] fields = {"filename","content"};
		Analyzer analyzer = new IKAnalyzer();
		Map<String, Float> boostMap = new HashMap<String, Float>();
		boostMap.put("filename", 1.0f);
		boostMap.put("content", 100.0f);
		
		MultiFieldQueryParser queryParse = new MultiFieldQueryParser(fields, analyzer, boostMap);
		Query query = queryParse.parse("鸳鸯");
		
		//打印结果
		printResult(searcher, query);
	}
	
}
