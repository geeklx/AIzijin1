package com.example.data

import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

// Moshi-compatible request and response models
data class GeminiPart(val text: String)
data class GeminiContent(val parts: List<GeminiPart>)
data class GeminiRequest(val contents: List<GeminiContent>)

data class GeminiCandidate(val content: GeminiContent?)
data class GeminiResponse(val candidates: List<GeminiCandidate>?)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object GeminiRetrofitClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val service: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }
}

class GeminiManager {

    suspend fun analyzeSectorFlow(sector: Sector): String = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext generateOfflineAnalysis(sector)
        }

        val prompt = """
            你是一位资深的A股量化策略分析师。
            请针对以下行业板块进行精简、深刻的资金流向研报分析：
            - 板块名称: ${sector.name}
            - 今日主力资金净流向: ${sector.flowAmount} 亿
            - 5日历史流向趋势: ${sector.historyFlows.joinToString(" 亿 -> ")} 亿
            - 板块核心龙头股今日资金流向情况:
              ${sector.leadingStocks.joinToString("\n  ") { "${it.name} (现价: ${it.price}, 涨跌幅: ${it.changePercent}, 资金流向: ${it.flowAmount}亿)" }}
            - 板块简述: ${sector.description}

            请提供：
            1. 【资金面剖析】: 深入分析该板块当前的资金进出深层逻辑。
            2. 【趋势与拐点】: 评估当前5日资金趋势，分析是阶段性吸筹、砸盘止盈，还是筑底期。
            3. 【后市战术建议】: 给出具体的操作建议（如分批建仓、逢高减仓、继续观望）。

            要求：
            - 语气专业、严谨、客观。
            - 回复内容精炼，重点突出，排版整洁，使用中文。
            - 长度控制在300字以内，不要多余废话。
        """.trimIndent()

        val requestBody = GeminiRequest(
            contents = listOf(
                GeminiContent(
                    parts = listOf(GeminiPart(text = prompt))
                )
            )
        )

        try {
            val response = GeminiRetrofitClient.service.generateContent(apiKey, requestBody)
            val responseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (!responseText.isNullOrBlank()) {
                responseText
            } else {
                "未能获取AI生成分析。提示：请检查网络或API额度。" + "\n\n" + generateOfflineAnalysis(sector)
            }
        } catch (e: Exception) {
            "AI分析服务暂时不可用（${e.localizedMessage ?: "连接失败"}）。" + "\n\n" + generateOfflineAnalysis(sector)
        }
    }

    private fun generateOfflineAnalysis(sector: Sector): String {
        val flowText = if (sector.flowAmount >= 0) "呈现净流入态势（+${sector.flowAmount}亿）" else "呈现净流出失血态势（${sector.flowAmount}亿）"
        val trendText = if (sector.historyFlows.last() > sector.historyFlows.first()) "震荡走高" else "震荡回踩"
        
        val recommend = if (sector.flowAmount > 20) {
            "【后市建议】: 当前处于资金强势突破期，核心龙头（如${sector.leadingStocks.firstOrNull()?.name ?: "龙头股"}）成交额急剧放大。激进投资者可轻仓顺势追击，稳健投资者可等回踩重要均线支撑时再分批吸纳。"
        } else if (sector.flowAmount in 0.0..20.0) {
            "【后市建议】: 板块整体缩量整固，资金虽温和流入，但持续性有待观察。当前策略应以定投筑底或滚动做T为主，密切关注成交量是否能有效放大。"
        } else if (sector.flowAmount > -30) {
            "【后市建议】: 资金温和获利了结，回落至支撑位附近。短期内情绪面临降温，建议不急于抄底，等待大盘止跌和5日均线走平后再考虑分批试探性建仓。"
        } else {
            "【后市建议】: 板块出现严重的失血主力砸盘出逃，筹码出现多头踩踏。短期内情绪跌入冰点，切忌盲目抄底。持仓者应考虑借反抽逢高离场，等待市场出清和筹码充分沉淀后重新评估。"
        }

        return """
            ⚠️ [提示: 未检测到系统配置的有效 GEMINI_API_KEY 密钥，以下为本地研报算法引擎生成的分析报告]
            
            【资金面剖析】: 
            今日【${sector.name}】板块主力资金$flowText。核心个股中，${sector.leadingStocks.firstOrNull()?.name ?: "龙头"}表现相对突出。整体来看，当前该板块的主力大单与中单散户博弈激烈，呈现资金阶段性分配特征。
            
            【趋势与拐点】: 
            结合5日主力资金历史走势来看，当前板块资金趋势表现为“$trendText”。短期技术面与资金筹码面产生较强共振，面临阻力位抛压或下方均线强支撑的抉择期。
            
            $recommend
        """.trimIndent()
    }
}
