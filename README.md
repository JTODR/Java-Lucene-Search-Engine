# Java Lucene Search Engine for the Cranfield Collection
An information retrieval Java search engine that indexes the Cranfield Collection of [documents](https://github.com/JTODR/Java-Lucene-Search-Engine/blob/master/data/cran.all.1400) and [queries](https://github.com/JTODR/Java-Lucene-Search-Engine/blob/master/data/cran.qry) the indexed documents using the Cranfield Collection queries. The search engine performance can be measured using the information retrieval metrics tool trec_eval. The results of the queries from this search engine are in the [output folder](https://github.com/JTODR/Java-Lucene-Search-Engine/tree/master/output). These results are compared with the trec_eval ground truth for the Cranfield Collection, which is located in the [cranqreltrec file](https://github.com/JTODR/Java-Lucene-Search-Engine/blob/master/data/cranqreltrec).

The **Apache Lucene** framework is used to create the index and query the indexed documents. **Maven** is used to manage dependencies.

----

## Running the Search Engine

The IndexDocs.java file indexes the Cranfield Collection documents. The QueryDocs.java file queries the indexed documents. The IndexDocs.java file must be run before running the QueryDocs.java file as the index must be created before it can be queried. 

### Running IndexDocs.java

An analyzer must be specified as a command line argument when running the IndexDocs.java file. The chosen analyzer can be a Standard Analyzer or an English Analyzer. 

To run IndexDocs.java: 

    java IndexDocs.java -analyzer [Standard | English]

### Running QueryDocs.java

An analyzer and a similarity scoring method must be specified as command line arguments when running the QueryDocs.java file. The chosen analyzer can be a Standard Analyzer or an English Analyzer. The chosen similarity score can be BM25 or VSM.

To run QueryDocs.java: 

    java QueryDocs.java -analyzer [Standard | English] -similarity [BM25 | VSM]

----

## Search Engine Components

The index engine follows the following method:
1. Retrieves the chosen analzyer.
2. Retrieves the parsed Cranfield documents.
3. Creates the indexer with the chosen analyzer.
4. Indexes each document on the document ID, the document title and the document contents.


The query engine follows the following method:
1. Retrieves the chosen analzyer and similarity scoring method.
2. Retrieves the parsed Cranfield queries.
3. Retrieves the common words list that is used to improve query quality.
4. For each query, remove words that match words in the common words list.
5. Creates a multi-field query parser with the chosen analyzer and boosts. A boost is applied to the contents of an indexed document over its title.
6. Performs the query search with the chosen similarity scoring method. 
7. Outputs the results of each query to the SearchEngineResults.txt output file. 
