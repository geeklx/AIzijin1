package com.example.data

import kotlin.random.Random

data class Stock(
    val name: String,
    val price: String,
    val changePercent: String,
    val flowAmount: Double // in 100M (亿)
)

data class Sector(
    val id: Int,
    val name: String,
    val flowAmount: Double, // in 100M (亿)
    val leadingStocks: List<Stock>,
    val description: String,
    val historyFlows: List<Double> // last 5 days flow history in 100M
) {
    val progress: Float
        get() = (flowAmount / 264.04).coerceIn(-1.0, 1.0).toFloat()
}

object SectorDataProvider {
    fun getInitialSectors(): List<Sector> {
        return listOf(
            Sector(
                id = 1,
                name = "证券II",
                flowAmount = 57.73,
                description = "非银金融子板块，主要受市场交投活跃度、印花税及两融业务影响，作为行情风向标，资金大幅流入通常意味着牛市预期或政策利好释出。",
                leadingStocks = listOf(
                    Stock("中信证券", "22.50", "+4.85%", 18.52),
                    Stock("东方财富", "16.88", "+7.42%", 15.30),
                    Stock("中信建投", "24.62", "+3.95%", 10.15),
                    Stock("海通证券", "8.92", "+3.10%", 8.42)
                ),
                historyFlows = listOf(12.5, -8.2, 24.1, 41.5, 57.73)
            ),
            Sector(
                id = 2,
                name = "AI应用",
                flowAmount = 37.39,
                description = "人工智能及软件应用开发。大模型加速落地及端侧AI应用爆发，推动下游软件开发及垂直应用场景企业资金大幅流入。",
                leadingStocks = listOf(
                    Stock("科大讯飞", "42.80", "+5.61%", 12.45),
                    Stock("金山办公", "265.50", "+6.12%", 10.82),
                    Stock("昆仑万维", "36.42", "+8.95%", 8.61),
                    Stock("三六零", "8.75", "+4.20%", 5.51)
                ),
                historyFlows = listOf(-15.2, 10.4, 18.5, -5.2, 37.39)
            ),
            Sector(
                id = 3,
                name = "化工",
                flowAmount = 12.69,
                description = "基础化工及精细化工。受近期部分化工品价格触底反弹（如制冷剂、磷化工等）及出口需求增加拉动，业绩确定性增强。",
                leadingStocks = listOf(
                    Stock("万华化学", "85.40", "+1.85%", 4.30),
                    Stock("盐湖股份", "17.15", "+2.40%", 3.25),
                    Stock("华鲁恒升", "28.52", "+1.95%", 2.82),
                    Stock("荣盛石化", "10.45", "+0.92%", 2.32)
                ),
                historyFlows = listOf(5.4, 8.2, -2.1, 11.2, 12.69)
            ),
            Sector(
                id = 4,
                name = "养殖业",
                flowAmount = 8.19,
                description = "生猪及禽类养殖。猪周期底部盘整，近期猪价阶段性反弹，行业去产能化效果显现，资金博弈周期拐点意愿强烈。",
                leadingStocks = listOf(
                    Stock("牧原股份", "44.60", "+2.51%", 3.62),
                    Stock("温氏股份", "19.82", "+1.85%", 2.45),
                    Stock("新希望", "9.52", "+0.80%", 1.22),
                    Stock("圣农发展", "16.15", "+1.10%", 0.90)
                ),
                historyFlows = listOf(-8.5, -4.2, 3.1, 6.5, 8.19)
            ),
            Sector(
                id = 5,
                name = "煤炭",
                flowAmount = 1.98,
                description = "传统高股息红利板块。夏季用电高峰逼近带来电煤需求支撑，其高现金流、高分红属性在防守仓位中占有极高地位。",
                leadingStocks = listOf(
                    Stock("中国神华", "41.20", "+0.85%", 0.95),
                    Stock("陕西煤业", "25.82", "+1.12%", 0.62),
                    Stock("兖矿能源", "18.45", "+0.50%", 0.31),
                    Stock("山西焦煤", "10.15", "+0.30%", 0.10)
                ),
                historyFlows = listOf(14.2, 10.5, 8.4, 4.2, 1.98)
            ),
            Sector(
                id = 6,
                name = "白酒",
                flowAmount = 1.89,
                description = "食品饮料权重龙头。在端午及中秋备货期间，高端白酒批价基本企稳，机构资金小幅回流估值底部的白酒龙头。",
                leadingStocks = listOf(
                    Stock("贵州茅台", "1485.00", "+0.45%", 0.85),
                    Stock("五粮液", "141.20", "+0.72%", 0.52),
                    Stock("泸州老窖", "152.40", "+0.95%", 0.35),
                    Stock("山西汾酒", "215.10", "+1.10%", 0.17)
                ),
                historyFlows = listOf(-22.4, -15.1, -4.2, -1.2, 1.89)
            ),
            Sector(
                id = 7,
                name = "黄金",
                flowAmount = -1.11,
                description = "贵金属及避险板块。近期美联储降息预期反复，国际金价高位震荡，避险资金阶段性获利了结，呈现资金小幅流出。",
                leadingStocks = listOf(
                    Stock("紫金矿业", "17.42", "-0.85%", -0.52),
                    Stock("山东黄金", "24.50", "-1.10%", -0.31),
                    Stock("中金黄金", "12.88", "-0.65%", -0.21),
                    Stock("赤峰黄金", "15.40", "-0.40%", -0.07)
                ),
                historyFlows = listOf(15.4, 18.2, 8.9, 1.2, -1.11)
            ),
            Sector(
                id = 8,
                name = "创新药",
                flowAmount = -1.81,
                description = "生物医药研发。受美联储利率政策高企预期及美法案不确定性影响，创新药出海逻辑受到情绪扰动，资金面有所承压。",
                leadingStocks = listOf(
                    Stock("恒瑞医药", "41.50", "-1.20%", -0.72),
                    Stock("药明康德", "43.82", "-2.40%", -0.61),
                    Stock("百济神州", "131.20", "-0.85%", -0.35),
                    Stock("信达生物", "38.15", "-1.10%", -0.13)
                ),
                historyFlows = listOf(4.2, -2.1, -8.4, -5.2, -1.81)
            ),
            Sector(
                id = 9,
                name = "人形机器人",
                flowAmount = -3.66,
                description = "智能机器人产业链。特斯拉Optimus进展仍受量产节点关注，前期估值炒作过高，当前处于技术落地空窗期的筹码回落阶段。",
                leadingStocks = listOf(
                    Stock("三花智控", "21.40", "-1.85%", -1.45),
                    Stock("拓普集团", "58.20", "-2.10%", -1.10),
                    Stock("绿的谐波", "110.50", "-3.40%", -0.82),
                    Stock("鸣志电器", "48.92", "-1.95%", -0.29)
                ),
                historyFlows = listOf(11.2, 5.4, -1.2, -4.5, -3.66)
            ),
            Sector(
                id = 10,
                name = "稀土",
                flowAmount = -6.69,
                description = "稀土永磁与战略资源。全球稀土供给预期增加及下游磁材需求偏弱导致稀土产品价格震荡寻底，资金处于流出观望期。",
                leadingStocks = listOf(
                    Stock("北方稀土", "16.82", "-1.50%", -2.85),
                    Stock("中国稀土", "24.15", "-2.10%", -2.10),
                    Stock("广晟有色", "28.40", "-1.85%", -1.12),
                    Stock("盛和资源", "9.42", "-0.95%", -0.62)
                ),
                historyFlows = listOf(-1.2, -2.4, -5.1, -4.8, -6.69)
            ),
            Sector(
                id = 11,
                name = "有色金属",
                flowAmount = -8.51,
                description = "铜、铝、锌等大宗工业金属。近期宏观经济复苏预期放缓，伦敦金属交易所铜价高位回落，行业盈利预期短期下调。",
                leadingStocks = listOf(
                    Stock("江西铜业", "24.50", "-1.95%", -3.12),
                    Stock("紫金矿业", "17.42", "-0.85%", -2.45),
                    Stock("中国铝业", "7.12", "-2.15%", -1.82),
                    Stock("云南铜业", "12.80", "-2.40%", -1.12)
                ),
                historyFlows = listOf(14.5, 8.2, -3.1, -5.4, -8.51)
            ),
            Sector(
                id = 12,
                name = "MLCC",
                flowAmount = -13.94,
                description = "片式多层陶瓷电容器。消费电子复苏尚显温和，中低端MLCC产能过剩价格竞争激烈，行业高端化转型仍需时间，资金退潮明显。",
                leadingStocks = listOf(
                    Stock("三环集团", "28.50", "-2.10%", -5.10),
                    Stock("风华高科", "12.42", "-3.05%", -4.20),
                    Stock("火炬电子", "23.15", "-2.45%", -2.82),
                    Stock("鸿远电子", "34.20", "-1.85%", -1.82)
                ),
                historyFlows = listOf(-4.2, -8.5, -10.1, -12.4, -13.94)
            ),
            Sector(
                id = 13,
                name = "电网设备",
                flowAmount = -17.32,
                description = "特高压及电网改造升级。前期特高压特许权招标带来的估值溢价被消化，部分主力大资金在年中节点进行战术性调仓。",
                leadingStocks = listOf(
                    Stock("国电南瑞", "24.15", "-1.82%", -6.50),
                    Stock("特变电工", "14.20", "-1.45%", -4.82),
                    Stock("许继电气", "26.85", "-2.95%", -3.50),
                    Stock("平高电气", "17.40", "-2.10%", -2.50)
                ),
                historyFlows = listOf(18.2, 12.4, 5.1, -8.4, -17.32)
            ),
            Sector(
                id = 14,
                name = "半导体",
                flowAmount = -31.60,
                description = "芯片及半导体封测。虽然晶圆代工产能利用率有所回升，但成熟制程降价压力大，行业整体周期复苏慢于预期，导致部分机构资金出逃。",
                leadingStocks = listOf(
                    Stock("中芯国际", "45.82", "-2.15%", -10.50),
                    Stock("北方华创", "312.40", "-1.80%", -8.42),
                    Stock("韦尔股份", "95.50", "-2.45%", -7.15),
                    Stock("海光信息", "78.92", "-3.10%", -5.53)
                ),
                historyFlows = listOf(22.1, 15.4, -10.5, -21.4, -31.60)
            ),
            Sector(
                id = 15,
                name = "消费电子",
                flowAmount = -40.77,
                description = "智能手机、PC及可穿戴设备。尽管AI手机概念频出，但整体终端换机周期拉长，三季度出货量预期偏温和，部分博弈资金阶段性撤离。",
                leadingStocks = listOf(
                    Stock("立讯精密", "34.15", "-3.10%", -15.42),
                    Stock("工业富联", "22.50", "-2.40%", -12.10),
                    Stock("歌尔股份", "17.82", "-3.95%", -8.50),
                    Stock("领益智造", "5.42", "-4.10%", -4.75)
                ),
                historyFlows = listOf(8.5, -12.4, -22.1, -31.5, -40.77)
            ),
            Sector(
                id = 16,
                name = "锂电池概念",
                flowAmount = -52.55,
                description = "新能源车电池及材料。行业产能严重过剩，碳酸锂价格近期再次跌破前期支撑位，各大电池及中游正极材料企业利润空间遭压缩。",
                leadingStocks = listOf(
                    Stock("宁德时代", "182.50", "-2.51%", -20.45),
                    Stock("亿纬锂能", "36.80", "-3.12%", -14.10),
                    Stock("天齐锂业", "32.15", "-4.20%", -10.20),
                    Stock("赣锋锂业", "30.40", "-4.55%", -7.80)
                ),
                historyFlows = listOf(-12.4, -28.5, -35.2, -45.1, -52.55)
            ),
            Sector(
                id = 17,
                name = "元件",
                flowAmount = -53.71,
                description = "基础电子元件与PCB载板。受出口订单增速放缓及半导体设备砍单传闻情绪打压，主力资金加速了结前期反弹获利筹码。",
                leadingStocks = listOf(
                    Stock("沪电股份", "32.15", "-3.80%", -18.20),
                    Stock("生益科技", "16.40", "-2.95%", -14.50),
                    Stock("深南电路", "82.50", "-3.40%", -12.10),
                    Stock("兴森科技", "9.12", "-4.15%", -8.91)
                ),
                historyFlows = listOf(-5.2, -18.4, -29.5, -42.1, -53.71)
            ),
            Sector(
                id = 18,
                name = "存储芯片",
                flowAmount = -55.89,
                description = "Flash与DRAM。虽然DDR5与HBM需求高企，但利基型存储（Nor Flash）价格依然承压，板块前期累计涨幅巨大，引发本轮集中获利盘回吐。",
                leadingStocks = listOf(
                    Stock("兆易创新", "72.40", "-3.95%", -18.50),
                    Stock("澜起科技", "51.20", "-2.80%", -15.12),
                    Stock("北京君正", "56.45", "-3.40%", -12.15),
                    Stock("江波龙", "78.20", "-4.20%", -10.12)
                ),
                historyFlows = listOf(18.4, 2.5, -15.4, -35.2, -55.89)
            ),
            Sector(
                id = 19,
                name = "商业航天",
                flowAmount = -57.47,
                description = "卫星互联网及火箭制造。属于高风险偏好概念板块。由于前期利好出尽，且无实质业绩支撑，高位妖股开始杀跌，资金踩踏出逃明显。",
                leadingStocks = listOf(
                    Stock("航天电子", "7.82", "-4.51%", -20.15),
                    Stock("航天动力", "9.15", "-5.82%", -15.42),
                    Stock("中国卫星", "22.40", "-3.90%", -12.10),
                    Stock("中科星图", "36.50", "-2.85%", -9.80)
                ),
                historyFlows = listOf(45.2, 22.4, -12.5, -38.4, -57.47)
            ),
            Sector(
                id = 20,
                name = "光学光电子",
                flowAmount = -68.24,
                description = "显示面板与红外滤光片。大尺寸电视液晶面板涨价趋势暂缓，下游消费显示采购动能不足，主力资金集中在半年报披露前进行调仓锁定收益。",
                leadingStocks = listOf(
                    Stock("京东方A", "3.85", "-2.53%", -25.40),
                    Stock("TCL科技", "4.12", "-2.82%", -20.12),
                    Stock("水晶光电", "14.50", "-3.90%", -15.30),
                    Stock("歌尔微", "21.40", "-4.50%", -7.42)
                ),
                historyFlows = listOf(-12.4, -26.5, -41.2, -55.4, -68.24)
            ),
            Sector(
                id = 21,
                name = "电力设备",
                flowAmount = -75.41,
                description = "光伏及风电设备。产能错配引发的严重内卷，组件价格持续在现金成本线以下徘徊。虽然有政策层面限制低端扩张，但实质产能出清仍需周期。",
                leadingStocks = listOf(
                    Stock("隆基绿能", "16.42", "-3.92%", -25.50),
                    Stock("阳光电源", "84.50", "-2.85%", -20.82),
                    Stock("通威股份", "18.10", "-4.12%", -16.35),
                    Stock("晶澳科技", "12.45", "-4.80%", -12.74)
                ),
                historyFlows = listOf(-25.2, -41.5, -52.4, -63.1, -75.41)
            ),
            Sector(
                id = 22,
                name = "算力概念",
                flowAmount = -76.59,
                description = "AI算力租赁、服务器、算力芯片。受制于部分芯片供应收紧及企业资本开支阶段性放缓，高估值算力服务器在当前财报季面临业绩兑现考验。",
                leadingStocks = listOf(
                    Stock("中科曙光", "41.50", "-4.15%", -25.82),
                    Stock("浪潮信息", "32.40", "-3.80%", -22.45),
                    Stock("紫光股份", "18.92", "-3.10%", -18.20),
                    Stock("神州数码", "26.15", "-4.50%", -10.12)
                ),
                historyFlows = listOf(35.2, 12.4, -22.1, -50.4, -76.59)
            ),
            Sector(
                id = 23,
                name = "玻璃基板",
                flowAmount = -80.89,
                description = "先进封装基板及半导体特种玻璃。属于高新材料热门炒作概念，本轮下跌为前期暴涨后的估值快速回踩，投机资金短期离场明显。",
                leadingStocks = listOf(
                    Stock("沃格光电", "24.15", "-6.82%", -30.40),
                    Stock("雷曼光电", "7.12", "-8.15%", -22.50),
                    Stock("彩虹股份", "6.85", "-4.20%", -18.42),
                    Stock("凯盛科技", "11.12", "-5.10%", -9.57)
                ),
                historyFlows = listOf(82.4, 45.1, -12.4, -48.2, -80.89)
            ),
            Sector(
                id = 24,
                name = "PCB",
                flowAmount = -113.92,
                description = "印制电路板。尽管算力服务器PCB需求极其旺盛，但普通汽车和工业级PCB面临降价压力。估值水位已运行在历史高分位，大资金集中止盈。",
                leadingStocks = listOf(
                    Stock("沪电股份", "32.15", "-3.80%", -42.15),
                    Stock("东山精密", "16.82", "-5.12%", -35.40),
                    Stock("深南电路", "82.50", "-3.40%", -24.12),
                    Stock("景旺电子", "19.45", "-2.95%", -12.25)
                ),
                historyFlows = listOf(45.1, 12.4, -34.5, -78.4, -113.92)
            ),
            Sector(
                id = 25,
                name = "CPO概念",
                flowAmount = -173.48,
                description = "光电共封装技术，属于光模块子行业。作为本轮AI大行情的核心领涨板块，前期涨幅惊人。在宏观政策调控及筹码拥挤度过高压力下，机构大单持续流出，主力砸盘止盈。",
                leadingStocks = listOf(
                    Stock("中际旭创", "115.50", "-4.82%", -65.12),
                    Stock("新易盛", "78.42", "-5.10%", -50.40),
                    Stock("天孚通信", "102.15", "-3.95%", -40.12),
                    Stock("光迅科技", "26.80", "-4.15%", -17.84)
                ),
                historyFlows = listOf(125.4, 42.1, -55.2, -120.4, -173.48)
            ),
            Sector(
                id = 26,
                name = "通信技术",
                flowAmount = -264.04,
                description = "5G及光纤通信传输。作为行业尾部，在运营商资本开支下调及基建投资顶峰过后，由于缺乏短期爆发性热点支撑，全天承接盘极其薄弱，主力资金不计成本抛售，造成最大失血现象。",
                leadingStocks = listOf(
                    Stock("中兴通讯", "24.50", "-4.51%", -95.12),
                    Stock("中国移动", "98.15", "-1.20%", -70.40),
                    Stock("中国电信", "5.82", "-1.85%", -52.12),
                    Stock("中国联通", "4.45", "-2.10%", -46.40)
                ),
                historyFlows = listOf(18.2, -45.1, -112.4, -180.5, -264.04)
            )
        )
    }

