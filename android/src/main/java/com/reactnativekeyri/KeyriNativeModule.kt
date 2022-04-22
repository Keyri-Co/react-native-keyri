package com.reactnativekeyri

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.facebook.react.bridge.ActivityEventListener
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.WritableNativeMap
import com.keyrico.keyrisdk.KeyriSdk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class KeyriNativeModule(private val reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext) {

  private lateinit var keyriSdk: KeyriSdk
  private var authWithScannerPromise: Promise? = null

  private val activityEventListener: ActivityEventListener =
    object : ActivityEventListener {
      override fun onActivityResult(
        activity: Activity?,
        requestCode: Int,
        resultCode: Int,
        intent: Intent?
      ) {
        if (requestCode == AUTH_REQUEST_CODE) {
          if (resultCode == Activity.RESULT_OK) {
            authWithScannerPromise?.resolve("Successfully authenticated")
          } else {
            authWithScannerPromise?.reject("Couldn't auth with scanner")
          }
        }
      }

      override fun onNewIntent(intent: Intent?) = Unit
    }

  private val keyriCoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

  init {
    reactContext.addActivityEventListener(activityEventListener)
  }

  override fun getName(): String {
    return "KeyriNativeModule"
  }

  // Required before other methods call
  @ReactMethod
  fun initialize(data: ReadableMap) {
    if (!::keyriSdk.isInitialized) {
      val rpPublicKey = data.takeIf { it.hasKey("rpPublicKey") }?.getString("rpPublicKey")
        ?: throw java.lang.IllegalStateException("You need to init SDK with rpPublicKey")

      val serviceDomain = data.takeIf { it.hasKey("serviceDomain") }?.getString("serviceDomain")
        ?: throw java.lang.IllegalStateException("You need to init SDK with serviceDomain")

      keyriSdk = KeyriSdk(reactContext as Context, rpPublicKey, serviceDomain)
    }
  }

  @ReactMethod
  fun generateAssociationKey(publicUserId: String, promise: Promise) {
    checkIsinit()
    keyriCoroutineScope.launch(Dispatchers.IO) {
      try {
        keyriSdk.generateAssociationKey(publicUserId)

        withContext(Dispatchers.Main) {
          promise.resolve("Key generated")
        }
      } catch (e: Throwable) {
        promise.reject(handleException(e))
      }
    }
  }

  @ReactMethod
  fun getAssociationKey(publicUserId: String, promise: Promise) {
    checkIsinit()
    keyriCoroutineScope.launch(Dispatchers.IO) {
      try {
        val associationKey: String? = keyriSdk.getAssociationKey(publicUserId)

        withContext(Dispatchers.Main) {
          promise.resolve(associationKey)
        }
      } catch (e: Throwable) {
        promise.reject(handleException(e))
      }
    }
  }

  @ReactMethod
  fun handleSessionId(sessionId: String, promise: Promise) {
    checkIsinit()
    keyriCoroutineScope.launch(Dispatchers.IO) {
      try {
        val session = keyriSdk.handleSessionId(sessionId)

        withContext(Dispatchers.Main) {
          WritableNativeMap().apply {
            putString("widgetEndPoint", session.widgetEndPoint)
            putString("widgetOrigin", session.widgetOrigin)
            putString("widgetUserAgent", session.widgetUserAgent)
            putString("action", session.action)
            putString("sessionId", session.sessionId)
            putString("sessionType", session.sessionType)
            putString("logo", session.logo)
            putString("iPAddressMobile", session.iPAddressMobile)
            putString("iPAddressWidget", session.iPAddressWidget)

            val riskAnalytics = WritableNativeMap().also { riskAnalyticsMap ->
              val riskAnalytics = session.riskAnalytics

              val geoData = WritableNativeMap().also { geoDataMap ->
                val geoData = riskAnalytics.geoData

                geoDataMap.putString("continent_code", geoData.continent_code)
                geoDataMap.putString("country_code", geoData.country_code)
                geoDataMap.putString("city", geoData.city)
                geoDataMap.putDouble("latitude", geoData.latitude)
                geoDataMap.putDouble("longitude", geoData.longitude)
                geoDataMap.putString("region_code", geoData.region_code)
              }

              riskAnalyticsMap.putMap("geoData", geoData)
              riskAnalyticsMap.putString("riskStatus", riskAnalytics.riskStatus)
            }

            putMap("riskAnalytics", riskAnalytics)
            putString("salt", session.salt)
            putString("hash", session.hash)
            session.username?.let { putString("username", it) }
          }.let(promise::resolve)
        }
      } catch (e: Throwable) {
        promise.reject(handleException(e))
      }
    }
  }

  @ReactMethod
  fun challengeSession(data: ReadableMap, promise: Promise) {
    checkIsinit()
    keyriCoroutineScope.launch(Dispatchers.IO) {
      try {
        val publicUserId: String =
          data.getString("publicUserId") ?: throw java.lang.IllegalStateException("You need to provide publicUserId")
        val sessionId: String =
          data.getString("sessionId") ?: throw java.lang.IllegalStateException("You need to provide sessionId")
        val secureCustom: String? = data.takeIf { it.hasKey("secureCustom") }?.getString("secureCustom")
        val publicCustom: String? = data.takeIf { it.hasKey("publicCustom") }?.getString("publicCustom")

        keyriSdk.challengeSession(publicUserId, sessionId, secureCustom, publicCustom)

        withContext(Dispatchers.Main) {
          promise.resolve("Authenticated")
        }
      } catch (e: Throwable) {
        promise.reject(handleException(e))
      }
    }
  }

  @ReactMethod
  fun easyKeyriAuth(data: ReadableMap, promise: Promise) {
    checkIsinit()
    reactContext.currentActivity?.let { activity ->
      authWithScannerPromise = promise

      val publicUserId: String =
        data.getString("publicUserId") ?: throw java.lang.IllegalStateException("You need to provide publicUserId")
      val secureCustom: String? = data.takeIf { it.hasKey("secureCustom") }?.getString("secureCustom")
      val publicCustom: String? = data.takeIf { it.hasKey("publicCustom") }?.getString("publicCustom")

      keyriSdk.easyKeyriAuth(activity as AppCompatActivity, AUTH_REQUEST_CODE, publicUserId, secureCustom, publicCustom)
    }
  }

  private fun handleException(throwable: Throwable): String {
    return throwable.message ?: "Something went wrong"
  }

  private fun checkIsinit() {
    if (!::keyriSdk.isInitialized) throw java.lang.IllegalStateException("You need to call initSdk(...) before")
  }

  companion object {
    private const val AUTH_REQUEST_CODE = 43411
  }
}
