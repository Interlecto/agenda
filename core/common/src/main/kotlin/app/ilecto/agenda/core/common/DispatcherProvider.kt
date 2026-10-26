package app.ilecto.agenda.core.common

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

/** Indirection over [Dispatchers] so coroutine dispatchers can be swapped out in tests. */
interface DispatcherProvider {
    val io: CoroutineDispatcher
    val default: CoroutineDispatcher
    val main: CoroutineDispatcher
}

// Bound to DispatcherProvider via a Hilt @Binds module in :core:data. Only the
// lightweight javax.inject annotation is used here, not Hilt itself, so this
// module stays framework-agnostic like core:model and core:domain.
class DefaultDispatcherProvider @Inject constructor() : DispatcherProvider {
    override val io: CoroutineDispatcher = Dispatchers.IO
    override val default: CoroutineDispatcher = Dispatchers.Default
    override val main: CoroutineDispatcher = Dispatchers.Main
}
