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
  fun initialize(appKey: String, publicKey: String, callbackUrl: String, allowMultipleAccounts: Boolean) {
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
  fun handleSessionId(sessionId: String, promise: Promise) {
    checkIsinit()
    keyriCoroutineScope.launch(Dispatchers.IO) {
      try {
        val session = keyriSdk.onReadSessionId(sessionId)

        withContext(Dispatchers.Main) {
          val resultData = WritableNativeMap()

          resultData.putString("serviceId", session.service.serviceId)
          resultData.putString("serviceName", session.service.name)
          resultData.putString("serviceLogo", session.service.logo)
          resultData.putString("username", session.username)
          resultData.putBoolean("isNewUser", session.isNewUser)

          promise.resolve(resultData)
        }
      } catch (e: Throwable) {
        promise.reject(handleException(e))
      }
    }
  }

  @ReactMethod
  fun sessionSignup(
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
  fun sessionLogin(
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
  fun directSignup(username: String, custom: String?, extendedHeaders: ReadableMap, promise: Promise) {
    checkIsinit()
    keyriCoroutineScope.launch(Dispatchers.IO) {
      try {
        val headers = extendedHeaders.toHashMap().map { it.key to it.value.toString() }.toMap()
        val authMobileResponse = keyriSdk.mobileSignup(username, custom, headers)

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
        val accounts = keyriSdk.accounts()

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
