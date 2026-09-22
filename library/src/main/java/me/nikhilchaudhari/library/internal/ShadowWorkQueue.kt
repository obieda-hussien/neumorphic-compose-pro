package me.nikhilchaudhari.library.internal

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import java.util.concurrent.atomic.AtomicLong

/** Shared requests survive a single subscriber's cancellation; queue and concurrency are bounded. */
internal object ShadowWorkQueue {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val permits = Semaphore(2)
    private val pending = mutableMapOf<String, Deferred<Boolean>>()
    private val elapsedNs = AtomicLong()
    private val completed = AtomicLong()
    private val failures = AtomicLong()

    suspend fun generate(key: String, work: () -> Boolean): Boolean {
        val job = synchronized(pending) {
            pending[key] ?: if (pending.size >= 64) null else {
                scope.async(start = CoroutineStart.LAZY) {
                    permits.withPermit {
                        val start = System.nanoTime()
                        try { work() }
                        catch (cancelled: CancellationException) { throw cancelled }
                        catch (_: Exception) { failures.incrementAndGet(); false }
                        finally { elapsedNs.addAndGet(System.nanoTime() - start); completed.incrementAndGet() }
                    }
                }.also { deferred ->
                    pending[key] = deferred
                    deferred.invokeOnCompletion { synchronized(pending) { pending.remove(key, deferred) } }
                    deferred.start()
                }
            }
        }
        return job?.await() ?: false
    }
    fun stats(): Triple<Int, Double, Long> = synchronized(pending) {
        Triple(pending.size, elapsedNs.get() / 1_000_000.0 / completed.get().coerceAtLeast(1), failures.get())
    }
}
