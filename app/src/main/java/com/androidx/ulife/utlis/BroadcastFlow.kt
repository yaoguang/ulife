package com.androidx.ulife.utlis

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

fun <T> Context.observeBroadcast(
    filter: IntentFilter,
    onStartListening: () -> Unit = {},
    extractData: (Context, Intent) -> T
): Flow<T> {
    return flow {
        val channel = Channel<T>(Channel.UNLIMITED)
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                channel.trySend(extractData(ctx, intent))
            }
        }
        registerReceiver(receiver, filter)
        onStartListening()
        try {
            channel.consumeEach {
                emit(it)
            }
        } finally {
            unregisterReceiver(receiver)
        }
    }
}