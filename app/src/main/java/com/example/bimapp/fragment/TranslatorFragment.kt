package com.example.bimapp.fragment

import androidx.fragment.app.Fragment
import org.pytorch.LiteModuleLoader
import org.pytorch.Module
import org.pytorch.Tensor
import android.content.Context
import com.example.bimapp.HandLandmarkerHelper
import com.example.bimapp.databinding.FragmentCameraBinding
import org.pytorch.IValue

import java.io.File
import java.io.FileOutputStream


class TranslatorFragment : Fragment() {

    object Translator {

        private val sequence = mutableListOf<List<Float>>()
        var sentence = mutableListOf<String>()

        private val predictions = mutableListOf<Int>()
        private var module: Module? = null
        private var outputTensor: Tensor? = null
        private var _fragmentCameraBinding: FragmentCameraBinding? = null


        private val fragmentCameraBinding
            get() = _fragmentCameraBinding!!

        fun processLandmarks(
            resultBundle: HandLandmarkerHelper.ResultBundle, context: Context): Pair<MutableList<String>, String>? {

            val landmarks = resultBundle.results.flatMap { it.landmarks() }.flatten()
//            println(landmarks)

            val finalLandmarks = landmarks?.let { landmarksList ->
                val flattenedList = landmarksList.flatMap { res ->
                    listOf(res.x(), res.y(), res.z())
                }

                val paddedList = flattenedList + List(126 - flattenedList.size) { 0.0 }
                paddedList
            }


            sequence.add(finalLandmarks as List<Float>)
            if (sequence.size > 30) {
                sequence.subList(0, sequence.size - 30).clear()
            }
//            println(sequence)



            if (sequence.size == 30 && landmarks.isNotEmpty()) {
                try {
                    // Load the model if not already loaded
                    if (module == null) {
                        module = LiteModuleLoader.load(assetFilePath(context, "bim_translator_model_5.ptl"))
                    }

                    if (module != null) {
                        // Preprocess the sequence of landmarks
                        val inputTensor = preprocessLandmarks(sequence)
//                        printInputTensor(inputTensor)
                        // Perform inference or other operations with the loaded model and tensor
                        outputTensor = module!!.forward(IValue.from(inputTensor)).toTensor()
                        val floatArray = outputTensor?.getDataAsFloatArray()
                        val scoresList: List<Float> = floatArray?.toList() ?: emptyList()

                        var maxScore = -Float.MAX_VALUE
                        var maxScoreIdx = -1

                        for ((index, score) in scoresList.withIndex()) {
                            if (score > maxScore) {
                                maxScore = score
                                maxScoreIdx = index
                            }
                        }
                        predictions.add(maxScoreIdx)
                        val gesture = GestureClasses.GESTURE_CLASSES[maxScoreIdx]

                        val lastPredictions = predictions.takeLast(10)
                        val uniquePredictions = lastPredictions.distinct()

                        println("Last 15 prediction: $uniquePredictions")

                        if (uniquePredictions.size == 1 && uniquePredictions[0] == maxScoreIdx){
                            val currentGesture = gesture
                            println(currentGesture)
                            if (sentence.isNotEmpty() && currentGesture != sentence.last()){
                                sentence.add(currentGesture)
                            } else if (sentence.isEmpty()){
                                sentence.add(currentGesture)
                            }
                        }

                        if (sentence.size > 4) {
                            sentence = sentence.takeLast(4).toMutableList()
                        }

                        println(sentence)


                        return Pair(sentence,gesture)
                    } else {
                        // Model loading failed
                        println("Failed to load the model")
                    }
                } catch (e: SecurityException) {
                    // Handle the exception
                    println("X jumpaaa")
                    e.printStackTrace()
                }
            } else {
                println("Received empty landmarks or sequence size less than 30 in Translator")
            }

            return null // Return null for conditions where gesture inference couldn't happen
        }

        private fun printInputTensor(inputTensor: Tensor) {
            val floatArray = inputTensor.getDataAsFloatArray()

            // Assuming the tensor has a shape of [1, 30, 126]
            val batchSize = 1
            val sequenceLength = 30
            val landmarksCount = 126

            for (i in 0 until batchSize) {
                for (j in 0 until sequenceLength) {
                    val sequenceStartIndex = i * sequenceLength * landmarksCount + j * landmarksCount
                    val sequenceEndIndex = sequenceStartIndex + landmarksCount
                    val sequenceData = floatArray.sliceArray(sequenceStartIndex until sequenceEndIndex)
                    println("Sequence $j: ${sequenceData.contentToString()}")
                }
            }
        }




        private fun preprocessLandmarks(sequence: List<List<Float>>): Tensor {
            // Assuming each sublist in the sequence contains the landmarks
            val last30Sequence = sequence.takeLast(30)
            val reshapedSequence = MutableList(30) { FloatArray(126) { 0.0f } }

            // Convert the last 30 landmarks into the desired format
            for (i in last30Sequence.indices) {
                val landmarks = last30Sequence[i]
                for (j in landmarks.indices) {
                    reshapedSequence[i][j] = landmarks[j]
                }
            }

            // Flatten the reshaped sequence manually
            val flatSequence = mutableListOf<Float>()
            for (array in reshapedSequence) {
                flatSequence.addAll(array.toList())
            }

            // Convert the flattened sequence into a PyTorch tensor
            val tensorData = flatSequence.toFloatArray()
            return Tensor.fromBlob(tensorData, longArrayOf(1, 30, 126))
        }


        private fun assetFilePath(context: Context, assetName: String): String {
            val file = File(context.filesDir, assetName)
            if (!file.exists()) {
                try {
                    val inputStream = context.assets.open(assetName)
                    val outputStream = FileOutputStream(file)
                    val buffer = ByteArray(4 * 1024)

                    var read: Int
                    while (inputStream.read(buffer).also { read = it } != -1) {
                        outputStream.write(buffer, 0, read)
                    }

                    outputStream.flush()
                    outputStream.close()
                    inputStream.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            return file.absolutePath
        }
    }

}


