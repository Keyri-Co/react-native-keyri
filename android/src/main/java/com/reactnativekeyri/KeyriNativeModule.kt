package com.reactnativekeyri

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.keyrico.keyrisdk.KeyriConfig
import com.keyrico.keyrisdk.KeyriSdk
import com.keyrico.keyrisdk.entity.PublicAccount
import com.keyrico.keyrisdk.entity.session.Session
import com.keyrico.keyrisdk.entity.session.service.Service
import com.keyrico.keyrisdk.entity.session.service.AndroidAppSettings
import com.keyrico.keyrisdk.entity.session.service.IosAppSettings
import com.keyrico.keyrisdk.entity.session.service.OriginalDomain
import com.keyrico.keyrisdk.entity.session.ipdata.Asn
import com.keyrico.keyrisdk.entity.session.ipdata.IpLanguage
import com.keyrico.keyrisdk.entity.session.ipdata.IpCurrency
import com.keyrico.keyrisdk.entity.session.ipdata.IpTimeZone
import com.keyrico.keyrisdk.entity.session.ipdata.Threat
import com.keyrico.keyrisdk.entity.session.ipdata.IpData
import com.keyrico.keyrisdk.exception.KeyriSdkException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ActivityEventListener
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.WritableNativeMap
import com.facebook.react.bridge.WritableNativeArray

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
        val service = data.getMap("service")?.let { mapToService(it) } ?: throw java.lang.IllegalStateException("Must contain service")
        val sessionId: String = data.getString("sessionId") ?: throw java.lang.IllegalStateException("Must contain sessionId")
        val username: String = data.takeIf { it.hasKey("username") }?.getString("username") ?: ""
        val custom: String? = data.takeIf { it.hasKey("custom") }?.getString("custom")

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
        val service = data.getMap("service")?.let { mapToService(it) } ?: throw java.lang.IllegalStateException("Must contain service")
        val sessionId: String = data.getString("sessionId") ?: throw java.lang.IllegalStateException("Must contain sessionId")
        val username: String = data.takeIf { it.hasKey("username") }?.getString("username") ?: ""
        val custom: String? = data.takeIf { it.hasKey("custom") }?.getString("custom")

        val account = PublicAccount(username ?: "", custom)

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
    }

    val resultData = WritableNativeMap().apply {
      putMap("service", serviceMap)

      session.username?.let { putString("username", it) }

      putBoolean("isNewUser", session.isNewUser)
      putMap("widgetIPData", session.widgetIPData?.let { createIpData(it) })
      putMap("mobileIPData", session.mobileIPData?.let { createIpData(it) })
      putString("sessionType", session.sessionType)
      putString("custom", session.custom)
    }

    return resultData
  }

  private fun createIpData(ipData: IpData): WritableNativeMap {
    return WritableNativeMap().apply {
      putString("ip", ipData.ip)
      putBoolean("isEu", ipData.isEu)
      putString("city", ipData.city)
      putString("region", ipData.region)
      putString("regionCode", ipData.regionCode)
      putString("countryName", ipData.countryName)
      putString("countryCode", ipData.countryCode)
      putString("continentName", ipData.continentName)
      putString("continentCode", ipData.continentCode)
      putDouble("latitude", ipData.latitude)
      putDouble("longitude", ipData.longitude)
      putString("postal", ipData.postal)
      putString("callingCode", ipData.callingCode)
      putString("flag", ipData.flag)
      putString("emojiFlag", ipData.emojiFlag)
      putString("emojiUnicode", ipData.emojiUnicode)

      val asnMap = WritableNativeMap().also {
        it.putString("asn", ipData.asn.asn)
        it.putString("name", ipData.asn.name)
        it.putString("domain", ipData.asn.domain)
        it.putString("route", ipData.asn.route)
        it.putString("type", ipData.asn.type)
      }

      putMap("asn", asnMap)

      val languages = WritableNativeArray()

      ipData.languages.forEach { lang ->
        val language = WritableNativeMap()

        language.putString("name", lang.name)
        language.putString("native", lang.native)
        language.putString("code", lang.code)

        languages.pushMap(language)
      }

      putArray("languages", languages)

      val currencyMap = WritableNativeMap().also {
        it.putString("name", ipData.currency.name)
        it.putString("code", ipData.currency.code)
        it.putString("symbol", ipData.currency.symbol)
        it.putString("native", ipData.currency.native)
        it.putString("plural", ipData.currency.plural)
      }

      putMap("currency", currencyMap)

      val timeZoneMap = WritableNativeMap().also {
        it.putString("name", ipData.timeZone.name)
        it.putString("abbr", ipData.timeZone.abbr)
        it.putString("offset", ipData.timeZone.offset)
        it.putBoolean("isDst", ipData.timeZone.isDst)
        it.putString("currentTime", ipData.timeZone.currentTime)
      }

      putMap("timeZone", timeZoneMap)

      val threatMap = WritableNativeMap().also {
        it.putBoolean("isTor", ipData.threat.isTor)
        it.putBoolean("isProxy", ipData.threat.isProxy)
        it.putBoolean("isAnonymous", ipData.threat.isAnonymous)
        it.putBoolean("isKnownAttacker", ipData.threat.isKnownAttacker)
        it.putBoolean("isKnownAbuser", ipData.threat.isKnownAbuser)
        it.putBoolean("isThreat", ipData.threat.isThreat)
        it.putBoolean("isBogon", ipData.threat.isBogon)
      }

      putMap("threat", threatMap)
      putString("count", ipData.count)
      putInt("status", ipData.status)
    }
  }

  private fun mapToService(data: ReadableMap): Service? {
    val androidAppSettingsMap = data.getMap("androidAppSettings")
    val iosAppSettingsMap = data.getMap("iosAppSettings")
    val originalDomainMap = data.getMap("originalDomain")

    val androidAppSettings = AndroidAppSettings(
      androidAppSettingsMap?.takeIf { it.hasKey("androidPackageName") }?.getString("androidPackageName"),
      androidAppSettingsMap?.takeIf { it.hasKey("sha256CertFingerprints") }?.getString("sha256CertFingerprints"),
      androidAppSettingsMap?.takeIf { it.hasKey("androidGooglePlayLink") }?.getString("androidGooglePlayLink")
    )

    val iosAppSettings = IosAppSettings(
      iosAppSettingsMap?.takeIf { it.hasKey("iosAppId") }?.getString("iosAppId"),
      iosAppSettingsMap?.takeIf { it.hasKey("iosAppStoreLink") }?.getString("iosAppStoreLink"),
      iosAppSettingsMap?.takeIf { it.hasKey("teamId") }?.getString("teamId"),
      iosAppSettingsMap?.takeIf { it.hasKey("bundleId") }?.getString("bundleId")
    )

    val originalDomain = OriginalDomain(
      originalDomainMap?.takeIf { it.hasKey("domainName") }?.getString("domainName"),
      originalDomainMap?.takeIf { it.hasKey("verifiedRecord") }?.getString("verifiedRecord"),
      originalDomainMap?.takeIf { it.hasKey("isDomainApproved") }?.getBoolean("isDomainApproved") ?: false
    )

    return Service(
      data.getBoolean("isValid"),
      data.getString("qrCodeType") ?: "",
      androidAppSettings,
      iosAppSettings,
      data.getString("subDomainName"),
      originalDomain,
      data.getString("serviceId") ?: "",
      data.getString("name") ?: "",
      data.getString("logo") ?: "",
      data.getString("key") ?: "",
      data.getString("createdAt") ?: "",
      data.getString("updatedAt") ?: "",
      data.getInt("version"),
      data.takeIf { it.hasKey("ironPlansUUID") }?.getString("ironPlansUUID"),
      data.takeIf { it.hasKey("qrLogo") }?.getString("qrLogo")
    )
  }

  private fun mapToIPData(data: ReadableMap): IpData {
    val asnMap = data.getMap("asn")
    val currencyMap = data.getMap("currency")
    val timeZoneMap = data.getMap("timeZone")
    val threatMap = data.getMap("threat")

    val asn = Asn(
      asnMap?.getString("asn") ?: "",
      asnMap?.getString("name") ?: "",
      asnMap?.getString("domain") ?: "",
      asnMap?.getString("route") ?: "",
      asnMap?.getString("type") ?: ""
    )

    val currency = IpCurrency(
      currencyMap?.getString("name") ?: "",
      currencyMap?.getString("code") ?: "",
      currencyMap?.getString("symbol") ?: "",
      currencyMap?.getString("native") ?: "",
      currencyMap?.getString("plural") ?: ""
    )

    val timeZone = IpTimeZone(
      timeZoneMap?.getString("name") ?: "",
      timeZoneMap?.getString("abbr") ?: "",
      timeZoneMap?.getString("offset") ?: "",
      timeZoneMap?.getBoolean("isDst") ?: false,
      timeZoneMap?.getString("currentTime") ?: ""
    )

    val threat = Threat(
      threatMap?.getBoolean("isTor") ?: false,
      threatMap?.getBoolean("isProxy") ?: false,
      threatMap?.getBoolean("isAnonymous") ?: false,
      threatMap?.getBoolean("isKnownAttacker") ?: false,
      threatMap?.getBoolean("isKnownAbuser") ?: false,
      threatMap?.getBoolean("isThreat") ?: false,
      threatMap?.getBoolean("isBogon") ?: false
    )

    return IpData(
      data.getString("ip") ?: "",
      data.getBoolean("isEu"),
      data.getString("city") ?: "",
      data.getString("region") ?: "",
      data.getString("regionCode") ?: "",
      data.getString("countryName") ?: "",
      data.getString("countryCode") ?: "",
      data.getString("continentName") ?: "",
      data.getString("continentCode") ?: "",
      data.getDouble("latitude"),
      data.getDouble("longitude"),
      data.getString("postal") ?: "",
      data.getString("callingCode") ?: "",
      data.getString("flag") ?: "",
      data.getString("emojiFlag") ?: "",
      data.getString("emojiUnicode") ?: "",
      asn,
      emptyList<IpLanguage>(),
      currency,
      timeZone,
      threat,
      data.getString("count") ?: "",
      data.getInt("status")
    )
  }
}
