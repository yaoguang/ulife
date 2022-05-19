package com.androidx.ulife.utlis;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.androidx.ulife.net.RetrofitApi;
import com.androidx.ulife.net.SuspendCall;

import kotlin.coroutines.Continuation;

public class Test {
    public void test() {
        RetrofitApi.INSTANCE.suspendCall(new SuspendCall<Integer>() {
            @Nullable
            @Override
            public Object call(@NonNull Continuation<? super Integer> $completion) {
                return RetrofitApi.INSTANCE.getApiService().test($completion);
            }

            @Override
            public void onResponse(Integer response) {

            }

            @Override
            public void onFailure(@Nullable Throwable t) {

            }
        });
    }
}
