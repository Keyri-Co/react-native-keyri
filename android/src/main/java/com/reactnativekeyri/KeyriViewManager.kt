package com.reactnativekeyri

import android.graphics.Color
import android.view.View
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.annotations.ReactProp

class KeyriViewManager : SimpleViewManager<View>() {
  override fun getName() = "KeyriView"

  override fun createViewInstance(reactContext: ThemedReactContext): View {
    return View(reactContext)
  }

  @ReactProp(name = "color")
  fun setColor(view: View, color: String) {
    view.setBackgroundColor(Color.parseColor(color))
  }
}
