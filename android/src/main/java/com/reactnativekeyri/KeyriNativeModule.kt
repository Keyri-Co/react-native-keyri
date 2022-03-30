package com.reactnativekeyri

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.keyrico.keyrisdk.KeyriConfig
import com.keyrico.keyrisdk.KeyriSdk
import com.keyrico.keyrisdk.entity.PublicAccount
import com.keyrico.keyrisdk.entity.Service
import com.keyrico.keyrisdk.entity.Session
import com.keyrico.keyrisdk.services.api.AuthMobileResponse
import com.keyrico.keyrisdk.exception.KeyriSdkException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ActivityEventListener
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReadableType
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.ReadableNativeArray
import com.facebook.react.bridge.WritableMap
import com.facebook.react.bridge.WritableArray
import com.facebook.react.bridge.WritableNativeMap
import com.facebook.react.bridge.WritableNativeArray
import java.lang.Exception

class KeyriNativeModule(private val reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext) {

  private lateinit var keyriSdk: KeyriSdk
  private lateinit var authWithScannerPromise: Promise

  private val activityEventListener: ActivityEventListener =
    object : ActivityEventListener {
      override fun onActivityResult(
        activity: Activity?,
        requestCode: Int,
        resultCode: Int,
        intent: Intent?
      ) {
        if (requestCode == KeyriSdk.AUTH_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
          authWithScannerPromise.resolve("Successfully authenticated")
        } else {
          authWithScannerPromise.reject("Couldn't auth with scanner")
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
      val appKey: String =
        data.getString("appKey") ?: throw java.lang.IllegalStateException("You need to init SDK with appKey")
      val publicKey: String =
        data.getString("publicKey") ?: throw java.lang.IllegalStateException("You need to init SDK with publicKey")
      val callbackUrl: String =
        data.getString("callbackUrl") ?: throw java.lang.IllegalStateException("You need to init SDK with callbackUrl")
      val allowMultipleAccounts =
        data.takeIf { it.hasKey("allowMultipleAccounts") }?.getBoolean("allowMultipleAccounts") ?: false

      keyriSdk = KeyriSdk(
        reactContext as Context,
        KeyriConfig(
          appKey = appKey,
          publicKey = publicKey,
          callbackUrl = callbackUrl,
          allowMultipleAccounts = allowMultipleAccounts
        )
      )
    }
  }

  @ReactMethod
  fun handleSessionId(sessionId: String, promise: Promise) {
    checkIsinit()
    keyriCoroutineScope.launch(Dispatchers.IO) {
      try {
        val session = keyriSdk.handleSessionId(sessionId)

        withContext(Dispatchers.Main) {
          promise.resolve(createResultData(session))
        }
      } catch (e: Throwable) {
        promise.reject(handleException(e))
      }
    }
  }

  @ReactMethod
  fun sessionSignup(data: ReadableMap, promise: Promise) {
    checkIsinit()
    keyriCoroutineScope.launch(Dispatchers.IO) {
      try {
        val username: String = data.getString("username") ?: ""
        val sessionId: String = data.getString("sessionId") ?: ""
        val serviceId: String = data.getString("serviceId") ?: ""
        val serviceName: String = data.getString("serviceName") ?: ""
        val serviceLogo: String = data.getString("serviceLogo") ?: ""
        val custom: String? = data.takeIf { it.hasKey("custom") }?.getString("custom")

        val service = Service(serviceId, serviceName, serviceLogo)

        keyriSdk.sessionSignup(username, sessionId, service, custom)

        withContext(Dispatchers.Main) {
          promise.resolve("Signed up")
        }
      } catch (e: Throwable) {
        promise.reject(handleException(e))
      }
    }
  }

  @ReactMethod
  fun sessionLogin(data: ReadableMap, promise: Promise) {
    checkIsinit()
    keyriCoroutineScope.launch(Dispatchers.IO) {
      try {
        val publicAccountUsername: String = data.getString("publicAccountUsername") ?: ""
        val sessionId: String = data.getString("sessionId") ?: ""
        val serviceId: String = data.getString("serviceId") ?: ""
        val serviceName: String = data.getString("serviceName") ?: ""
        val serviceLogo: String = data.getString("serviceLogo") ?: ""
        val publicAccountCustom = data.takeIf { it.hasKey("publicAccountCustom") }?.getString("publicAccountCustom")
        val custom = data.takeIf { it.hasKey("custom") }?.getString("custom")

        val service = Service(serviceId, serviceName, serviceLogo)
        val account = PublicAccount(publicAccountUsername, publicAccountCustom)

        keyriSdk.sessionLogin(account, sessionId, service, custom)

        withContext(Dispatchers.Main) {
          promise.resolve("Signed in")
        }
      } catch (e: Throwable) {
        promise.reject(handleException(e))
      }
    }
  }

