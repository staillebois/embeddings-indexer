package com.st

import jakarta.annotation.PostConstruct
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.eclipse.microprofile.reactive.messaging.Incoming
import dev.langchain4j.store.embedding.elasticsearch.ElasticsearchEmbeddingStore;
import org.apache.http.HttpHost
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestClientBuilder
import org.jboss.logging.Logger


class EmbeddingsResource {

    @ConfigProperty(name = "elastic.url")
    var elasticUrl: String? = null

    @ConfigProperty(name = "elastic.dimension")
    var elasticDimension: Int = 0

    @ConfigProperty(name = "elastic.indexName")
    var elasticIndexName: String? = null

    private var store: ElasticsearchEmbeddingStore? = null

    @PostConstruct
    fun initialize() {
        val storeBuilder: ElasticsearchEmbeddingStore.Builder =
            ElasticsearchEmbeddingStore.builder().serverUrl(elasticUrl)
        store = storeBuilder
            .restClient(buildRestClient())
            .dimension(elasticDimension)
            .indexName(elasticIndexName)
            .build()
        LOGGER.info("Elasticsearch Indexing Client OK on: $elasticUrl")
    }

    private fun buildRestClient(): RestClient {
        val builder: RestClientBuilder = RestClient.builder(HttpHost.create(elasticUrl))
        return builder.build()
    }

    @Incoming("rss-embeddings")
    fun rssFeed(rssEmbeddings: RssEmbeddings){
        LOGGER.info("Received RSS embeddings: $rssEmbeddings")
    }

    companion object {
        private val LOGGER: Logger = Logger.getLogger(EmbeddingsResource::class.java)
    }
}