package com.reactnativekeyri

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.keyrico.keyrisdk.KeyriSdk
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
            putString("ttl", session.ttl)
            putString("logo", session.logo)
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
    reactContext.getCurrentActivity()?.let { activity ->
      authWithScannerPromise = promise

      val publicUserId: String =
        data.getString("publicUserId") ?: throw java.lang.IllegalStateException("You need to provide publicUserId")
      val secureCustom: String? = data.takeIf { it.hasKey("secureCustom") }?.getString("secureCustom")
      val publicCustom: String? = data.takeIf { it.hasKey("publicCustom") }?.getString("publicCustom")

      keyriSdk.easyKeyriAuth(publicUserId, activity as AppCompatActivity, AUTH_REQUEST_CODE, secureCustom, publicCustom)
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

  companion object {
    private const val AUTH_REQUEST_CODE = 43411
  }
}
