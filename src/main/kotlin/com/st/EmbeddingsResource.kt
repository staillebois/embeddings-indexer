package com.st

import org.eclipse.microprofile.reactive.messaging.Incoming
import org.jboss.logging.Logger

class EmbeddingsResource {

    @Incoming("rss-embeddings")
    fun rssFeed(rssEmbeddings: RssEmbeddings){
        LOGGER.info("Received RSS embeddings: $rssEmbeddings")
    }

    companion object {
        private val LOGGER: Logger = Logger.getLogger(EmbeddingsResource::class.java)
    }
}