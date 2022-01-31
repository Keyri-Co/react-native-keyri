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
import com.facebook.react.bridge.Callback
import com.facebook.react.bridge.ReadableArray
import com.facebook.react.bridge.ReadableType
import com.facebook.react.bridge.WritableMap
import com.facebook.react.bridge.WritableArray
import com.facebook.react.bridge.ReadableMap
import java.lang.Exception

class KeyriNativeModule(private val reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext) {

  private var onAuthSuccess: Callback? = null
  private var errorCallback: Callback? = null

  private val activityEventListener: ActivityEventListener =
    object : ActivityEventListener {
      override fun onActivityResult(
        activity: Activity?,
        requestCode: Int,
        resultCode: Int,
        intent: Intent
      ) {
        if (requestCode == KeyriSdk.AUTH_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
          onAuthSuccess?.invoke()
        }
      }

      override fun onNewIntent(intent: Intent) = Unit
    }

  init {
    reactContext.addActivityEventListener(activityEventListener)
  }

  private val keyriSdk = KeyriSdk(
    reactContext as Context,
    KeyriConfig(
      appKey = "raB7SFWt27VoKqkPhaUrmWAsCJIO8Moj",
      publicKey = "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAE56eKjQNfIbfWYCBQLCF2yV6VySbHMzuc07JYCOS6juySvUWE/ubYvw9pJGAgQfmNr2n4LAQggoapHgfHkTNqbg==",
      callbackUrl = "http://18.234.222.59:5000/users/session-mobile",
      allowMultipleAccounts = true
    )
  )

  override fun getName(): String {
    return "KeyriNativeModule"
  }

  private val exceptionHandler = CoroutineExceptionHandler { _, exception ->
    errorCallback?.invoke(exception.message)
  }

  private val keyriCoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate + exceptionHandler)

  @ReactMethod
  fun listenActivityResult(onAuthSuccess: Callback) {
    this.onAuthSuccess = onAuthSuccess
  }

  @ReactMethod
  fun listenErrors(errorCallback: Callback) {
    this.errorCallback = errorCallback
  }

  @ReactMethod
  fun onReadSessionId(
    sessionId: String,
    successCallback: Callback
  ) {
    keyriCoroutineScope.launch(Dispatchers.IO) {
      val session = keyriSdk.onReadSessionId(sessionId)

      withContext(Dispatchers.Main) {
        successCallback.invoke(
          session.service.serviceId,
          session.service.name,
          session.service.logo,
          session.username,
          session.isNewUser
        )
      }
    }
  }

  @ReactMethod
  fun signup(
    username: String,
    sessionId: String,
    serviceId: String,
    serviceName: String,
    serviceLogo: String,
    custom: String?,
    callback: Callback
  ) {
    keyriCoroutineScope.launch(Dispatchers.IO) {
      val service = Service(serviceId, serviceName, serviceLogo)

      keyriSdk.signup(username, sessionId, service, custom)

      withContext(Dispatchers.Main) {
        callback.invoke()
      }
    }
  }

  @ReactMethod
  fun login(
    publicAccountUsername: String,
    publicAccountCustom: String?,
    sessionId: String,
    service: Service,
    custom: String?,
    callback: Callback
  ) {
    keyriCoroutineScope.launch(Dispatchers.IO) {
      val account = PublicAccount(publicAccountUsername, publicAccountCustom)

      keyriSdk.login(account, sessionId, service, custom)

      withContext(Dispatchers.Main) {
        callback.invoke()
      }
    }
  }

  @ReactMethod
  fun mobileSignup(
    username: String,
    custom: String?,
    extendedHeaders: ReadableMap,
    callback: Callback
  ) {
    keyriCoroutineScope.launch(Dispatchers.IO) {
      val headers = extendedHeaders.toHashMap().map { it.key to it.value.toString() }.toMap()
      val authMobileResponse = keyriSdk.mobileSignup(username, custom, headers)

      withContext(Dispatchers.Main) {
        callback.invoke(
          authMobileResponse.user.userId,
          authMobileResponse.user.name,
          authMobileResponse.token,
          authMobileResponse.refreshToken
        )
      }
    }
  }

  @ReactMethod
  fun mobileLogin(
    publicAccountUsername: String,
    publicAccountCustom: String?,
    extendedHeaders: ReadableMap,
    callback: Callback
  ) {
    keyriCoroutineScope.launch(Dispatchers.IO) {
      val account = PublicAccount(publicAccountUsername, publicAccountCustom)
      val headers = extendedHeaders.toHashMap().map { it.key to it.value.toString() }.toMap()
      val authMobileResponse = keyriSdk.mobileLogin(account, headers)

      withContext(Dispatchers.Main) {
        callback.invoke(
          authMobileResponse.user.userId,
          authMobileResponse.user.name,
          authMobileResponse.token,
          authMobileResponse.refreshToken
        )
      }
    }
  }

  @ReactMethod
  fun accounts(callback: (WritableMap?) -> Unit) {
    keyriCoroutineScope.launch(Dispatchers.IO) {
      val accounts = keyriSdk.accounts()
      val accountsMap = toWritableMap(accounts.map { it.username to it.custom }.toMap())

      withContext(Dispatchers.Main) {
        callback(accountsMap)
      }
    }
  }

  @ReactMethod
  fun removeAccount(
    publicAccountUsername: String,
    publicAccountCustom: String?,
    callback: Callback
  ) {
    keyriCoroutineScope.launch(Dispatchers.IO) {
      val account = PublicAccount(publicAccountUsername, publicAccountCustom)

      keyriSdk.removeAccount(account)

      withContext(Dispatchers.Main) {
        callback.invoke()
      }
    }
  }

  @ReactMethod
  fun authWithScanner(customArg: String? = "CUSTOM") {
    reactContext.getCurrentActivity()?.let { activity ->
      keyriSdk.authWithScanner(activity, customArg)
    }
  }

  private fun toWritableMap(map: Map<String?, Any?>): WritableMap? {
    val writableMap: WritableMap = Arguments.createMap()
    val iterator = map.entries.iterator()

    while (iterator.hasNext()) {
      val pair = iterator.next()
      val value: Any? = pair.value

      when {
        value == null -> writableMap.putNull(pair.key as String)
        value is Boolean -> writableMap.putBoolean(pair.key as String, value)
        value is Double -> writableMap.putDouble(pair.key as String, value)
        value is Int -> writableMap.putInt(pair.key as String, value)
        value is String -> writableMap.putString(pair.key as String, value)
        else -> writableMap.putNull(pair.key as String)
      }
    }

    return writableMap
  }
}
