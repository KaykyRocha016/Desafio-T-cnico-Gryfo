package com.example.learningcamerax

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture.OnImageCapturedCallback
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.learningcamerax.ui.theme.LearningCameraXTheme
import com.plcoding.cameraxguide.PhotoBottomSheetContent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(!hasRequiredPermissions()){
            ActivityCompat.requestPermissions(
                this,CAMERAX_PERMISSIONS,0
            )
        }
        setContent {
            LearningCameraXTheme {
                val scaffoldState= rememberBottomSheetScaffoldState()
                val scope= rememberCoroutineScope()

                val controller= remember {
                    LifecycleCameraController(applicationContext).apply {
                        setEnabledUseCases(CameraController.IMAGE_CAPTURE or CameraController.VIDEO_CAPTURE)
                    }
                }
                // Passando a atividade ao criar a instância do MainViewModel
                val viewModel = viewModel { MainViewModel(this@MainActivity) }
                val bitmaps by viewModel.bitmaps.collectAsState()
                val response by viewModel.response.collectAsState()

                //um LaunchedEffect para observar a resposta
                LaunchedEffect(viewModel.response) {
                    viewModel.response.collect { response ->
                        if (response.isNotEmpty()) {
                            scaffoldState.snackbarHostState.showSnackbar(
                                message = response,
                                duration = SnackbarDuration.Long

                            )
                            

                        }
                    }
                }

                BottomSheetScaffold(
                    scaffoldState=scaffoldState,
                    sheetPeekHeight=8.dp,
                    sheetContent={
                        PhotoBottomSheetContent(
                            bitmaps =bitmaps,
                            modifier=Modifier.fillMaxSize()
                        )

                    }
                ){ padding->
                    Box(modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)){
                        CameraPreview(controller =controller , modifier = Modifier.fillMaxSize())
                        IconButton(onClick = {
                            controller.cameraSelector = if(controller.cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA)
                                CameraSelector.DEFAULT_FRONT_CAMERA
                            else
                                CameraSelector.DEFAULT_BACK_CAMERA
                        },
                            modifier=Modifier.offset(16.dp,16.dp)

                        )
                        {
                            Icon(
                                imageVector= Icons.Default.Cameraswitch,
                                contentDescription="switch Camera"
                            )

                        }
                        Row(
                            modifier= Modifier
                                .fillMaxSize()
                                .align(Alignment.BottomCenter)
                                .padding(16.dp), horizontalArrangement = Arrangement.SpaceAround


                        ){
                            IconButton(onClick = { /*TODO*/
                                scope.launch { scaffoldState.bottomSheetState.expand() }
                            }) {
                                Icon(
                                    imageVector=Icons.Default.Photo,
                                    contentDescription="open gallery"
                                )

                            }
                            IconButton(onClick = { /*TODO*/
                                takeAPhoto(controller=controller, onPhotoTake = viewModel::onTakePhoto)
                            }) {
                                Icon(
                                    imageVector=Icons.Default.PhotoCamera,
                                    contentDescription="take a photo"
                                )

                            }

                        }

                    }

                }

            }
        }

    }
    private fun takeAPhoto(controller:LifecycleCameraController,onPhotoTake: (Bitmap)->Unit) {
        controller.takePicture(
            ContextCompat.getMainExecutor(applicationContext),
            object: OnImageCapturedCallback(){
                override fun onCaptureSuccess(image: ImageProxy) {
                    super.onCaptureSuccess(image)
                    val matrix=Matrix().apply {
                        postRotate(image.imageInfo.rotationDegrees.toFloat())
                    }
                    val rotatedBitmap=Bitmap.createBitmap(image.toBitmap(),0,0,image.width,image.height,matrix,true)
                    onPhotoTake(rotatedBitmap)
                }

                override fun onError(exception: ImageCaptureException) {
                    super.onError(exception)
                    Log.e("Camera","não foi possivel tirar a foto", exception)
                }
            }
        )
    }
    private fun hasRequiredPermissions():Boolean{
        return CAMERAX_PERMISSIONS.all { ContextCompat.checkSelfPermission(
            applicationContext,it
        )== PackageManager.PERMISSION_GRANTED }

    }
    companion object{
        private val CAMERAX_PERMISSIONS=
            arrayOf(Manifest.permission.CAMERA,Manifest.permission.RECORD_AUDIO)
    }
}
// ViewModel para a lógica da câmera e a comunicação com o servidor
// ViewModel para a lógica da câmera e a comunicação com o servidor
class MainViewModel(private val activity: Activity) : ViewModel() {
    private val _bitmaps = MutableStateFlow<List<Bitmap>>(emptyList())
    val bitmaps: StateFlow<List<Bitmap>> = _bitmaps

    //StateFlow para armazenar a resposta do servidor
    private val _response = MutableStateFlow<String>("")
    val response: StateFlow<String> = _response

    // Função chamada quando uma foto é capturada
    fun onTakePhoto(bitmap: Bitmap) {
        _bitmaps.value = _bitmaps.value + bitmap
        if (_bitmaps.value.size == 2) {
            sendPhotosToEndpointAsync(_bitmaps.value[0], _bitmaps.value[1])
            _bitmaps.value = emptyList()
        }
    }

    // Envia as fotos capturadas para o servidor de forma assíncrona
    private fun sendPhotosToEndpointAsync(bitmap1: Bitmap, bitmap2: Bitmap) {
        val client = OkHttpClient()
        val url = "https://api.gryfo.com.br/face_match"

        val bitmap1Base64 = bitmapToBase64(bitmap1)
        val bitmap2Base64 = bitmapToBase64(bitmap2)

        val jsonObject = JSONObject()
        jsonObject.put("document_img", bitmap1Base64)
        jsonObject.put("face_img", bitmap2Base64)

        val body = jsonObject.toString()
            .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

        val request = Request.Builder()
            .addHeader("Authorization", "DesafioEstag:9sndf96soADfhnJSgnsJDFiufgnn9suvn498gBN9nfsDesafioEstag")
            .url(url)
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                _response.value = "Erro ao enviar fotos: ${e.message}"
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    _response.value = "requisição realizada com sucesso, código: ${response.code}\n"
                } else {
                    _response.value = "Erro na requisição, código: ${response.code} }"
                }

            }
        })
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }
}