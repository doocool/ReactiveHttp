package github.leavesc.reactivehttp.datasource

import github.leavesc.reactivehttp.callback.RequestPairCallback
import github.leavesc.reactivehttp.callback.RequestTripleCallback
import github.leavesc.reactivehttp.exception.ServerCodeBadException
import github.leavesc.reactivehttp.mode.IHttpWrapMode
import github.leavesc.reactivehttp.viewmodel.IUIAction
import kotlinx.coroutines.*

/**
 * @Author: leavesC
 * @Date: 2020/5/4 0:55
 * @Desc:
 * 提供了 两个/三个 接口同时并发请求的方法
 * 当所有接口都请求成功时，会通过 onSuccess 方法传出请求结果
 * 当包含的某个接口请求失败时，则会直接回调 onFail 方法
 * @GitHub：https://github.com/leavesC
 */
abstract class RemoteExtendDataSource<Api : Any>(
    iUIAction: IUIAction?,
    baseHttpUrl: String,
    apiServiceClass: Class<Api>
) : RemoteDataSource<Api>(iUIAction, baseHttpUrl, apiServiceClass) {

    fun <DataA, DataB> enqueue(
        apiFunA: suspend Api.() -> IHttpWrapMode<DataA>,
        apiFunB: suspend Api.() -> IHttpWrapMode<DataB>,
        showLoading: Boolean = false,
        callbackFun: (RequestPairCallback<DataA, DataB>.() -> Unit)? = null
    ): Job {
        return lifecycleSupportedScope.launch(Dispatchers.Main.immediate) {
            val callback = if (callbackFun == null) {
                null
            } else {
                RequestPairCallback<DataA, DataB>().apply {
                    callbackFun.invoke(this)
                }
            }
            try {
                if (showLoading) {
                    showLoading()
                }
                callback?.onStart?.invoke()
                val taskList = listOf(
                    lifecycleSupportedScope.async { apiFunA.invoke(apiService) },
                    lifecycleSupportedScope.async { apiFunB.invoke(apiService) }
                )
                val responseList = taskList.awaitAll()
                val failed = responseList.find { it.httpIsFailed }
                if (failed != null) {
                    throw ServerCodeBadException(failed)
                }
                onGetResponse(callback, responseList)
            } catch (throwable: Throwable) {
                handleException(throwable, callback)
            } finally {
                try {
                    callback?.onFinally?.invoke()
                } finally {
                    if (showLoading) {
                        dismissLoading()
                    }
                }
            }
        }
    }

    private suspend fun <DataA, DataB> onGetResponse(
        callback: RequestPairCallback<DataA, DataB>?,
        responseList: List<IHttpWrapMode<out Any?>>,
    ) {
        callback ?: return
        withContext(NonCancellable) {
            callback.onSuccess?.let {
                withContext(Dispatchers.Main.immediate) {
                    it.invoke(
                        responseList[0].httpData as DataA,
                        responseList[1].httpData as DataB
                    )
                }
            }
            callback.onSuccessIO?.let {
                withContext(Dispatchers.IO) {
                    it.invoke(
                        responseList[0].httpData as DataA,
                        responseList[1].httpData as DataB
                    )
                }
            }
        }
    }

    fun <DataA, DataB, DataC> enqueue(
        apiFunA: suspend Api.() -> IHttpWrapMode<DataA>,
        apiFunB: suspend Api.() -> IHttpWrapMode<DataB>,
        apiFunC: suspend Api.() -> IHttpWrapMode<DataC>,
        showLoading: Boolean = false,
        callbackFun: (RequestTripleCallback<DataA, DataB, DataC>.() -> Unit)? = null
    ): Job {
        return lifecycleSupportedScope.launch(Dispatchers.Main.immediate) {
            val callback = if (callbackFun == null) {
                null
            } else {
                RequestTripleCallback<DataA, DataB, DataC>().apply {
                    callbackFun.invoke(this)
                }
            }
            try {
                if (showLoading) {
                    showLoading()
                }
                val taskList = listOf(
                    lifecycleSupportedScope.async { apiFunA.invoke(apiService) },
                    lifecycleSupportedScope.async { apiFunB.invoke(apiService) },
                    lifecycleSupportedScope.async { apiFunC.invoke(apiService) }
                )
                val responseList = taskList.awaitAll()
                val failed = responseList.find { it.httpIsFailed }
                if (failed != null) {
                    throw ServerCodeBadException(failed)
                }
                onGetResponse(callback, responseList)
            } catch (throwable: Throwable) {
                handleException(throwable, callback)
            } finally {
                try {
                    callback?.onFinally?.invoke()
                } finally {
                    if (showLoading) {
                        dismissLoading()
                    }
                }
            }
        }
    }

    private suspend fun <DataA, DataB, DataC> onGetResponse(
        callback: RequestTripleCallback<DataA, DataB, DataC>?,
        responseList: List<IHttpWrapMode<out Any?>>
    ) {
        callback ?: return
        withContext(NonCancellable) {
            callback.onSuccess?.let {
                withContext(Dispatchers.Main.immediate) {
                    it.invoke(
                        responseList[0].httpData as DataA,
                        responseList[1].httpData as DataB,
                        responseList[2].httpData as DataC
                    )
                }
            }
            callback.onSuccessIO?.let {
                withContext(Dispatchers.IO) {
                    it.invoke(
                        responseList[0].httpData as DataA,
                        responseList[1].httpData as DataB,
                        responseList[2].httpData as DataC
                    )
                }
            }
        }
    }

}