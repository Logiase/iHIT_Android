package top.logiase.ihit.data.schedule

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import okhttp3.*
import java.util.regex.Pattern

object SubjectRepertory {

    const val CAPTCHA_ERROR = 633
    const val ACCOUNT_ERROR = 453
    const val LOGIN_SUCCESS = 534
    const val LOGIN_ERROR = 664

    private val TAG = SubjectRepertory::class.java.name

    val cookieStore: MutableList<Cookie> = ArrayList()
    val httpClient: OkHttpClient
        get() {
            val client: OkHttpClient
            val spec = ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                .tlsVersions(TlsVersion.TLS_1_2)
                .cipherSuites(
                    CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,
                    CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
                    CipherSuite.TLS_DHE_RSA_WITH_AES_128_GCM_SHA256
                )
                .build()

            client = OkHttpClient.Builder()
                .cookieJar(object : CookieJar {
                    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
                        for (cookie in cookies) {
                            if (cookie.name.contains("DSID")) {
                                Log.d(
                                    SubjectRepertory.TAG,
                                    "saveFromResponse: cookie size=" + cookies.size
                                )
                                Log.d(
                                    SubjectRepertory.TAG,
                                    "saveFromResponse: name=" + cookie.name + ", value=" + cookie.value
                                )
                                SubjectRepertory.cookieStore.clear()
                                SubjectRepertory.cookieStore.addAll(cookies)
                            }
                        }
                    }

                    override fun loadForRequest(url: HttpUrl): List<Cookie> {
                        return cookieStore
                    }
                })
                .connectionSpecs(listOf(spec))
                .build()

            return client
        }

    fun vpnLogin(usrID: String, passwd: String): Int {
        val formBody = FormBody.Builder()
            .add("tz_offset", "540")
            .add("username", usrID)
            .add("password", passwd)
            .add("realm", "学生")
            .add("btnSubmit", "登录")
            .build()
        val request = Request.Builder()
            .url("https://vpn.hit.edu.cn/dana-na/auth/url_default/login.cgi")
            .post(formBody)
            .build()
        val response = httpClient.newCall(request).execute()

        if (response.priorResponse?.headers("location")!!.contains("p=user")) {
            Log.d(TAG, "vpnLogin: 已登录")
            return vpnReLogin(response.body!!.string())
        } else if (response.priorResponse?.headers("location")!!.contains("p=f")) {
            Log.d(TAG, "vpnLogin: 账号错误")
            return ACCOUNT_ERROR
        } else if (response.priorResponse?.headers("location")!!.contains("index")) {
            Log.d(TAG, "vpnLogin: 登陆成功")
            return LOGIN_SUCCESS
        }

        return LOGIN_ERROR
    }

    fun vpnReLogin(html: String): Int {
        val url = "https://vpn.hit.edu.cn/dana-na/auth/url_default/login.cgi"
        val regex =
            "<input id=\"DSIDFormDataStr\" type=\"hidden\" name=\"FormDataStr\" value=\"([^ ]+)\">" // 判断是否已经登录的正则
        var reloginToken = ""
        val p = Pattern.compile(regex)
        val m = p.matcher(html)
        if (m.find()) {
            reloginToken = m.group(1)!!
            Log.d(TAG, "vpn_relogin: FormDataStr= $reloginToken")
        }
        Log.d(TAG, "vpn_relogin: ")
        val relogin_data = FormBody.Builder()
            .add("btnContinue", "继续会话")
            .add("FormDataStr", reloginToken)
            .build()
        val client = HttpUtil.httpClient
        val request = Request.Builder()
            .url(url)
            .post(relogin_data)
            .build()
        val call = client.newCall(request)
        call.execute()
        return LOGIN_SUCCESS
    }

    fun setCookie() {
        val client = HttpUtil.httpClient
        val request = Request.Builder()
            .get()
            .url("https://vpn.hit.edu.cn/,DanaInfo=jwts.hit.edu.cn,SSO=U+")
            .build()
        val call = client.newCall(request)
        call.execute()
    }

    fun getCaptcha(): Bitmap? {
        setCookie()
        val request = Request.Builder()
            .get()
            .url("https://vpn.hit.edu.cn/,DanaInfo=jwts.hit.edu.cn+captchaImage")
            .build()
        val response = httpClient.newCall(request).execute()
        if (response.isSuccessful) {
            val inputStream = response.body!!.byteStream()
            return BitmapFactory.decodeStream(inputStream)
        } else {
            Log.d(TAG, "getCaptcha: Failed")
        }

        return null
    }

    fun jwtsLogin(usrID: String, passwd: String, captcha: String): Int {
        val formBody = FormBody.Builder()
            .add("usercode", usrID)
            .add("password", passwd)
            .add("code", captcha)
            .build()
        val request = Request.Builder()
            .url("https://vpn.hit.edu.cn/,DanaInfo=jwts.hit.edu.cn+loginLdap")
            .post(formBody)
            .build()
        val priorResponse = httpClient.newCall(request).execute().priorResponse
        if (priorResponse != null && priorResponse.header("location") == "https://vpn.hit.edu.cn/,DanaInfo=jwts.hit.edu.cn+") {
            Log.d(TAG, "jwtsLogin: 验证码错误")
            return CAPTCHA_ERROR
        }

        return LOGIN_SUCCESS
    }

    fun vpnKbPost(xnxq: String): String {
        val kb_data = FormBody.Builder()
            .add("xnxq", xnxq)
            .build()
        val request = Request.Builder()
            .post(kb_data)
            .url("https://vpn.hit.edu.cn/kbcx/,DanaInfo=jwts.hit.edu.cn+queryGrkb")
            .build()

        val response = httpClient.newCall(request).execute()
        val string = response.body!!.string()
        if (response.isSuccessful) {
            Log.d(TAG, "vpnKbPost: $string")
        } else {
            Log.d(TAG, "vpnKbPost: failed")
        }
        return string
    }
}