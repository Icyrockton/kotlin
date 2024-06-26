/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.coroutines

/**
 * Marks coroutine context element that intercepts coroutine continuations.
 * The coroutines framework uses [ContinuationInterceptor.Key] to retrieve the interceptor and
 * intercepts all coroutine continuations with [interceptContinuation] invocations.
 *
 * [ContinuationInterceptor] behaves like a [polymorphic element][AbstractCoroutineContextKey], meaning that
 * its implementation delegates [get][CoroutineContext.Element.get] and [minusKey][CoroutineContext.Element.minusKey]
 * to [getPolymorphicElement] and [minusPolymorphicKey] respectively.
 * [ContinuationInterceptor] subtypes can be extracted from the coroutine context using either [ContinuationInterceptor.Key]
 * or subtype key if it extends [AbstractCoroutineContextKey].
 */
@SinceKotlin("1.3")
public interface ContinuationInterceptor : CoroutineContext.Element {
    /**
     * The key that defines *the* context interceptor.
     */
    public companion object Key : CoroutineContext.Key<ContinuationInterceptor>

    /**
     * Returns continuation that wraps the original [continuation], thus intercepting all resumptions.
     * This function is invoked by coroutines framework when needed and the resulting continuations are
     * cached internally per each instance of the original [continuation].
     *
     * This function may simply return original [continuation] if it does not want to intercept this particular continuation.
     *
     * When the original [continuation] completes, coroutine framework invokes [releaseInterceptedContinuation]
     * with the resulting continuation if it was intercepted, that is if `interceptContinuation` had previously
     * returned a different continuation instance.
     */
    public fun <T> interceptContinuation(continuation: Continuation<T>): Continuation<T>

    /**
     * Invoked for the continuation instance returned by [interceptContinuation] when the original
     * continuation completes and will not be used anymore. This function is invoked only if [interceptContinuation]
     * had returned a different continuation instance from the one it was invoked with.
     *
     * Default implementation does nothing.
     *
     * @param continuation Continuation instance returned by this interceptor's [interceptContinuation] invocation.
     */
    public fun releaseInterceptedContinuation(continuation: Continuation<*>) {
        /* do nothing by default */
    }

    public override operator fun <E : CoroutineContext.Element> get(key: CoroutineContext.Key<E>): E? {
        // getPolymorphicKey specialized for ContinuationInterceptor key
        @OptIn(ExperimentalStdlibApi::class)
        if (key is AbstractCoroutineContextKey<*, *>) {
            @Suppress("UNCHECKED_CAST")
            return if (key.isSubKey(this.key)) key.tryCast(this) as? E else null
        }
        @Suppress("UNCHECKED_CAST")
        return if (ContinuationInterceptor === key) this as E else null
    }


    public override fun minusKey(key: CoroutineContext.Key<*>): CoroutineContext {
        // minusPolymorphicKey specialized for ContinuationInterceptor key
        @OptIn(ExperimentalStdlibApi::class)
        if (key is AbstractCoroutineContextKey<*, *>) {
            return if (key.isSubKey(this.key) && key.tryCast(this) != null) EmptyCoroutineContext else this
        }
        return if (ContinuationInterceptor === key) EmptyCoroutineContext else this
    }
}