  @ReactMethod
  fun directSignup(username: String, extendedHeaders: ReadableMap, custom: String?, promise: Promise) {
    checkIsinit()
    keyriCoroutineScope.launch(Dispatchers.IO) {
      try {
        val headers = extendedHeaders.toHashMap().map { it.key to it.value.toString() }.toMap()
        val authMobileResponse = keyriSdk.directSignup(username, custom, headers)

        withContext(Dispatchers.Main) {
          val resultData = WritableNativeMap()

          resultData.putString("userId", authMobileResponse.user.userId)
          resultData.putString("userName", authMobileResponse.user.name)
          resultData.putString("token", authMobileResponse.token)
          resultData.putString("refreshToken", authMobileResponse.refreshToken)

          promise.resolve(resultData)
        }
      } catch (e: Throwable) {
        promise.reject(handleException(e))
      }
    }
  }

  @ReactMethod
  fun directLogin(
    publicAccountUsername: String,
    extendedHeaders: ReadableMap,
    publicAccountCustom: String?,
    promise: Promise
  ) {
    checkIsinit()
    keyriCoroutineScope.launch(Dispatchers.IO) {
      try {
        val account = PublicAccount(publicAccountUsername, publicAccountCustom)
        val headers = extendedHeaders.toHashMap().map { it.key to it.value.toString() }.toMap()
        val authMobileResponse = keyriSdk.directLogin(account, headers)

        withContext(Dispatchers.Main) {
          val resultData = WritableNativeMap()

          resultData.putString("userId", authMobileResponse.user.userId)
          resultData.putString("userName", authMobileResponse.user.name)
          resultData.putString("token", authMobileResponse.token)
          resultData.putString("refreshToken", authMobileResponse.refreshToken)

          promise.resolve(resultData)
        }
      } catch (e: Throwable) {
        promise.reject(handleException(e))
      }
    }
  }

  @ReactMethod
  fun getAccounts(promise: Promise) {
    checkIsinit()
    keyriCoroutineScope.launch(Dispatchers.IO) {
      try {
        val accounts = keyriSdk.getAccounts()

        withContext(Dispatchers.Main) {
          val resultData = WritableNativeArray()

          accounts.forEach { account ->
            val accountMap = WritableNativeMap()

            accountMap.putString("username", account.username)
            accountMap.putString("custom", account.custom)

            resultData.pushMap(accountMap)
          }

          promise.resolve(resultData)
        }
      } catch (e: Throwable) {
        promise.reject(handleException(e))
      }
    }
  }

  @ReactMethod
  fun removeAccount(publicAccountUsername: String, publicAccountCustom: String?, promise: Promise) {
    checkIsinit()
    keyriCoroutineScope.launch(Dispatchers.IO) {
      try {
        val account = PublicAccount(publicAccountUsername, publicAccountCustom)

        keyriSdk.removeAccount(account)

        withContext(Dispatchers.Main) {
          promise.resolve("Account removed")
        }
      } catch (e: Throwable) {
        promise.reject(handleException(e))
      }
    }
  }

  @ReactMethod
  fun easyKeyriAuth(customArg: String? = "CUSTOM", promise: Promise) {
    checkIsinit()
    reactContext.getCurrentActivity()?.let { activity ->
      authWithScannerPromise = promise
      keyriSdk.easyKeyriAuth(activity, customArg)
    }
  }

  @ReactMethod
  fun whitelabelAuth(data: ReadableMap, promise: Promise) {
    checkIsinit()
    keyriCoroutineScope.launch(Dispatchers.IO) {
      try {
        val sessionId: String = data.getString("sessionId") ?: ""
        val custom: String = data.getString("custom") ?: ""

        keyriSdk.whitelabelAuth(sessionId, custom)

        withContext(Dispatchers.Main) {
          promise.resolve("Custom was sent")
        }
      } catch (e: Throwable) {
        promise.reject(handleException(e))
      }
    }
  }

  private fun handleException(throwable: Throwable): String {
    return if (throwable is KeyriSdkException) {
      reactContext.getString(throwable.errorMessage)
    } else {
      throwable.message ?: "Something went wrong"
    }
  }

  private fun checkIsinit() {
    if (!::keyriSdk.isInitialized) throw java.lang.IllegalStateException("You need to call initSdk(...) before")
  }

