package org.jellyplus.client.domain.models

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

actual val Dispatchers.IO: CoroutineDispatcher get() = Dispatchers.Default
