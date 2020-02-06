package top.logiase.ihit.data.schedule

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import okhttp3.*
import java.io.IOException
import java.util.*
import java.util.regex.Pattern

/**
 * HTTP网络请求封装工具类，用于登录，课表请求等
 */
object HttpUtil {
    private val TAG = HttpUtil::class.java.name
    private val cookieStore: MutableList<Cookie> = ArrayList()
    private const val ACCONUT_ERROR = 504
    private const val CAPTCHA_ERROR = 486
    private const val LOGIN_SUCCESS = 433
    /**
     * 登录hit vpn
     * @param usrId
     * @param pwd
     * @return
     * @throws IOException
     */
    @Throws(IOException::class)
    fun vpn_login(usrId: String?, pwd: String?): Int {
        val url = "https://vpn.hit.edu.cn/dana-na/auth/url_default/login.cgi"
        val client = httpClient
        val vpn_data = FormBody.Builder()
            .add("tz_offset", "540")
            .add("username", usrId!!)
            .add("password", pwd!!)
            .add("realm", "学生")
            .add("btnSubmit", "登录")
            .build()
        val request = Request.Builder()
            .url(url)
            .post(vpn_data)
            .build()
        val call = client.newCall(request)
        val response = call.execute()
        val prior = response.priorResponse
        val html = response.body!!.string()
        if (prior!!.header("location")!!.contains("p=user")) { //  https://vpn.hit.edu.cn/dana-na/auth/url_default/welcome.cgi?p=user-confirm&id=
// 此时表示账号已登录，此时需要重新登录
            Log.d(TAG, "vpn_login: 已登录，重新登录")
            vpn_relogin(html)
        } else if (prior.header("location")!!.contains("p=f")) { // https://vpn.hit.edu.cn/dana-na/auth/url_default/welcome.cgi?p=failed
            Log.d(TAG, "vpn_login: 账号或密码错误")
            return ACCONUT_ERROR
        } else if (prior.header("location")!!.contains("index")) {
            Log.d(TAG, "vpn_login: 登录成功")
        }
        return LOGIN_SUCCESS
    }

    /**
     * 当vpn已登录时，用于重新登录
     * @param html 含上次会话token的网页
     * @return
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun vpn_relogin(html: String): Int {
        val url = "https://vpn.hit.edu.cn/dana-na/auth/url_default/login.cgi"
        val regex =
            "<input id=\"DSIDFormDataStr\" type=\"hidden\" name=\"FormDataStr\" value=\"([^ ]+)\">" // 判断是否已经登录的正则
        var relogin_token = ""
        // 获取已登录的token
        val p = Pattern.compile(regex)
        val m = p.matcher(html)
        if (m.find()) {
            relogin_token = m.group(1)
            Log.d(TAG, "vpn_relogin: FormDataStr= $relogin_token")
        }
        Log.d(TAG, "vpn_relogin: ")
        val relogin_data = FormBody.Builder()
            .add("btnContinue", "继续会话")
            .add("FormDataStr", relogin_token)
            .build()
        val client = httpClient
        val request = Request.Builder()
            .url(url)
            .post(relogin_data)
            .build()
        val call = client.newCall(request)
        val response = call.execute()
        return LOGIN_SUCCESS
    }

    /**
     * 在使用vpn登录jwts请求验证码之前，先请求一下该网址，将Cookie送过去再说
     */
    @Throws(IOException::class)
    fun set_cookie_before_login() {
        val client = httpClient
        val request = Request.Builder()
            .get()
            .url("https://vpn.hit.edu.cn/,DanaInfo=jwts.hit.edu.cn,SSO=U+")
            .build()
        val call = client.newCall(request)
        call.execute()
    }//获取流
    //转化为bitmap
    // TODO 改

    /**
     * vpn登录之前，获取验证码
     * @return
     * @throws IOException // SocketTimeoutException
     */
    @get:Throws(IOException::class)
    val captchaImage: Bitmap?
        get() {
            set_cookie_before_login()
            val url = "https://vpn.hit.edu.cn/,DanaInfo=jwts.hit.edu.cn+captchaImage"
            val client = httpClient
            val request = Request.Builder()
                .get()
                .url(url)
                .build()
            val call = client.newCall(request)
            val response = call.execute()
            val body = response.body
            if (response.isSuccessful) { //获取流
                val `in` = body?.byteStream()
                //转化为bitmap
                return BitmapFactory.decodeStream(`in`)
            } else {
                Log.d(TAG, "getCaptchaImage: failed")
            }
            return null // TODO 改
        }

