package com.reactnativekeyri

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import com.facebook.react.bridge.ActivityEventListener
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.WritableNativeArray
import com.facebook.react.bridge.WritableNativeMap
import com.keyrico.keyrisdk.Keyri
import com.keyrico.scanner.easyKeyriAuth
import com.keyrico.keyrisdk.entity.session.Session
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class KeyriNativeModule(private val reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext) {

  private val keyri by lazy(::Keyri)

  private var authWithScannerPromise: Promise? = null

  private val sessions: MutableList<Session> = mutableListOf()

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

  @ReactMethod
  fun generateAssociationKey(publicUserId: String, promise: Promise) {
    keyriCoroutineScope.launch(Dispatchers.IO) {
      try {
        val generatedKey = keyri.generateAssociationKey(publicUserId)

        withContext(Dispatchers.Main) {
          promise.resolve(generatedKey)
        }
      } catch (e: Throwable) {
        promise.reject(handleException(e))
      }
    }
  }

  @ReactMethod
  fun getUserSignature(publicUserId: String?, customSignedData: String, promise: Promise) {
    keyriCoroutineScope.launch(Dispatchers.IO) {
      try {
        val signature = keyri.getUserSignature(publicUserId, customSignedData)

        withContext(Dispatchers.Main) {
          promise.resolve(signature)
        }
      } catch (e: Throwable) {
        promise.reject(handleException(e))
      }
    }
  }

  @ReactMethod
  fun listAssociationKey(promise: Promise) {
    keyriCoroutineScope.launch(Dispatchers.IO) {
      try {
        val associationKeys = keyri.listAssociationKey()

        val resultData = WritableNativeArray()

        associationKeys.forEach(resultData::pushString)

        withContext(Dispatchers.Main) {
          promise.resolve(resultData)
        }
      } catch (e: Throwable) {
        promise.reject(handleException(e))
      }
    }
  }

  @ReactMethod
  fun getAssociationKey(publicUserId: String?, promise: Promise) {
    keyriCoroutineScope.launch(Dispatchers.IO) {
      try {
        val associationKey = keyri.getAssociationKey(publicUserId)

        withContext(Dispatchers.Main) {
          promise.resolve(associationKey)
        }
      } catch (e: Throwable) {
        promise.reject(handleException(e))
      }
    }
  }

  @ReactMethod
  fun initiateQrSession(data: ReadableMap, promise: Promise) {
    keyriCoroutineScope.launch(Dispatchers.IO) {
      try {
        val appKey: String =
          data.getString("appKey") ?: throw java.lang.IllegalStateException("You need to provide appKey")
        val sessionId: String =
          data.getString("sessionId") ?: throw java.lang.IllegalStateException("You need to provide sessionId")
        val publicUserId: String? = data.getString("publicUserId")

        val session = keyri.initiateQrSession(appKey, sessionId, publicUserId).getOrThrow()

        sessions.add(session)

        withContext(Dispatchers.Main) {
          WritableNativeMap().apply {
            putString("widgetOrigin", session.widgetOrigin)
            putString("sessionId", session.sessionId)
            putString("iPAddressMobile", session.iPAddressMobile)
            putString("iPAddressWidget", session.iPAddressWidget)

            val widgetUserAgentMap = WritableNativeMap().also {
              session.widgetUserAgent?.let { widgetUserAgent ->
                it.putBoolean("isDesktop", widgetUserAgent.isDesktop)
                it.putString("os", widgetUserAgent.os)
                it.putString("browser", widgetUserAgent.browser)
              }
            }

            val userParametersMap = WritableNativeMap().also {
              session.userParameters?.let { userParameters ->
                it.putString("custom", userParameters.custom)
              }
            }

            val riskAnalyticsMap = WritableNativeMap().also { riskAnalyticsMap ->
              session.riskAnalytics?.let { riskAnalytics ->
                val riskAttributesMap = WritableNativeMap().also {
                  riskAnalytics.riskAttributes?.let { riskAttributes ->
                    it.putBoolean("isKnownAbuser", riskAttributes.isKnownAbuser ?: false)
                    it.putBoolean("isIcloudRelay", riskAttributes.isIcloudRelay ?: false)
                    it.putBoolean("isKnownAttacker", riskAttributes.isKnownAttacker ?: false)
                    it.putBoolean("isAnonymous", riskAttributes.isAnonymous ?: false)
                    it.putBoolean("isThreat", riskAttributes.isThreat ?: false)
                    it.putBoolean("isBogon", riskAttributes.isBogon ?: false)
                    it.putBoolean("blocklists", riskAttributes.blocklists ?: false)
                    it.putBoolean("isDatacenter", riskAttributes.isDatacenter ?: false)
                    it.putBoolean("isTor", riskAttributes.isTor ?: false)
                    it.putBoolean("isProxy", riskAttributes.isProxy ?: false)
                  }
                }

                val geoDataMap = WritableNativeMap().also { geoDataMap ->
                  val mobileMap = WritableNativeMap().also {
                    riskAnalytics.geoData?.mobile?.let { mobile ->
                      it.putString("continentCode", mobile.continentCode)
                      it.putString("countryCode", mobile.countryCode)
                      it.putString("city", mobile.city)
                      it.putDouble("latitude", mobile.latitude)
                      it.putDouble("longitude", mobile.longitude)
                      it.putString("regionCode", mobile.regionCode)
                    }
                  }

                  val browserMap = WritableNativeMap().also {
                    riskAnalytics.geoData?.browser?.let { browser ->
                      it.putString("continentCode", browser.continentCode)
                      it.putString("countryCode", browser.countryCode)
                      it.putString("city", browser.city)
                      it.putDouble("latitude", browser.latitude)
                      it.putDouble("longitude", browser.longitude)
                      it.putString("regionCode", browser.regionCode)
                    }
                  }

                  geoDataMap.putMap("mobile", mobileMap)
                  geoDataMap.putMap("browser", browserMap)
                }

                riskAnalyticsMap.putMap("riskAttributes", riskAttributesMap)
                riskAnalyticsMap.putString("riskStatus", riskAnalytics.riskStatus)
                riskAnalyticsMap.putString("riskFlagString", riskAnalytics.riskFlagString)
                riskAnalyticsMap.putMap("geoData", geoDataMap)
              }
            }

            putMap("widgetUserAgent", widgetUserAgentMap)
            putMap("userParameters", userParametersMap)
            putMap("riskAnalytics", riskAnalyticsMap)
          }.let(promise::resolve)
        }
      } catch (e: Throwable) {
        promise.reject(handleException(e))
      }
    }
  }

  @ReactMethod
  fun initializeDefaultScreen(sessionId: String, payload: String, promise: Promise) {
    keyriCoroutineScope.launch(Dispatchers.IO) {
      try {
        val session = sessions.firstOrNull { it.sessionId == sessionId }
          ?: throw java.lang.IllegalStateException("Session not found")

        val fm = requireNotNull((reactContext.currentActivity as? AppCompatActivity)?.supportFragmentManager)

        val isApproved = keyri.initializeDefaultConfirmationScreen(fm, session, payload).getOrThrow()
        withContext(Dispatchers.Main) {
          promise.resolve(isApproved)
        }
      } catch (e: Throwable) {
        promise.reject(handleException(e))
      }
    }
  }

  @ReactMethod
  fun confirmSession(sessionId: String, payload: String, promise: Promise) {
    finishSession(sessionId, payload, true, promise)
  }

  @ReactMethod
  fun denySession(sessionId: String, payload: String, promise: Promise) {
    finishSession(sessionId, payload, false, promise)
  }

  @ReactMethod
  fun easyKeyriAuth(data: ReadableMap, promise: Promise) {
    reactContext.currentActivity?.let { activity ->
      authWithScannerPromise = promise

      val publicUserId: String? = data.getString("publicUserId")
      val appKey: String =
        data.getString("appKey") ?: throw java.lang.IllegalStateException("You need to provide appKey")
      val payload: String =
        data.getString("payload") ?: throw java.lang.IllegalStateException("You need to provide payload")

      easyKeyriAuth(activity, AUTH_REQUEST_CODE, appKey, payload, publicUserId)
    }
  }

  @ReactMethod
  fun processLink(data: ReadableMap, promise: Promise) {
    keyriCoroutineScope.launch(Dispatchers.IO) {
      try {
        val publicUserId: String? = data.getString("publicUserId")
        val url: String = data.getString("url") ?: throw java.lang.IllegalStateException("You need to provide url")
        val appKey: String =
          data.getString("appKey") ?: throw java.lang.IllegalStateException("You need to provide appKey")
        val payload: String =
          data.getString("payload") ?: throw java.lang.IllegalStateException("You need to provide payload")

        val sessionId = Uri.parse(url)
        val fm = requireNotNull((reactContext.currentActivity as? AppCompatActivity)?.supportFragmentManager)
        val isSuccess = keyri.easyKeyriAuth(fm, sessionId, appKey, payload, publicUserId).getOrThrow()

        withContext(Dispatchers.Main) {
          promise.resolve(isSuccess)
        }
      } catch (e: Throwable) {
        promise.reject(handleException(e))
      }
    }
  }

  private fun finishSession(sessionId: String, payload: String, isApproved: Boolean, promise: Promise) {
    keyriCoroutineScope.launch(Dispatchers.IO) {
      try {
        val session = sessions.firstOrNull { it.sessionId == sessionId }
          ?: throw java.lang.IllegalStateException("Session not found")

        val isSuccess = if (isApproved) {
          session.confirm(payload)
        } else {
          session.deny(payload)
        }.getOrThrow()

        withContext(Dispatchers.Main) {
          promise.resolve(isSuccess)
        }
      } catch (e: Throwable) {
        promise.reject(handleException(e))
      }
    }
  }

  private fun handleException(throwable: Throwable): String {
    return throwable.message ?: "Something went wrong"
  }

  companion object {
    private const val AUTH_REQUEST_CODE = 43411
  }
}
