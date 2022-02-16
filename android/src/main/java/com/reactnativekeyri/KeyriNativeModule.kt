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
import com.facebook.react.bridge.ReadableArray
import com.facebook.react.bridge.ReadableType
import com.facebook.react.bridge.WritableMap
import com.facebook.react.bridge.WritableArray
import com.facebook.react.bridge.ReadableMap
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
  fun initSdk(appKey: String, publicKey: String, callbackUrl: String, allowMultipleAccounts: Boolean) {
    if (!::keyriSdk.isInitialized) {
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
  fun onReadSessionId(sessionId: String, promise: Promise) {
    checkIsinit()
    keyriCoroutineScope.launch(Dispatchers.IO) {
      try {
        val session = keyriSdk.onReadSessionId(sessionId)

        withContext(Dispatchers.Main) {
          promise.resolve(session)
        }
      } catch (e: Throwable) {
        promise.reject(handleException(e))
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
    promise: Promise
  ) {
    checkIsinit()
    keyriCoroutineScope.launch(Dispatchers.IO) {
      try {
        val service = Service(serviceId, serviceName, serviceLogo)

        keyriSdk.signup(username, sessionId, service, custom)

        withContext(Dispatchers.Main) {
          promise.resolve("Signed up")
        }
      } catch (e: Throwable) {
        promise.reject(handleException(e))
      }
    }
  }

  @ReactMethod
  fun login(
    publicAccountUsername: String,
    publicAccountCustom: String?,
    sessionId: String,
    serviceId: String,
    serviceName: String,
    serviceLogo: String,
    custom: String?,
    promise: Promise
  ) {
    checkIsinit()
    keyriCoroutineScope.launch(Dispatchers.IO) {
      try {
        val service = Service(serviceId, serviceName, serviceLogo)
        val account = PublicAccount(publicAccountUsername, publicAccountCustom)

        keyriSdk.login(account, sessionId, service, custom)

        withContext(Dispatchers.Main) {
          promise.resolve("Signed in")
        }
      } catch (e: Throwable) {
        promise.reject(handleException(e))
      }
    }
  }

  @ReactMethod
  fun mobileSignup(username: String, custom: String?, extendedHeaders: ReadableMap, promise: Promise) {
    checkIsinit()
    keyriCoroutineScope.launch(Dispatchers.IO) {
      try {
        val headers = extendedHeaders.toHashMap().map { it.key to it.value.toString() }.toMap()
        val authMobileResponse = keyriSdk.mobileSignup(username, custom, headers)

        withContext(Dispatchers.Main) {
          promise.resolve(authMobileResponse)
        }
      } catch (e: Throwable) {
        promise.reject(handleException(e))
      }
    }
  }

  @ReactMethod
  fun mobileLogin(
    publicAccountUsername: String,
    publicAccountCustom: String?,
    extendedHeaders: ReadableMap,
    promise: Promise
  ) {
    checkIsinit()
    keyriCoroutineScope.launch(Dispatchers.IO) {
      try {
        val account = PublicAccount(publicAccountUsername, publicAccountCustom)
        val headers = extendedHeaders.toHashMap().map { it.key to it.value.toString() }.toMap()
        val authMobileResponse = keyriSdk.mobileLogin(account, headers)

        withContext(Dispatchers.Main) {
          promise.resolve(authMobileResponse)
        }
      } catch (e: Throwable) {
        promise.reject(handleException(e))
      }
    }
  }

  @ReactMethod
  fun accounts(promise: Promise) {
    checkIsinit()
    keyriCoroutineScope.launch(Dispatchers.IO) {
      try {
        val accounts = keyriSdk.accounts()

        withContext(Dispatchers.Main) {
          promise.resolve(accounts)
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
  fun authWithScanner(customArg: String? = "CUSTOM", promise: Promise) {
    checkIsinit()
    reactContext.getCurrentActivity()?.let { activity ->
      authWithScannerPromise = promise
      keyriSdk.authWithScanner(activity, customArg)
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
}
