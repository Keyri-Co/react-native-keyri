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
import com.keyrico.keyrisdk.entity.session.Session
import com.keyrico.keyrisdk.exception.DenialException
import com.keyrico.keyrisdk.sec.fraud.enums.EventType
import com.keyrico.scanner.easyKeyriAuth
import kotlinx.coroutines.*

class KeyriNativeModule(private val reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {

  private lateinit var keyri: Keyri
  private lateinit var appKey: String
  private var publicApiKey: String? = null
  private var serviceEncryptionKey: String? = null
  private var blockEmulatorDetection: Boolean = true

  private var authWithScannerPromise: Promise? = null

  private var activeSession: Session? = null

  private val activityEventListener: ActivityEventListener = object : ActivityEventListener {
    override fun onActivityResult(activity: Activity?, requestCode: Int, resultCode: Int, intent: Intent?) {
      if (requestCode == AUTH_REQUEST_CODE) {
        if (resultCode == Activity.RESULT_OK) {
          authWithScannerPromise?.resolve(Unit)
        } else {
          authWithScannerPromise?.reject(Exception("Cancelled by user"))
        }
      }
    }

    override fun onNewIntent(intent: Intent?) = Unit
  }

  init {
    reactContext.addActivityEventListener(activityEventListener)
  }

  override fun getName(): String {
    return "KeyriNativeModule"
  }

  @ReactMethod
  fun initialize(data: ReadableMap, promise: Promise) {
    try {
      appKey = data.getString("appKey") ?: throw java.lang.IllegalStateException("You need to provide appKey")
      publicApiKey = data.takeIf { it.hasKey("publicApiKey") }?.getString("publicApiKey")
      serviceEncryptionKey = data.takeIf { it.hasKey("serviceEncryptionKey") }?.getString("serviceEncryptionKey")
      blockEmulatorDetection = data.takeIf { it.hasKey("blockEmulatorDetection") }?.getBoolean("blockEmulatorDetection")
        ?: true

      keyri = Keyri(reactContext, appKey, publicApiKey, serviceEncryptionKey, blockEmulatorDetection)
    } catch (e: Throwable) {
      promise.reject(e)
    }
  }

  @ReactMethod
  fun generateAssociationKey(publicUserId: String?, promise: Promise) {
    keyriCoroutineScope(promise) {
      val generatedKey = publicUserId?.let {
        keyri.generateAssociationKey(it).getOrThrow()
      } ?: keyri.generateAssociationKey().getOrThrow()

      generatedKey
    }
  }

  @ReactMethod
  fun generateUserSignature(data: String, publicUserId: String?, promise: Promise) {
    keyriCoroutineScope(promise) {
      val signature = publicUserId?.let {
        keyri.generateUserSignature(it, data).getOrThrow()
      } ?: keyri.generateUserSignature(data = data).getOrThrow()

      signature
    }
  }

  @ReactMethod
  fun listAssociationKeys(promise: Promise) {
    keyriCoroutineScope(promise) {
      val associationKeys = keyri.listAssociationKeys().getOrThrow()
      val resultData = WritableNativeMap()

      associationKeys.forEach {
        resultData.putString(it.key, it.value)
      }

      resultData
    }
  }

  @ReactMethod
  fun listUniqueAccounts(promise: Promise) {
    keyriCoroutineScope(promise) {
      val uniqueAccounts = keyri.listUniqueAccounts().getOrThrow()
      val resultData = WritableNativeMap()

      uniqueAccounts.forEach {
        resultData.putString(it.key, it.value)
      }

      resultData
    }
  }

  @ReactMethod
  fun getAssociationKey(publicUserId: String?, promise: Promise) {
    keyriCoroutineScope(promise) {
      val associationKey = publicUserId?.let {
        keyri.getAssociationKey(it).getOrThrow()
      } ?: keyri.getAssociationKey().getOrThrow()

      associationKey
    }
  }

  @ReactMethod
  fun removeAssociationKey(publicUserId: String, promise: Promise) {
    keyriCoroutineScope(promise) {
      keyri.removeAssociationKey(publicUserId).getOrThrow()
    }
  }

  @ReactMethod
  fun sendEvent(data: ReadableMap, promise: Promise) {
    keyriCoroutineScope(promise) {
      val publicUserId = data.takeIf { it.hasKey("publicUserId") }?.getString("publicUserId") ?: "ANON"
      val eventType = data.getString("eventType")
        ?: throw java.lang.IllegalStateException("You need to provide eventType")
      val success = data.getBoolean("success")

      val type = EventType.values().first { it.type == eventType }

      val fingerprintEventResponse = keyri.sendEvent(publicUserId, type, success).getOrThrow()

      WritableNativeMap().apply {
        putString("apiCiphertextSignature", fingerprintEventResponse.apiCiphertextSignature)
        putString("publicEncryptionKey", fingerprintEventResponse.publicEncryptionKey)
        putString("ciphertext", fingerprintEventResponse.ciphertext)
        putString("iv", fingerprintEventResponse.iv)
        putString("salt", fingerprintEventResponse.salt)
      }
    }
  }

  @ReactMethod
  fun initiateQrSession(sessionId: String, publicUserId: String?, promise: Promise) {
    keyriCoroutineScope(promise) {
      val session = keyri.initiateQrSession(sessionId, publicUserId).getOrThrow()

      activeSession = session

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

              if (hasMobile || hasBrowser) {
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
                }

                riskAnalyticsMap.putMap("geoData", geoDataMap)
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
      }
    }
  }

  @ReactMethod
  fun login(publicUserId: String?, promise: Promise) {
    keyriCoroutineScope(promise) {
      val loginObject = keyri.login(publicUserId).getOrThrow()

      WritableNativeMap().apply {
        putString("timestampNonce", loginObject.timestampNonce)
        putString("signature", loginObject.signature)
        putString("publicKey", loginObject.publicKey)
        putString("userId", loginObject.userId)
      }
    }
  }

  @ReactMethod
  fun register(publicUserId: String?, promise: Promise) {
    keyriCoroutineScope(promise) {
      val registerObject = keyri.register(publicUserId).getOrThrow()

      WritableNativeMap().apply {
        putString("publicKey", registerObject.publicKey)
        putString("userId", registerObject.userId)
      }
    }
  }

  @ReactMethod
  fun initializeDefaultConfirmationScreen(payload: String, promise: Promise) {
    keyriCoroutineScope(promise) {
      val session = activeSession ?: throw java.lang.IllegalStateException("Session not found")

      val fm = requireNotNull((reactContext.currentActivity as? AppCompatActivity)?.supportFragmentManager)
      val result = keyri.initializeDefaultConfirmationScreen(fm, session, payload)

      var res = false

      if (result.isSuccess) {
        res = true
      } else {
        result.exceptionOrNull()?.takeIf { it is DenialException }?.let { res = false } ?: result.getOrThrow()
      }

      res
    }
  }

  @ReactMethod
  fun confirmSession(payload: String, trustNewBrowser: Boolean?, promise: Promise) {
    finishSession(payload, isApproved = true, trustNewBrowser = trustNewBrowser ?: false, promise)
  }

  @ReactMethod
  fun denySession(payload: String, promise: Promise) {
    finishSession(payload, isApproved = false, trustNewBrowser = false, promise)
  }

  @ReactMethod
  fun easyKeyriAuth(payload: String, publicUserId: String?, promise: Promise) {
    reactContext.currentActivity?.let { activity ->
      authWithScannerPromise = promise

      try {
        easyKeyriAuth(
          activity,
          AUTH_REQUEST_CODE,
          appKey,
          publicApiKey,
          serviceEncryptionKey,
          blockEmulatorDetection,
          payload,
          publicUserId
        )
      } catch (e: Throwable) {
        promise.reject(e)

        authWithScannerPromise = null
      }
    }
  }

  @ReactMethod
  fun processLink(data: ReadableMap, promise: Promise) {
    keyriCoroutineScope(promise) {
      val publicUserId: String? = data.takeIf { it.hasKey("publicUserId") }?.getString("publicUserId")
      val url: String = data.getString("url") ?: throw java.lang.IllegalStateException("You need to provide url")
      val payload: String = data.getString("payload")
        ?: throw java.lang.IllegalStateException("You need to provide payload")

      val uri = Uri.parse(url)
      val fm = requireNotNull((reactContext.currentActivity as? AppCompatActivity)?.supportFragmentManager)

      val result = keyri.processLink(fm, uri, payload, publicUserId)

      var res = false

      if (result.isSuccess) {
        res = true
      } else {
        result.exceptionOrNull()?.takeIf { it is DenialException }?.let { res = false } ?: result.getOrThrow()
      }

      res
    }
  }

  private fun finishSession(payload: String, isApproved: Boolean, trustNewBrowser: Boolean = false, promise: Promise) {
    keyriCoroutineScope(promise) {
      val session = activeSession ?: throw java.lang.IllegalStateException("Session not found")

      if (isApproved) {
        session.confirm(payload, reactContext, trustNewBrowser)
      } else {
        session.deny(payload, reactContext)
      }.getOrThrow()
    }
  }

  private inline fun <reified T> keyriCoroutineScope(promise: Promise, crossinline block: suspend () -> T) {
    val exceptionHandler = CoroutineExceptionHandler { _, e ->
      promise.reject(e)
    }

    CoroutineScope(SupervisorJob() + Dispatchers.IO + exceptionHandler).launch {
      val result = block()

      if (T::class.java.isAssignableFrom(Unit::class.java)) {
        promise.resolve(true)
      }

      withContext(Dispatchers.Main) {
        promise.resolve(result)
      }
    }
  }

  companion object {
    private const val AUTH_REQUEST_CODE = 43411
  }
}
