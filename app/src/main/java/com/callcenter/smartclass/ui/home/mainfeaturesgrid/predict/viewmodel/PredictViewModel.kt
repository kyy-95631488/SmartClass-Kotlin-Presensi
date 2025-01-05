package com.callcenter.smartclass.ui.home.mainfeaturesgrid.predict.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.callcenter.smartclass.data.ChildProfile
import com.callcenter.smartclass.ui.home.mainfeaturesgrid.predictv2.data.AppDatabase
import com.callcenter.smartclass.ui.home.mainfeaturesgrid.predictv2.data.Child
import com.callcenter.smartclass.ui.home.mainfeaturesgrid.predictv2.data.ChildDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class PredictViewModel(application: Application) : AndroidViewModel(application) {
    private var tflite: Interpreter? = null
    private val childDao: ChildDao = AppDatabase.getDatabase(application).childDao()

    @Throws(IOException::class)
    private fun loadModelFile(context: android.content.Context): MappedByteBuffer {
        val assetManager = context.assets
        assetManager.openFd("smartclass.tflite").use { fileDescriptor ->
            FileInputStream(fileDescriptor.fileDescriptor).use { inputStream ->
                val fileChannel: FileChannel = inputStream.channel
                val startOffset: Long = fileDescriptor.startOffset
                val declaredLength: Long = fileDescriptor.declaredLength
                return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
            }
        }
    }

    fun loadModel(context: android.content.Context) {
        try {
            val modelBuffer = loadModelFile(context)
            tflite = Interpreter(modelBuffer)
            Log.d("PredictViewModel", "Model berhasil dimuat")
        } catch (e: IOException) {
            Log.e("PredictViewModel", "Error saat memuat model", e)
        }
    }

    fun fetchChildrenData(onResult: (List<ChildProfile>) -> Unit) {
        viewModelScope.launch {
            try {
                val children: List<Child> = withContext(Dispatchers.IO) {
                    childDao.getAllChildren()
                }
                val childProfiles = children.map { child ->
                    ChildProfile(
                        id = child.id,
                        name = child.name,
                        birthDate = child.birthDate,
                        gender = child.gender,
                        height = child.height,
                        weight = child.weight,
                        headCircumference = child.headCircumference
                    )
                }
                onResult(childProfiles)
            } catch (e: Exception) {
                Log.e("PredictViewModel", "Error saat mengambil data dari Room", e)
                onResult(emptyList())
            }
        }
    }

    fun predict(child: ChildProfile): FloatArray? {
        tflite?.let { interpreter ->
            val heightValue = child.height.replace(",", ".").toFloatOrNull()
            val weightValue = child.weight.replace(",", ".").toFloatOrNull()
            val headCircumferenceValue = child.headCircumference.replace(",", ".").toFloatOrNull()
            val ageValue = child.getAge()

            if (heightValue == null || weightValue == null || headCircumferenceValue == null || ageValue == null) {
                Log.e("PredictViewModel", "Data input tidak valid")
                return null
            }

            val gender = if (child.gender.lowercase() == "perempuan") 0.0f else 1.0f

            val inputSize = 5
            val inputBuffer = ByteBuffer.allocateDirect(4 * inputSize).order(ByteOrder.nativeOrder())
            inputBuffer.putFloat(gender)
            inputBuffer.putFloat(ageValue)
            inputBuffer.putFloat(heightValue)
            inputBuffer.putFloat(weightValue)
            inputBuffer.putFloat(headCircumferenceValue)
            inputBuffer.rewind()

            val outputTensor = interpreter.getOutputTensor(0)
            val outputShape = outputTensor.shape()
            val outputSize = outputShape[1]
            val outputBuffer = ByteBuffer.allocateDirect(4 * outputSize).order(ByteOrder.nativeOrder())

            interpreter.run(inputBuffer, outputBuffer)
            outputBuffer.rewind()

            val predictions = FloatArray(outputSize)
            outputBuffer.asFloatBuffer().get(predictions)

            Log.d("PredictViewModel", "Prediksi stunting: ${predictions.joinToString()}")

            return predictions
        } ?: run {
            Log.e("PredictViewModel", "Interpreter belum diinisialisasi")
            return null
        }
    }

    fun deleteChild(childProfile: ChildProfile, onResult: () -> Unit) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val child = childDao.getChildById(childProfile.id)
                if (child != null) {
                    childDao.deleteChild(child)
                }
            }
            onResult()
        }
    }

    override fun onCleared() {
        super.onCleared()
        tflite?.close()
    }
}
