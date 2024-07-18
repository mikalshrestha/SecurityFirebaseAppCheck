package com.mikal.minorproject

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header

class MainActivity : AppCompatActivity() {

    private var btnSendRequest: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        FirebaseApp.initializeApp(this)
        val firebaseAppCheck = FirebaseAppCheck.getInstance()
        firebaseAppCheck.installAppCheckProviderFactory(
            PlayIntegrityAppCheckProviderFactory.getInstance()
        )
        btnSendRequest = findViewById(R.id.btnSendRequest);

        btnSendRequest?.setOnClickListener {
            getAppCheckTokenAndMakeApiCall()
        }
    }

    private fun getAppCheckTokenAndMakeApiCall() {
        FirebaseAppCheck.getInstance().getToken(false)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val appCheckToken = task.result?.token
                    Log.d("AppCheckToken", appCheckToken ?: "")
                    appCheckToken?.let { makeApiCall(it) }
                } else {
                    Log.e("AppCheck", "Error getting App Check token", task.exception)
                }
            }
    }


    private fun makeApiCall(token: String) {
        val call = RetrofitClient.apiService.getUserDetails(token)

        call.enqueue(object : Callback<ApiResponseModel> {
            override fun onResponse(
                call: Call<ApiResponseModel>,
                response: Response<ApiResponseModel>
            ) {
                if (response.isSuccessful) {
                    val responseModel = response.body()
                    if (responseModel != null && responseModel.isSuccess) {
                        val convertedMessage =
                            "Your details fetched successfully with Username: ${responseModel.username} and Email: ${responseModel.email}"
                        showToastDialog(this@MainActivity, convertedMessage)
                    } else {
                        showToastDialog(this@MainActivity, response.message())
                    }
                }
            }

            override fun onFailure(call: Call<ApiResponseModel>, t: Throwable) {
                showToastDialog(this@MainActivity, t.localizedMessage)
            }
        })
    }

    fun showToastDialog(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}


object RetrofitClient {
    private const val BASE_URL = "http://10.13.160.247:8081/api/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: ApiService = retrofit.create(ApiService::class.java)
}

interface ApiService {
    @GET("user/details")
    fun getUserDetails(
        @Header("X-Firebase-AppCheck") token: String
    ): Call<ApiResponseModel>
}

data class ApiResponseModel(
    @SerializedName("success")
    val isSuccess: Boolean = false,
    val message: String,
    val username: String = "",
    val email: String = "",
    val mobileNumber: String = "",
    val address: String = ""

)
