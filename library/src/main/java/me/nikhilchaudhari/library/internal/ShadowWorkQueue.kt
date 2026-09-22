package me.nikhilchaudhari.library.internal

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import java.util.concurrent.atomic.AtomicLong

/** Bounded single-flight generation. Work is cancelled after the last visible subscriber leaves. */
internal object ShadowWorkQueue {
    private data class Request(val result: Deferred<Boolean>, var subscribers: Int = 0)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val permits = Semaphore(2)
    private val pending = mutableMapOf<String, Request>()
    private val elapsedNs = AtomicLong()
    private val completed = AtomicLong()
    private val failures = AtomicLong()

    suspend fun generate(key: String, work: () -> Boolean): Boolean {
        val request = synchronized(pending) {
            val existing = pending[key]?.takeUnless { it.result.isCancelled }
            val selected = existing ?: if (pending.size >= 64) null else {
                val deferred = scope.async(start = CoroutineStart.LAZY) {
                    permits.withPermit {
                        ensureActive()
                        val start = System.nanoTime()
                        try { work() }
                        catch (cancelled: CancellationException) { throw cancelled }
                        catch (_: Exception) { failures.incrementAndGet(); false }
                        finally { elapsedNs.addAndGet(System.nanoTime() - start); completed.incrementAndGet() }
                    }
                }
                Request(deferred).also { created ->
                    pending[key] = created
                    deferred.invokeOnCompletion { synchronized(pending) { pending.remove(key, created) } }
                }
            }
            selected?.also { it.subscribers++; it.result.start() }
        } ?: return false
        try {
            return request.result.await()
        } finally {
            synchronized(pending) {
                request.subscribers--
                if (request.subscribers == 0 && !request.result.isCompleted) {
                    pending.remove(key, request)
                    request.result.cancel()
                }
            }
        }
    }
    fun stats(): Triple<Int, Double, Long> = synchronized(pending) {
        Triple(pending.size, elapsedNs.get() / 1_000_000.0 / completed.get().coerceAtLeast(1), failures.get())
    }
}
