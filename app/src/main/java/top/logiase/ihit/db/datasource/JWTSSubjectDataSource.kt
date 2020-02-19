package top.logiase.ihit.db.datasource

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import okhttp3.*
import java.util.regex.Pattern

/**
 * @author Logiase
 */
object JWTSSubjectDataSource {

    private val TAG = this::class.java.name

    const val LOGIN_SUCCESS = 4001
    const val ACCOUNT_ERROR = 4002
    const val CAPTCHA_ERROR = 4003
    const val LOGIN_ERROR = 4004

    private val cookieStore = HashMap<String, List<Cookie>>()

    private val httpClient: OkHttpClient by lazy {
        val spec = ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
            .tlsVersions(TlsVersion.TLS_1_2)
            .build()

        return@lazy OkHttpClient.Builder()
            .cookieJar(object : CookieJar {
                override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
                    cookieStore[url.host] = cookies
                }

                override fun loadForRequest(url: HttpUrl): List<Cookie> {
                    return cookieStore[url.host] ?: emptyList()
                }

            })
            .connectionSpecs(listOf(spec))
            .build()
    }

    private fun vpnLogin(userID: String, password: String): Int {
        val formBody: FormBody = FormBody.Builder()
            .add("tz_offset", "540")
            .add("username", userID)
            .add("password", password)
            .add("realm", "学生")
            .add("btnSubmit", "登录")
            .build()
        val request = Request.Builder()
            .url("https://vpn.hit.edu.cn/dana-na/auth/url_default/login.cgi")
            .post(formBody)
            .build()
        val response = httpClient.newCall(request).execute()

        return when {
            response.priorResponse?.headers("location")!!.contains("p=user") -> {
                Log.d(TAG, "vpnLogin: 已登录")
                vpnReLogin(response.body!!.string())
            }
            response.priorResponse?.headers("location")!!.contains("p=f") -> {
                Log.d(TAG, "vpnLogin: 账号错误")
                ACCOUNT_ERROR
            }
            response.priorResponse?.headers("location")!!.contains("index") -> {
                Log.d(TAG, "vpnLogin: 登陆成功")
                LOGIN_SUCCESS
            }
            else -> LOGIN_ERROR
        }
    }

    private fun vpnReLogin(html: String): Int {
        val url = "https://vpn.hit.edu.cn/dana-na/auth/url_default/login.cgi"
        val regex =
            "<input id=\"DSIDFormDataStr\" type=\"hidden\" name=\"FormDataStr\" value=\"([^ ]+)\">" // 判断是否已经登录的正则
        var reloginToken = ""
        val m = Pattern.compile(regex).matcher(html)
        if (m.find()) {
            reloginToken = m.group(1)!!
            Log.d(TAG, "vpn_relogin: FormDataStr= $reloginToken")
        }
        Log.d(TAG, "vpn_relogin: ")
        val reLoginData = FormBody.Builder()
            .add("btnContinue", "继续会话")
            .add("FormDataStr", reloginToken)
            .build()
        val client = httpClient
        val request = Request.Builder()
            .url(url)
            .post(reLoginData)
            .build()
        val call = client.newCall(request)
        call.execute()
        return LOGIN_SUCCESS
    }

    private fun setCookie() {
        val request = Request.Builder()
            .get()
            .url("https://vpn.hit.edu.cn/,DanaInfo=jwts.hit.edu.cn,SSO=U+")
            .build()
        val call = httpClient.newCall(request)
        call.execute()
    }

    private fun getCaptchaImage(): Bitmap? {
        setCookie()

        val url = "https://vpn.hit.edu.cn/,DanaInfo=jwts.hit.edu.cn+captchaImage"

        val request = Request.Builder()
            .get()
            .url(url)
            .build()

        val response = httpClient.newCall(request).execute()
        return if (response.isSuccessful) {
            val inStream = response.body?.byteStream()
            BitmapFactory.decodeStream(inStream)
        } else {
            Log.d(TAG, "jwts:获取验证码失败")
            null
        }
    }

    private fun jwtsLogin(userID: String, password: String, captcha: String): Int {
        val formBody = FormBody.Builder()
            .add("usercode", userID)
            .add("password", password)
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

    private fun vpnKbPost(xnxq: String): String {
        val kbData = FormBody.Builder()
            .add("xnxq", xnxq)
            .build()
        val request = Request.Builder()
            .post(kbData)
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