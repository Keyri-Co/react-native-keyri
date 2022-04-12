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
  private var authWithScannerPromise: Promise? = null

  private val activityEventListener: ActivityEventListener =
    object : ActivityEventListener {
      override fun onActivityResult(
        activity: Activity?,
        requestCode: Int,
        resultCode: Int,
        intent: Intent?
      ) {
        if (requestCode == KeyriSdk.AUTH_REQUEST_CODE) {
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
          rpPublicKey = publicKey,
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

        val service = initService(serviceId, serviceName, serviceLogo)

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

        val service = initService(serviceId, serviceName, serviceLogo)
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
        val aesKey: String? = data.getString("aesKey")

        keyriSdk.whitelabelAuth(sessionId, custom, aesKey)

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
      session.service.androidAppSettings?.androidPackageName?.let { putString("androidPackageName", it) }
      session.service.androidAppSettings?.sha256CertFingerprints?.let { putString("sha256CertFingerprints", it) }
      session.service.androidAppSettings?.androidGooglePlayLink?.let { putString("androidGooglePlayLink", it) }
    }

    val iosAppSettingsMap = WritableNativeMap().apply {
      session.service.iosAppSettings?.iosAppStoreLink?.let { putString("iosAppStoreLink", it) }
      session.service.iosAppSettings?.teamId?.let { putString("teamId", it) }
      session.service.iosAppSettings?.bundleId?.let { putString("bundleId", it) }
    }

    val originalDomainMap = WritableNativeMap().apply {
      session.service.originalDomain?.domainName?.let { putString("domainName", it) }
      session.service.originalDomain?.verifiedRecord?.let { putString("verifiedRecord", it) }
      session.service.originalDomain?.isDomainApproved?.let { putBoolean("isDomainApproved", it) }
    }

    val serviceMap = WritableNativeMap().apply {
      session.service.isValid?.let { putBoolean("isValid", it) }
      session.service.qrCodeType?.let { putString("qrCodeType", it) }
      putMap("androidAppSettings", androidAppSettingsMap)
      putMap("iosAppSettings", iosAppSettingsMap)
      session.service.subDomainName?.let { putString("subDomainName", it) }
      putMap("originalDomain", originalDomainMap)
      session.service.id?.let { putString("_id", it) }
      session.service.name?.let { putString("name", it) }
      session.service.logo?.let { putString("logo", it) }
      session.service.createdAt?.let { putString("createdAt", it) }
      session.service.updatedAt?.let { putString("updatedAt", it) }
      session.service.ironPlansUUID?.let { putString("ironPlansUUID", it) }
      session.service.qrLogo?.let { putString("qrLogo", it) }
    }

    val resultData = WritableNativeMap().apply {
      putMap("service", serviceMap)

      session.username?.let { putString("username", it) }

      session.isNewUser?.let { putBoolean("isNewUser", it) }
      putMap("widgetIPData", session.widgetIPData?.let { createIpData(it) })
      putMap("mobileIPData", session.mobileIPData?.let { createIpData(it) })
      session.sessionType?.let { putString("sessionType", it) }
      session.custom?.let { putString("custom", it) }
    }

    return resultData
  }

  private fun createIpData(ipData: IpData): WritableNativeMap {
    return WritableNativeMap().apply {
      ipData.ip?.let { putString("ip", it) }
      ipData.is_eu?.let { putBoolean("is_eu", it) }
      ipData.city?.let { putString("city", it) }
      ipData.region?.let { putString("region", it) }
      ipData.region_code?.let { putString("region_code", it) }
      ipData.country_name?.let { putString("country_name", it) }
      ipData.country_code?.let { putString("country_code", it) }
      ipData.continent_name?.let { putString("continent_name", it) }
      ipData.continent_code?.let { putString("continent_code", it) }
      ipData.latitude?.let { putDouble("latitude", it) }
      ipData.longitude?.let { putDouble("longitude", it) }
      ipData.postal?.let { putString("postal", it) }
      ipData.calling_code?.let { putString("calling_code", it) }
      ipData.flag?.let { putString("flag", it) }
      ipData.emoji_flag?.let { putString("emoji_flag", it) }
      ipData.emoji_unicode?.let { putString("emoji_unicode", it) }

      val asnMap = WritableNativeMap().also { map ->
        ipData.asn?.asn?.let { map.putString("asn", it) }
        ipData.asn?.name?.let { map.putString("name", it) }
        ipData.asn?.domain?.let { map.putString("domain", it) }
        ipData.asn?.route?.let { map.putString("route", it) }
        ipData.asn?.type?.let { map.putString("type", it) }
      }

      putMap("asn", asnMap)

      val languages = WritableNativeArray()

      ipData?.languages?.forEach { lang ->
        val language = WritableNativeMap()

        lang.name?.let { language.putString("name", it) }
        lang.native?.let { language.putString("native", it) }
        lang.code?.let { language.putString("code", it) }

        languages.pushMap(language)
      }

      putArray("languages", languages)

      val currencyMap = WritableNativeMap().also { map ->
        ipData?.currency?.name?.let { map.putString("name", it) }
        ipData?.currency?.code?.let { map.putString("code", it) }
        ipData?.currency?.symbol?.let { map.putString("symbol", it) }
        ipData?.currency?.native?.let { map.putString("native", it) }
        ipData?.currency?.plural?.let { map.putString("plural", it) }
      }

      putMap("currency", currencyMap)

      val timeZoneMap = WritableNativeMap().also { map ->
        ipData?.time_zone?.name?.let { map.putString("name", it) }
        ipData?.time_zone?.abbr?.let { map.putString("abbr", it) }
        ipData?.time_zone?.offset?.let { map.putString("offset", it) }
        ipData?.time_zone?.is_dst?.let { map.putBoolean("is_dst", it) }
        ipData?.time_zone?.current_time?.let { map.putString("current_time", it) }
      }

      putMap("time_zone", timeZoneMap)

      val threatMap = WritableNativeMap().also { map ->
        ipData?.threat?.is_tor?.let { map.putBoolean("is_tor", it) }
        ipData?.threat?.is_proxy?.let { map.putBoolean("is_proxy", it) }
        ipData?.threat?.is_anonymous?.let { map.putBoolean("is_anonymous", it) }
        ipData?.threat?.is_known_attacker?.let { map.putBoolean("is_known_attacker", it) }
        ipData?.threat?.is_known_abuser?.let { map.putBoolean("is_known_abuser", it) }
        ipData?.threat?.is_threat?.let { map.putBoolean("is_threat", it) }
        ipData?.threat?.is_bogon?.let { map.putBoolean("is_bogon", it) }
      }

      putMap("threat", threatMap)
      ipData?.count?.let { putString("count", it) }
      ipData?.status?.let { putInt("status", it) }
    }
  }

  private fun initService(serviceId: String, serviceName: String, serviceLogo: String): Service {
    return Service(
      null,
      null,
      null,
      null,
      null,
      null,
      serviceId,
      serviceName,
      serviceLogo,
      null,
      null,
      null,
      null
    )
  }
}
