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

  private lateinit var keyri : Keyri

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
            authWithScannerPromise?.reject("Cancelled by user")
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
  fun initKeyri(appKey: String) {
    keyri = Keyri(reactContext, appKey)
  }

  @ReactMethod
  fun generateAssociationKey(publicUserId: String?, promise: Promise) {
    try {
      val generatedKey = publicUserId?.let {
        keyri.generateAssociationKey(it)
      } ?: keyri.generateAssociationKey()

      promise.resolve(generatedKey)
    } catch (e: Throwable) {
      promise.reject(handleException(e))
    }
  }

  @ReactMethod
  fun getUserSignature(publicUserId: String?, customSignedData: String, promise: Promise) {
    try {
      val signature = publicUserId?.let {
        keyri.generateUserSignature(it, customSignedData)
      } ?: keyri.generateUserSignature(data = customSignedData)

      promise.resolve(signature)
    } catch (e: Throwable) {
      promise.reject(handleException(e))
    }
  }

  @ReactMethod
  fun listAssociationKey(promise: Promise) {
    try {
      val associationKeys = keyri.listAssociationKey()
      val resultData = WritableNativeMap()

      associationKeys.forEach {
        resultData.putString(it.key, it.value)
      }

      promise.resolve(resultData)
    } catch (e: Throwable) {
      promise.reject(handleException(e))
    }
  }

  @ReactMethod
  fun listUniqueAccounts(promise: Promise) {
    try {
      val associationKeys = keyri.listUniqueAccounts()
      val resultData = WritableNativeMap()

      associationKeys.forEach {
        resultData.putString(it.key, it.value)
      }

      promise.resolve(resultData)
    } catch (e: Throwable) {
      promise.reject(handleException(e))
    }
  }

  @ReactMethod
  fun getAssociationKey(publicUserId: String?, promise: Promise) {
    try {
      val associationKey = publicUserId?.let {
        keyri.getAssociationKey(it)
      } ?: keyri.getAssociationKey()

      promise.resolve(associationKey)
    } catch (e: Throwable) {
      promise.reject(handleException(e))
    }
  }

  @ReactMethod
  fun removeAssociationKey(publicUserId: String?, promise: Promise) {
    try {
      publicUserId?.let {
        keyri.removeAssociationKey(it)
      } ?: keyri.removeAssociationKey()

      promise.resolve("Success")
    } catch (e: Throwable) {
      promise.reject(handleException(e))
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
        val publicUserId: String? = data.takeIf { it.hasKey("publicUserId") }?.getString("publicUserId")

        val session = keyri.initiateQrSession(sessionId, publicUserId).getOrThrow()

        sessions.add(session)

        withContext(Dispatchers.Main) {
          WritableNativeMap().apply {
            putString("widgetOrigin", session.widgetOrigin)
            putString("sessionId", session.sessionId)
            putString("iPAddressMobile", session.iPAddressMobile)
            putString("iPAddressWidget", session.iPAddressWidget)

            val widgetUserAgentMap = WritableNativeMap().also {
              session.widgetUserAgent?.let { widgetUserAgent ->
                it.putString("os", widgetUserAgent.os)
                it.putString("browser", widgetUserAgent.browser)
              }
            }

            val userParametersMap = WritableNativeMap().also {
              session.userParameters?.let { userParameters ->
                it.putString("base64EncodedData", userParameters.base64EncodedData)
              }
            }

            if (session.riskAnalytics?.riskStatus != null && session.riskAnalytics?.riskFlagString != null) {
              val riskAnalyticsMap = WritableNativeMap().also { riskAnalyticsMap ->
                session.riskAnalytics?.let { riskAnalytics ->
                  val hasMobile = riskAnalytics.geoData?.mobile != null
                  val hasBrowser = riskAnalytics.geoData?.browser != null

                  if (hasMobile) {
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

                      geoDataMap.putMap("mobile", mobileMap)
                    }

                    if (hasBrowser) {
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

                      geoDataMap.putMap("browser", browserMap)
                    }

                    if (hasMobile || hasBrowser) {
                      riskAnalyticsMap.putMap("geoData", geoDataMap)
                    }
                  }

                  riskAnalyticsMap.putString("riskStatus", riskAnalytics.riskStatus)
                  riskAnalyticsMap.putString("riskFlagString", riskAnalytics.riskFlagString)
                }
              }

              putMap("riskAnalytics", riskAnalyticsMap)
            }

            val mobileTemplateResponse = session.mobileTemplateResponse

            if (mobileTemplateResponse != null) {
              val mobileTemplateMap = WritableNativeMap().also { mobileTemplateMap ->
                mobileTemplateMap.putString("title", mobileTemplateResponse.title)
                mobileTemplateMap.putString("message", mobileTemplateResponse.message)

                mobileTemplateResponse.widget?.let { widget ->
                  val widgetMap = WritableNativeMap().also {
                    it.putString("location", widget.location)
                    it.putString("issue", widget.issue)
                  }

                  mobileTemplateMap.putMap("widget", widgetMap)
                }

                mobileTemplateResponse.mobile?.let { mobile ->
                  val mobileMap = WritableNativeMap().also {
                    it.putString("location", mobile.location)
                    it.putString("issue", mobile.issue)
                  }

                  mobileTemplateMap.putMap("mobile", mobileMap)
                }

                mobileTemplateResponse.userAgent?.let { userAgent ->
                  val userAgentMap = WritableNativeMap().also {
                    it.putString("name", userAgent.name)
                    it.putString("issue", userAgent.issue)
                  }

                  mobileTemplateMap.putMap("userAgent", userAgentMap)
                }

                mobileTemplateResponse.flags?.let { flags ->
                  val flagsMap = WritableNativeMap().also {
                    it.putBoolean("isDatacenter", flags.isDatacenter ?: false)
                    it.putBoolean("isNewBrowser", flags.isNewBrowser ?: false)
                  }

                  mobileTemplateMap.putMap("flags", flagsMap)
                }
              }

              putMap("mobileTemplateResponse", mobileTemplateMap)
            }

            putMap("widgetUserAgent", widgetUserAgentMap)
            putMap("userParameters", userParametersMap)
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

      try {
        val publicUserId: String? = data.takeIf { it.hasKey("publicUserId") }?.getString("publicUserId")
        val appKey: String =
          data.getString("appKey") ?: throw java.lang.IllegalStateException("You need to provide appKey")
        val payload: String =
          data.getString("payload") ?: throw java.lang.IllegalStateException("You need to provide payload")

        easyKeyriAuth(activity, AUTH_REQUEST_CODE, appKey, payload, publicUserId)
      } catch (e: Throwable) {
        promise.reject(handleException(e))

        authWithScannerPromise = null
      }
    }
  }

  @ReactMethod
  fun processLink(data: ReadableMap, promise: Promise) {
    keyriCoroutineScope.launch(Dispatchers.IO) {
      try {
        val publicUserId: String? = data.takeIf { it.hasKey("publicUserId") }?.getString("publicUserId")
        val url: String = data.getString("url") ?: throw java.lang.IllegalStateException("You need to provide url")
        val appKey: String =
          data.getString("appKey") ?: throw java.lang.IllegalStateException("You need to provide appKey")
        val payload: String =
          data.getString("payload") ?: throw java.lang.IllegalStateException("You need to provide payload")

        val uri = Uri.parse(url)
        val fm = requireNotNull((reactContext.currentActivity as? AppCompatActivity)?.supportFragmentManager)
        val isSuccess = keyri.processLink(fm, uri, payload, publicUserId).getOrThrow()

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
          session.confirm(payload, reactContext)
        } else {
          session.deny(payload, reactContext)
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
