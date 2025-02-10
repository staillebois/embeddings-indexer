package com.st

import dev.langchain4j.data.document.Metadata
import dev.langchain4j.data.embedding.Embedding
import dev.langchain4j.data.segment.TextSegment
import jakarta.annotation.PostConstruct
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.eclipse.microprofile.reactive.messaging.Incoming
import dev.langchain4j.store.embedding.elasticsearch.ElasticsearchEmbeddingStore;
import org.apache.http.HttpHost
import org.elasticsearch.client.RestClient
import org.jboss.logging.Logger
import java.lang.String.join


class EmbeddingsResource {

    @ConfigProperty(name = "elastic.url")
    var elasticUrl: String? = null

    @ConfigProperty(name = "elastic.socketTimeout")
    var elasticSocketTimeout: Int = 0

    @ConfigProperty(name = "elastic.indexName")
    var elasticIndexName: String? = null

    private var store: ElasticsearchEmbeddingStore? = null

    @PostConstruct
    fun initialize() {
        store = ElasticsearchEmbeddingStore.builder()
            .restClient(buildRestClient())
            .indexName(elasticIndexName)
            .build()
        LOGGER.info("Elasticsearch Indexing Client OK on: $elasticUrl for index $elasticIndexName")
    }

    private fun buildRestClient(): RestClient {
        return RestClient.builder(HttpHost.create(elasticUrl))
            .setRequestConfigCallback{requestConfigBuilder -> requestConfigBuilder.setSocketTimeout(elasticSocketTimeout)}
            .build()
    }

    @Incoming("rss-embeddings")
    fun rssFeed(rssEmbeddings: RssEmbeddings){
        val metas: Map<String, String> = java.util.Map.of(
            "title", rssEmbeddings.title,
            "link", rssEmbeddings.link,
            "pubDate", rssEmbeddings.pubDate,
            "category", rssEmbeddings.category
        )
        // Join with double lineSeparator (\n\n) in order to leverage DocumentByParagraphSplitter
        val content: String = join(System.lineSeparator() + System.lineSeparator(),
            listOf(rssEmbeddings.title,rssEmbeddings.description))
        val embedding: Embedding = Embedding.from(rssEmbeddings.embeddings)
        store?.add(embedding, TextSegment(content, Metadata(metas)))
    }

    companion object {
        private val LOGGER: Logger = Logger.getLogger(EmbeddingsResource::class.java)
    }
}