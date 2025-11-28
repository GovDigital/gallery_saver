package carnegietechnologies.gallery_saver

import android.app.Activity
import androidx.annotation.NonNull
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result

class GallerySaverPlugin : FlutterPlugin, MethodCallHandler, ActivityAware {

    private lateinit var channel: MethodChannel
    private var activity: Activity? = null
    private var gallerySaver: GallerySaver? = null
    private var activityBinding: ActivityPluginBinding? = null

    override fun onAttachedToEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(binding.binaryMessenger, "gallery_saver")
        channel.setMethodCallHandler(this)
    }

    override fun onMethodCall(call: MethodCall, result: Result) {
        val act = activity
        if (act == null) {
            result.error("NO_ACTIVITY", "Plugin requires an Activity.", null)
            return
        }

        if (gallerySaver == null) {
            gallerySaver = GallerySaver(act)
            activityBinding?.addRequestPermissionsResultListener(gallerySaver!!)
        }

        when (call.method) {

            // Updated method names expected by most forks
            "saveImageToGallery",
            "saveImage" -> {
                gallerySaver!!.checkPermissionAndSaveFile(call, result, MediaType.image)
            }

            "saveVideoToGallery",
            "saveVideo" -> {
                gallerySaver!!.checkPermissionAndSaveFile(call, result, MediaType.video)
            }

            else -> result.notImplemented()
        }
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activity = binding.activity
        activityBinding = binding
        gallerySaver = GallerySaver(binding.activity)
        binding.addRequestPermissionsResultListener(gallerySaver!!)
    }

    override fun onDetachedFromActivity() {
        gallerySaver?.let {
            activityBinding?.removeRequestPermissionsResultListener(it)
        }
        activityBinding = null
        activity = null
        gallerySaver = null
    }


    override fun onDetachedFromActivityForConfigChanges() {
        onDetachedFromActivity()
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        onAttachedToActivity(binding)
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }
}
