package com.chaquopy.chaquopy

import androidx.annotation.NonNull
import com.chaquo.python.PyException
import com.chaquo.python.PyObject
import com.chaquo.python.Python

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import java.util.*

/** ChaquopyPlugin */
class ChaquopyPlugin : FlutterPlugin, MethodCallHandler {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private lateinit var channel: MethodChannel

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "chaquopy")
        channel.setMethodCallHandler(this)
    }

    //  * This will run python code consisting of error and result output...
    fun _runPythonTextCode(code: String): String {
        val _returnOutput: MutableMap<String, Any?> = HashMap()
        val _python: Python = Python.getInstance()
//        val _console: PyObject = _python.getModule("script")
//        val _sys: PyObject = _python.getModule("sys")
//        val _io: PyObject = _python.getModule("io")
//
//        return try {
//            val _textOutputStream: PyObject = _io.callAttr("StringIO")
//            _sys["stdout"] = _textOutputStream
//            _console.callAttrThrows("mainTextCode", code)
//            _returnOutput["textOutputOrError"] = _textOutputStream.callAttr("getvalue").toString()
//            _returnOutput
//        } catch (e: PyException) {
//            _returnOutput["textOutputOrError"] = e.message.toString()
//            _returnOutput
//        }
        // Obtain the system's input stream (available from Chaquopy)

        // Obtain the system's input stream (available from Chaquopy)
        val sys: PyObject = _python.getModule("sys")
        val io: PyObject = _python.getModule("io")
        // Obtain the right python module
        // Obtain the right python module
        val module: PyObject = _python.getModule("infer_fas_tflite")

        // Redirect the system's output stream to the Python interpreter

        // Redirect the system's output stream to the Python interpreter
        val textOutputStream: PyObject = io.callAttr("StringIO")
        sys.put("stdout", textOutputStream)

        // Create a string variable that will contain the standard output of the Python interpreter

        // Create a string variable that will contain the standard output of the Python interpreter
        var interpreterOutput = ""

        // Execute the Python code

        // Execute the Python code
        interpreterOutput = try {
            //String csvData = DBHelper.getInstance(this).getAllEntriesAsCsv();
            module.callAttr(
                "preprocess_and_infer",
                "sample_input_2.json",
                "input_scaler.z",
                "model.tflite",
                "input_columns.csv",
                "params.json"
            )

            //module.callAttrThrows("main", "hrv.csv");
            interpreterOutput = textOutputStream.callAttr("getvalue").toString()
        } catch (e: PyException) {
            // If there's an error, you can obtain its output as well
            // e.g. if you mispell the code
            // Missing parentheses in call to 'print'
            // Did you mean print("text")?
            // <string>, line 1
            interpreterOutput = e.message.toString()
        }

        // Outputs the results:

        return interpreterOutput
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        if (call.method == "runPythonScript") {
            try {
                val code: String? = call.arguments()
                val _code: String = code?.toString().orEmpty()
                val _result: Map<String, Any?> = _runPythonTextCode(_code)
                result.success(_result)
            } catch (e: Exception) {
                val _result: MutableMap<String, Any?> = HashMap()
                _result["textOutputOrError"] = e.message.toString()
                result.success(_result)
            }
        }
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }
}
