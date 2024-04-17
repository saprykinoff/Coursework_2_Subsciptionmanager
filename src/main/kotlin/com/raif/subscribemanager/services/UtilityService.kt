package com.raif.subscribemanager.services

import org.json.JSONObject
import org.springframework.stereotype.Service

@Service
class UtilityService {
    fun secondsToTime(secs: Int): String {
        return "$secs секунд"
    }

    fun createSubscriptionQr(id: Int, name: String) : JSONObject? {
        val response = khttp.post(
            "http://147.78.66.234:8081/payment-api/v1/subscription",
            json = mapOf(
                "id" to "sub_$id",
                "subscriptionPurpose" to "Подписка на группу $name",
                "redirectUrl" to ""
            )
        )
        return try {
            response.jsonObject
        } catch (e: Exception) {
            null
        }
    }
}