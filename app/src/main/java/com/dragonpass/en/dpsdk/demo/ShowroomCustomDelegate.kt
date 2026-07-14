package com.dragonpass.en.dpsdk.demo

import androidx.fragment.app.FragmentActivity
import com.dragonpass.spark.dpweb.CallBackFunction
import com.dragonpass.spark.dpweb.entity.JsCustomEventMsgEntity
import com.dragonpass.spark.dpweb.web.DPSDK

/**
 * Showroom 专用的 Custom Event 处理器。
 * 拦截 requestPayment 直接返回成功，其余事件不做处理。
 */
object ShowroomCustomDelegate {

    fun handle(
        activity: FragmentActivity,
        data: String,
        event: JsCustomEventMsgEntity,
        callback: CallBackFunction?
    ) {
        when (event.eventType) {
            "requestPayment" -> {
                callback?.onCallBack(
                    """{"statusCode":1,"data":{"message":"Showroom: payment bypassed"}}"""
                )
            }
            else -> {
                callback?.onCallBack(
                    """{"statusCode":1,"data":{"message":"Showroom: unhandled event ${event.eventType}"}}"""
                )
            }
        }
    }
}