    fun simulateDataUpdates(currentSectors: List<Sector>): List<Sector> {
        return currentSectors.map { sector ->
            // Simulate random realistic fluctuation within +/- 3% of original flow or max +/- 5.0 亿
            val change = Random.nextDouble(-3.0, 3.0)
            val newAmount = (sector.flowAmount + change).coerceIn(-500.0, 500.0)
            
            // Re-simulate stocks accordingly
            val updatedStocks = sector.leadingStocks.map { stock ->
                val stockChangePercent = Random.nextDouble(-1.5, 1.5)
                val currentPercentVal = stock.changePercent.replace("%", "").replace("+", "").toDoubleOrNull() ?: 0.0
                val newPercent = currentPercentVal + stockChangePercent
                val sign = if (newPercent >= 0) "+" else ""
                val percentStr = "$sign${String.format("%.2f", newPercent)}%"
                
                val currentPrice = stock.price.toDoubleOrNull() ?: 10.0
                val priceChange = currentPrice * (stockChangePercent / 100)
                val newPrice = String.format("%.2f", (currentPrice + priceChange).coerceAtLeast(0.01))
                
                val stockFlowChange = change * (0.1 + Random.nextDouble(0.1, 0.3))
                val newStockFlow = stock.flowAmount + stockFlowChange
                
                Stock(
                    name = stock.name,
                    price = newPrice,
                    changePercent = percentStr,
                    flowAmount = newStockFlow
                )
            }
            
            // Shift history flows slightly
            val newHistory = sector.historyFlows.toMutableList()
            if (newHistory.size >= 5) {
                newHistory.removeAt(0)
            }
            newHistory.add(newAmount)
            
            sector.copy(
                flowAmount = Math.round(newAmount * 100.0) / 100.0,
                leadingStocks = updatedStocks,
                historyFlows = newHistory
            )
        }
    }
}
