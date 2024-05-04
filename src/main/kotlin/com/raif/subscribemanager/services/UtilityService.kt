package com.raif.subscribemanager.services

import com.raif.subscribemanager.models.Payment
import com.raif.subscribemanager.models.Subscription
import org.json.JSONObject
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class UtilityService(
    private val dataLayer: DataLayer
) {
    val idVersion = "prod" // TODO не забыть поменять на prod

    private val logger = LoggerFactory.getLogger("Utility")
    fun secondsToTime(secs: Int): String {
        return "$secs секунд"
    }

    fun createSubscription(name: String, createdByUserId: Long): Subscription? {
        val sub = Subscription()
        sub.group = dataLayer.findGroup(name) ?: return null
        dataLayer.saveSub(sub)
        val response = khttp.post(
            "http://147.78.66.234:8081/payment-api/v1/subscription",
            json = mapOf(
                "id" to "sub_${idVersion}_${sub.id}",
                "subscriptionPurpose" to "Подписка на группу $name",
                "redirectUrl" to ""
            )
        )
        logger.info("createSub {}", response.text)
        val res = try {
            response.jsonObject
        } catch (e: Exception) {
            logger.info(e.message)
            null
        }
        if (res == null) {
            dataLayer.deleteSub(sub.id)
            return null
        }
        sub.qrId = res.getJSONObject("qr").getString("id")
        sub.qrUrl = res.getJSONObject("qr").getString("url")
        sub.createdByUserId = createdByUserId
        println("qrId: ${sub.qrId}")
        println("qrUrl: ${sub.qrUrl}")
        dataLayer.saveSub(sub)
        return sub
    }

    fun getSubQr(id: Int): JSONObject? {
        val response = khttp.get(
            "http://147.78.66.234:8081/payment-api/v1/subscription/sub_${idVersion}_$id"
        )
        return try {
            response.jsonObject
        } catch (e: Exception) {
            null
        }
    }

    fun createPaySubQr(subId: Int, amount: Double): Payment? {
        val pay = dataLayer.createPay(subId)
        val response = khttp.post(
            "http://147.78.66.234:8081/payment-api/v1/subscription/pay/sub_${idVersion}_$subId",
            json = mapOf(
                "additionalInfo" to "not blank",
                "amount" to amount,
                "currency" to "RUB",
                "order" to "pay_${idVersion}_${pay.id}"
            )
        )
        if (response.statusCode != 200) {
            return null
        }
        pay.status = response.jsonObject.getString("paymentStatus")
        return pay
    }

    fun getPaySubQr(payId: Int): JSONObject? {
        logger.info("getPay {}", payId)
        val pay = dataLayer.getPay(payId) ?: return null
        val subId = pay.subId
        val url =
            "http://147.78.66.234:8081/payment-api/v1/subscription/pay/sub_${idVersion}_$subId/orders/pay_${idVersion}_$payId"
        logger.info("getPay {}", url)
        val response = khttp.get(
            url
        )
        logger.info("getPay {}", response.text)

        if (response.statusCode != 200) {
            return null
        }
        try {
            val res = response.jsonObject
            return res

        } catch (e: Exception) {
            return null
        }
    }


}