  private fun createResultData(session: Session): WritableNativeMap {
    val androidAppSettingsMap = WritableNativeMap().apply {
      putString("androidPackageName", session.service.androidAppSettings.androidPackageName)
      putString("sha256CertFingerprints", session.service.androidAppSettings.sha256CertFingerprints)
      putString("androidGooglePlayLink", session.service.androidAppSettings.androidGooglePlayLink)
    }

    val iosAppSettingsMap = WritableNativeMap().apply {
      putString("iosAppId", session.service.iosAppSettings.iosAppId)
      putString("iosAppStoreLink", session.service.iosAppSettings.iosAppStoreLink)
      putString("teamId", session.service.iosAppSettings.teamId)
      putString("bundleId", session.service.iosAppSettings.bundleId)
    }

    val originalDomainMap = WritableNativeMap().apply {
      putString("domainName", session.service.originalDomain.domainName)
      putString("verifiedRecord", session.service.originalDomain.verifiedRecord)
      putBoolean("isDomainApproved", session.service.originalDomain.isDomainApproved)
    }

    val serviceMap = WritableNativeMap().apply {
      putBoolean("isValid", session.service.isValid)
      putString("qrCodeType", session.service.qrCodeType)
      putMap("androidAppSettings", androidAppSettingsMap)
      putMap("iosAppSettings", iosAppSettingsMap)
      putString("subDomainName", session.service.subDomainName)
      putMap("originalDomain", originalDomainMap)
      putString("serviceId", session.service.serviceId)
      putString("name", session.service.name)
      putString("logo", session.service.logo)
      putString("key", session.service.key)
      putString("createdAt", session.service.createdAt)
      putString("updatedAt", session.service.updatedAt)
      putInt("version", session.service.version)
      putString("ironPlansUUID", session.service.ironPlansUUID)
      putString("qrLogo", session.service.qrLogo)
      putString("sessionType", session.service.sessionType)
      putString("custom", session.service.custom)
    }

    val widgetIPDataMap = WritableNativeMap().apply {
      putString("ip", session.widgetIPData.ip)
      putBoolean("isEu", session.widgetIPData.isEu)
      putString("city", session.widgetIPData.city)
      putString("region", session.widgetIPData.region)
      putString("regionCode", session.widgetIPData.regionCode)
      putString("countryName", session.widgetIPData.countryName)
      putString("countryCode", session.widgetIPData.countryCode)
      putString("continentName", session.widgetIPData.continentName)
      putString("continentCode", session.widgetIPData.continentCode)
      putDouble("latitude", session.widgetIPData.latitude)
      putDouble("longitude", session.widgetIPData.longitude)
      putString("postal", session.widgetIPData.postal)
      putString("callingCode", session.widgetIPData.callingCode)
      putString("flag", session.widgetIPData.flag)
      putString("emojiFlag", session.widgetIPData.emojiFlag)
      putString("emojiUnicode", session.widgetIPData.emojiUnicode)

      val asnMap = WritableNativeMap().also {
        it.putString("asn", session.widgetIPData.asn.asn)
        it.putString("name", session.widgetIPData.asn.name)
        it.putString("domain", session.widgetIPData.asn.domain)
        it.putString("route", session.widgetIPData.asn.route)
        it.putString("type", session.widgetIPData.asn.type)
      }

      putMap("asn", asnMap)

      val languages = WritableNativeArray()

      session.widgetIPData.languages.forEach { lang ->
        val language = WritableNativeMap()

        language.putString("name", lang.name)
        language.putString("native", lang.native)
        language.putString("code", lang.code)

        languages.pushMap(language)
      }

      putArray("languages", languages)

      val currencyMap = WritableNativeMap().also {
        it.putString("name", session.widgetIPData.currency.name)
        it.putString("code", session.widgetIPData.currency.code)
        it.putString("symbol", session.widgetIPData.currency.symbol)
        it.putString("native", session.widgetIPData.currency.native)
        it.putString("plural", session.widgetIPData.currency.plural)
      }

      putMap("currency", currencyMap)

      val timeZoneMap = WritableNativeMap().also {
        it.putString("name", session.widgetIPData.timeZone.name)
        it.putString("abbr", session.widgetIPData.timeZone.abbr)
        it.putString("offset", session.widgetIPData.timeZone.offset)
        it.putBoolean("isDst", session.widgetIPData.timeZone.isDst)
        it.putString("currentTime", session.widgetIPData.timeZone.currentTime)
      }

      putMap("timeZone", timeZoneMap)

      val threatMap = WritableNativeMap().also {
        it.putBoolean("isTor", session.widgetIPData.threat.isTor)
        it.putBoolean("isProxy", session.widgetIPData.threat.isProxy)
        it.putBoolean("isAnonymous", session.widgetIPData.threat.isAnonymous)
        it.putBoolean("isKnownAttacker", session.widgetIPData.threat.isKnownAttacker)
        it.putBoolean("isKnownAbuser", session.widgetIPData.threat.isKnownAbuser)
        it.putBoolean("isThreat", session.widgetIPData.threat.isThreat)
        it.putBoolean("isBogon", session.widgetIPData.threat.isBogon)
      }

      putMap("threat", threatMap)
      putString("count", session.widgetIPData.count)
      putInt("status", session.widgetIPData.status)
    }

    val mobileIPDataMap = WritableNativeMap().apply {
      putString("ip", session.mobileIPData.ip)
      putBoolean("isEu", session.mobileIPData.isEu)
      putString("city", session.mobileIPData.city)
      putString("region", session.mobileIPData.region)
      putString("regionCode", session.mobileIPData.regionCode)
      putString("countryName", session.mobileIPData.countryName)
      putString("countryCode", session.mobileIPData.countryCode)
      putString("continentName", session.mobileIPData.continentName)
      putString("continentCode", session.mobileIPData.continentCode)
      putDouble("latitude", session.mobileIPData.latitude)
      putDouble("longitude", session.mobileIPData.longitude)
      putString("postal", session.mobileIPData.postal)
      putString("callingCode", session.mobileIPData.callingCode)
      putString("flag", session.mobileIPData.flag)
      putString("emojiFlag", session.mobileIPData.emojiFlag)
      putString("emojiUnicode", session.mobileIPData.emojiUnicode)

      val asnMap = WritableNativeMap().also {
        it.putString("asn", session.mobileIPData.asn.asn)
        it.putString("name", session.mobileIPData.asn.name)
        it.putString("domain", session.mobileIPData.asn.domain)
        it.putString("route", session.mobileIPData.asn.route)
        it.putString("type", session.mobileIPData.asn.type)
      }

      putMap("asn", asnMap)

      val languages = WritableNativeArray()

      session.mobileIPData.languages.forEach { lang ->
        val language = WritableNativeMap()

        language.putString("name", lang.name)
        language.putString("native", lang.native)
        language.putString("code", lang.code)

        languages.pushMap(language)
      }

      putArray("languages", languages)

      val currencyMap = WritableNativeMap().also {
        it.putString("name", session.mobileIPData.currency.name)
        it.putString("code", session.mobileIPData.currency.code)
        it.putString("symbol", session.mobileIPData.currency.symbol)
        it.putString("native", session.mobileIPData.currency.native)
        it.putString("plural", session.mobileIPData.currency.plural)
      }

      putMap("currency", currencyMap)

      val timeZoneMap = WritableNativeMap().also {
        it.putString("name", session.mobileIPData.timeZone.name)
        it.putString("abbr", session.mobileIPData.timeZone.abbr)
        it.putString("offset", session.mobileIPData.timeZone.offset)
        it.putBoolean("isDst", session.mobileIPData.timeZone.isDst)
        it.putString("currentTime", session.mobileIPData.timeZone.currentTime)
      }

      putMap("timeZone", timeZoneMap)

      val threatMap = WritableNativeMap().also {
        it.putBoolean("isTor", session.mobileIPData.threat.isTor)
        it.putBoolean("isProxy", session.mobileIPData.threat.isProxy)
        it.putBoolean("isAnonymous", session.mobileIPData.threat.isAnonymous)
        it.putBoolean("isKnownAttacker", session.mobileIPData.threat.isKnownAttacker)
        it.putBoolean("isKnownAbuser", session.mobileIPData.threat.isKnownAbuser)
        it.putBoolean("isThreat", session.mobileIPData.threat.isThreat)
        it.putBoolean("isBogon", session.mobileIPData.threat.isBogon)
      }

      putMap("threat", threatMap)
      putString("count", session.mobileIPData.count)
      putInt("status", session.mobileIPData.status)
    }

    val resultData = WritableNativeMap().apply {
      putMap("service", serviceMap)
      putString("username", session.username)
      putBoolean("isNewUser", session.isNewUser)
      putMap("widgetIPData", widgetIPDataMap)
      putMap("mobileIPData", mobileIPDataMap)
    }

    return resultData
  }
}