    /**
     * 经vpn登录jwts
     * @param usrId 学号
     * @param pwd 密码
     * @param captcha 验证码
     * @return 返回登录结果
     * @throws IOException
     */
    @Throws(IOException::class)
    fun vpn_jwts_login(usrId: String?, pwd: String?, captcha: String?): Int {
        val url = "https://vpn.hit.edu.cn/,DanaInfo=jwts.hit.edu.cn+loginLdap"
        //        String url = "https://vpn.hit.edu.cn/,DanaInfo=jwts.hit.edu.cn+login";
        val client = httpClient
        val jwts_data = FormBody.Builder()
            .add("usercode", usrId!!)
            .add("password", pwd!!)
            .add("code", captcha!!)
            .build()
        val request = Request.Builder()
            .url(url)
            .post(jwts_data)
            .build()
        val call = client.newCall(request)
        val response = call.execute()
        val prior = response.priorResponse
        if (prior != null && prior.header("location") == "https://vpn.hit.edu.cn/,DanaInfo=jwts.hit.edu.cn+") { //  https://vpn.hit.edu.cn/dana-na/auth/url_default/welcome.cgi?p=user-confirm&id=
// 此时表示账号已登录，此时需要重新登录
            Log.d(TAG, "vpn_kb_post: 验证码错误")
            return CAPTCHA_ERROR
        }
        return LOGIN_SUCCESS
    }

    /**
     * 获取某一学期的课表
     * @return
     * @throws IOException
     */
    @Throws(IOException::class)
    fun vpn_kb_post(xnxq: String?): String {
        val url = "https://vpn.hit.edu.cn/kbcx/,DanaInfo=jwts.hit.edu.cn+queryGrkb"
        val client = httpClient
        val kb_data = FormBody.Builder()
            .add("xnxq", xnxq!!)
            .build()
        val request = Request.Builder()
            .post(kb_data)
            .url(url)
            .build()
        val call = client.newCall(request)
        val response = call.execute()
        val string = response.body!!.string()
        if (response.isSuccessful) {
            Log.d(TAG, "vpn_kb_post: $string")
        } else {
            Log.d(TAG, "vpn_kb_post: failed")
        }
        return string
    }

    /**
     * 获取某一学期的课表
     * @return
     * @throws IOException
     */
    @Throws(IOException::class)
    fun vpn_kb_post_test(xnxq: String?, xh: String?): String {
        val url =
            "https://vpn.hit.edu.cn/jskbcx/,DanaInfo=jwts.hit.edu.cn+BzrqueryGrkbTy"
        val client = httpClient
        val kb_data = FormBody.Builder()
            .add("xnxq", xnxq!!)
            .add("xh", xh!!)
            .build()
        val request = Request.Builder()
            .post(kb_data)
            .url(url)
            .build()
        val call = client.newCall(request)
        val response = call.execute()
        val string = response.body!!.string()
        if (response.isSuccessful) {
            Log.d(TAG, "vpn_kb_post: $string")
        } else {
            Log.d(TAG, "vpn_kb_post: failed")
        }
        return string
    }

    //                        cookieStore.addAll(cookies);
    val httpClient: OkHttpClient
        get() {
            val client: OkHttpClient
            val spec: ConnectionSpec = ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                .tlsVersions(TlsVersion.TLS_1_2)
                .cipherSuites(
                    TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,
                    TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
                    TLS_DHE_RSA_WITH_AES_128_GCM_SHA256
                )
                .build()
            client = OkHttpClient.Builder()
                .cookieJar(object : CookieJar {
                    override fun saveFromResponse(
                        url: HttpUrl,
                        cookies: List<Cookie>
                    ) { //                        cookieStore.addAll(cookies);
                        for (cookie in cookies) {
                            if (cookie.name.contains("DSID")) {
                                Log.d(
                                    TAG,
                                    "saveFromResponse: cookie size=" + cookies.size
                                )
                                Log.d(
                                    TAG,
                                    "saveFromResponse: name=" + cookie.name + ", value=" + cookie.value
                                )
                                cookieStore.clear()
                                cookieStore.addAll(cookies)
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
}