package dog.del.app.stats

import dog.del.data.base.DB
import dog.del.data.base.Database
import dog.del.data.base.model.document.XdDocument
import io.ktor.request.ApplicationRequest
import jetbrains.exodus.database.TransientEntityStore
import kotlinx.coroutines.*
import org.koin.core.KoinComponent
import org.koin.core.inject

class DBStatisticsReporter : StatisticsReporter, KoinComponent {
    override val showCount = true
    private val db by inject<TransientEntityStore>()
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.DB + job)

    override val embedCode = ""

    override fun reportImpression(slug: String, frontend: Boolean, request: ApplicationRequest) {
        scope.launch {
            db.transactional {
                XdDocument.find(slug)?.apply {
                    viewCount++
                }
            }
        }
    }

    override suspend fun getImpressions(slug: String): Int = withContext(scope.coroutineContext) {
        db.transactional(readonly = true) {
            XdDocument.find(slug)?.viewCount ?: 0
        }
    }

    override fun reportEvent(event: StatisticsReporter.Event, request: ApplicationRequest) {
        // Not implemented for our basic db reporter
    }

    override fun getUrl(slug: String): String? = null
}