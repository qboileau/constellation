/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2014 Geomatys.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.constellation.admin.index.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.NoLockFactory;
import org.apache.lucene.util.Version;
import org.apache.sis.metadata.iso.DefaultMetadata;
import org.apache.sis.util.logging.Logging;
import org.constellation.admin.exception.ConstellationException;
import org.constellation.admin.index.IndexEngine;
import org.constellation.configuration.ConfigDirectory;
import org.constellation.utils.MetadataFeeder;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Implementation of {@link IndexEngine} with Lucene.
 *
 * @author Christophe Mourette (Geomatys).
 * @author Christophe Mourette (Geomatys).
 * @since 0.9
 */
@Component
public class LuceneIndexEngine implements IndexEngine {

    /**
     * Used for debugging purposes.
     */
    private static final Logger LOGGER = Logging.getLogger(LuceneIndexEngine.class);

    private IndexWriter indexWriter;

    private IndexSearcher indexSearcher;


    @PostConstruct
    private void init() throws IOException {
        createIndex();
        initIndexSearcher();

    }

    private void createIndex() throws IOException {
        final File dataIndexDirectory = ConfigDirectory.getDataIndexDirectory();
        final Directory directory = FSDirectory.open(dataIndexDirectory, NoLockFactory.getNoLockFactory());
        StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_46);
        IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_46, analyzer);
        indexWriter = new IndexWriter(directory, config);
        indexWriter.commit();
    }

    private void initIndexSearcher() throws IOException {

        DirectoryReader directoryReader = DirectoryReader.open(indexWriter.getDirectory());
        indexSearcher = new IndexSearcher(directoryReader);
    }

    @PreDestroy
    private void destroy() throws IOException {
        LOGGER.info("closing metadata index");
        indexWriter.close();
    }

    /**
     * Adding metadata for given data Id into Lucene index.
     * @param metadata given metadata object to index.
     * @param dataId data Id.
     */
    public void addMetadataToIndexForData(final DefaultMetadata metadata,final Integer dataId) throws ConstellationException {
        final MetadataFeeder metadataFeeder = new MetadataFeeder(metadata);
        final Document doc = new Document();
        doc.add(new IntField("dataId",dataId, Field.Store.YES));
        addDocument(metadataFeeder,doc);
    }

    /**
     * Adding metadata for given data Id into Lucene index.
     *
     * @param metadata given metadata object to index.
     * @param datasetId dataset Id.
     * @throws ConstellationException
     */
    public void addMetadataToIndexForDataset(final DefaultMetadata metadata,final Integer datasetId) throws ConstellationException {
        final MetadataFeeder metadataFeeder = new MetadataFeeder(metadata);
        final Document doc = new Document();
        doc.add(new IntField("datasetId", datasetId, Field.Store.YES));
        addDocument(metadataFeeder, doc);
    }

    private void addDocument(final MetadataFeeder metadataFeeder,final Document doc) throws ConstellationException {
        try {
            final String keywords = StringUtils.join(metadataFeeder.getKeywordsNoType(), " ");
            if (keywords!=null && keywords.length()>0) {
                doc.add(new TextField("keywords", keywords, Field.Store.NO));
            }
            final String title = metadataFeeder.getTitle();
            if (title != null && title.length()>0){
                doc.add(new TextField("title", title, Field.Store.NO));
            }
            final String abstractField = metadataFeeder.getAbstract();
            if (abstractField != null && abstractField.length()>0) {
                doc.add(new TextField("abstract", abstractField, Field.Store.NO));
            }
            final List<String> topicCategories = metadataFeeder.getAllTopicCategory();
            if (topicCategories != null && topicCategories.size()>0) {
                doc.add(new TextField("topic", StringUtils.join(topicCategories, ' '), Field.Store.NO));
            }
            final List<String> sequenceIdentifiers = metadataFeeder.getAllSequenceIdentifier();
            if (sequenceIdentifiers != null && sequenceIdentifiers.size()>0){
                doc.add(new TextField("data",StringUtils.join(sequenceIdentifiers, ' '),Field.Store.NO));
            }
            final String processingLevel = metadataFeeder.getProcessingLevel();
            if (processingLevel != null && processingLevel.length()>0){
                doc.add(new TextField("level",processingLevel,Field.Store.NO));
            }
            final List<String> geographicIdentifiers = metadataFeeder.getAllGeographicIdentifier();
            if (geographicIdentifiers != null && geographicIdentifiers.size()>0){
                doc.add(new TextField("area",StringUtils.join(sequenceIdentifiers, ' '),Field.Store.NO));
            }
            indexWriter.addDocument(doc);
            indexWriter.commit();
        } catch (IOException e) {
            throw new ConstellationException(e);
        }
    }

    /**
     * Returns {@code Set} of integer id of documents that matches lucene query.
     * the id of document is under the property dataId or datasetId.
     *
     * @param queryString the lucene query
     * @param attributeId the attribute targeted for document to return, possible values are dataId or datasetId.
     * @return {@code Set} of id of dataset if attributeId is 'dataset', and of data if attributeId is 'dataId'.
     * @throws ParseException
     * @throws IOException
     */
    public Set<Integer> searchOnMetadata(final String queryString, final String attributeId) throws ParseException, IOException {
        final Set<Integer> result = new HashSet<>();
        initIndexSearcher();
        final TopScoreDocCollector collector = TopScoreDocCollector.create(5, true);
        final MultiFieldQueryParser queryParser = new MultiFieldQueryParser(Version.LUCENE_46, new String[]{
                "title", "abstract", "keywords", "topic", "data", "level", "area" }, new StandardAnalyzer(Version.LUCENE_46));
        queryParser.setDefaultOperator(QueryParser.Operator.OR);
        final Query q = queryParser.parse(queryString);
        indexSearcher.search(q, collector);
        final ScoreDoc[] hits = collector.topDocs().scoreDocs;
        for (final ScoreDoc scoreDoc : hits){
            final Document doc = indexSearcher.doc(scoreDoc.doc);
            final String value = doc.get(attributeId);
            if(value !=null){
                final Integer dataId = Integer.valueOf(doc.get(attributeId));
                result.add(dataId);
            }
        }
        return result;
    }

}
