package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.GeminiManager
import com.example.data.Sector
import com.example.data.SectorDataProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class SortOrder {
    NET_FLOW_HIGH_TO_LOW,
    NET_FLOW_LOW_TO_HIGH
}

class StockFlowViewModel : ViewModel() {

    private val geminiManager = GeminiManager()

    // Base sector list from data provider
    private val _rawSectors = MutableStateFlow<List<Sector>>(emptyList())
    val rawSectors: StateFlow<List<Sector>> = _rawSectors.asStateFlow()

    // Filter and search states
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _sortOrder = MutableStateFlow(SortOrder.NET_FLOW_HIGH_TO_LOW)
    val sortOrder: StateFlow<SortOrder> = _sortOrder.asStateFlow()

    // Real-time Simulation State
    private val _isSimulating = MutableStateFlow(false)
    val isSimulating: StateFlow<Boolean> = _isSimulating.asStateFlow()
    private var simulationJob: Job? = null

    // Selected Sector for Drill Down details
    private val _selectedSector = MutableStateFlow<Sector?>(null)
    val selectedSector: StateFlow<Sector?> = _selectedSector.asStateFlow()

    // AI Analysis States
    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    private val _analysisResult = MutableStateFlow<String?>(null)
    val analysisResult: StateFlow<String?> = _analysisResult.asStateFlow()

    private var liveDataJob: Job? = null

    init {
        // Load initial data matching the exact state from the user's uploaded image
        _rawSectors.value = SectorDataProvider.getInitialSectors()
        startLivePolling()
    }

    private fun startLivePolling() {
        liveDataJob?.cancel()
        liveDataJob = viewModelScope.launch {
            while (true) {
                try {
                    val response = com.example.data.EastmoneyRetrofitClient.service.getSectorFundFlow()
                    val diff = response.data?.diff
                    if (!diff.isNullOrEmpty()) {
                        val merged = mergeRealSectors(_rawSectors.value.ifEmpty { SectorDataProvider.getInitialSectors() }, diff)
                        _rawSectors.value = merged
                        
                        // If a sector is currently selected, keep its details in sync with the live data
                        val selected = _selectedSector.value
                        if (selected != null) {
                            val updated = _rawSectors.value.find { it.id == selected.id }
                            if (updated != null) {
                                _selectedSector.value = updated
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                delay(15000) // Poll real-time data every 15 seconds
            }
        }
    }

    private fun stopLivePolling() {
        liveDataJob?.cancel()
        liveDataJob = null
    }

    private fun mergeRealSectors(localSectors: List<Sector>, realSectors: List<com.example.data.EastmoneySectorItem>): List<Sector> {
        return localSectors.map { local ->
            val lName = local.name
            val mappedName = when (lName) {
                "证券II" -> "证券"
                "AI应用" -> "软件开发"
                "锂电池概念" -> "电池"
                "CPO概念" -> "通信设备"
                "算力概念" -> "计算机设备"
                "商业航天" -> "航天航空"
                "创新药" -> "化学制药"
                "人形机器人" -> "通用设备"
                "玻璃基板" -> "玻璃玻纤"
                "黄金" -> "贵金属"
                "白酒" -> "酿酒行业"
                "稀土" -> "小金属"
                "元件" -> "电子元件"
                "有色金属" -> "有色金属"
                "MLCC" -> "电子元件"
                "电网设备" -> "电网设备"
                "半导体" -> "半导体"
                "消费电子" -> "消费电子"
                "存储芯片" -> "半导体"
                "光学光电子" -> "光学光电子"
                "电力设备" -> "光伏设备"
                "PCB" -> "电子元件"
                "通信技术" -> "通信服务"
                else -> lName
            }

            val matchedReal = realSectors.find { real ->
                val rName = real.name ?: ""
                rName == mappedName || rName.contains(mappedName) || mappedName.contains(rName)
            }

            if (matchedReal != null) {
                val realFlowYuan = matchedReal.netFlow ?: 0.0
                val realFlowAmount100M = realFlowYuan / 100000000.0 // Yuan to 亿
                val roundedFlow = Math.round(realFlowAmount100M * 100.0) / 100.0

                // Update today's flow in history
                val updatedHistory = local.historyFlows.toMutableList()
                if (updatedHistory.isNotEmpty()) {
                    updatedHistory[updatedHistory.lastIndex] = roundedFlow
                }

                // Update leading stocks based on live info if available
                val updatedStocks = local.leadingStocks.toMutableList()
                matchedReal.leadingStockName?.let { lsName ->
                    if (updatedStocks.isNotEmpty() && lsName.isNotBlank()) {
                        val firstStock = updatedStocks[0]
                        val realStockChangeStr = matchedReal.leadingStockChange?.let {
                            val sign = if (it >= 0) "+" else ""
                            "$sign${String.format("%.2f", it)}%"
                        } ?: firstStock.changePercent

                        updatedStocks[0] = firstStock.copy(
                            name = lsName,
                            changePercent = realStockChangeStr,
                            flowAmount = Math.round((roundedFlow * 0.35) * 100.0) / 100.0 // estimate leading stock flow
                        )
                    }
                }

                // Estimate/scale other stocks in the list to be proportional to today's real sector flow
                for (i in 1 until updatedStocks.size) {
                    val stock = updatedStocks[i]
                    val factor = 1.0 - (i * 0.2)
                    val stockFlow = Math.round((roundedFlow * 0.15 * factor) * 100.0) / 100.0
                    val isPositive = stockFlow >= 0
                    val sign = if (isPositive) "+" else ""
                    val currentPctVal = stock.changePercent.replace("%", "").replace("+", "").toDoubleOrNull() ?: 1.0
                    val updatedPct = if (isPositive) Math.abs(currentPctVal) else -Math.abs(currentPctVal)
                    updatedStocks[i] = stock.copy(
                        flowAmount = stockFlow,
                        changePercent = "$sign${String.format("%.2f", updatedPct)}%"
                    )
                }

                local.copy(
                    flowAmount = roundedFlow,
                    historyFlows = updatedHistory,
                    leadingStocks = updatedStocks
                )
            } else {
                local
            }
        }
    }

    // Combined sectors for display (applying filters, search, and sorting)
    val displaySectors: StateFlow<List<Sector>> = combine(
        _rawSectors,
        _searchQuery,
        _sortOrder
    ) { raw, query, sort ->
        var list = if (query.isBlank()) {
            raw
        } else {
            raw.filter { it.name.contains(query, ignoreCase = true) }
        }

        list = when (sort) {
            SortOrder.NET_FLOW_HIGH_TO_LOW -> list.sortedByDescending { it.flowAmount }
            SortOrder.NET_FLOW_LOW_TO_HIGH -> list.sortedBy { it.flowAmount }
        }
        list
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Summary insights computed on-the-fly
    val totalInflowSectorsCount: StateFlow<Int> = _rawSectors.combine(MutableStateFlow(0)) { raw, _ ->
        raw.count { it.flowAmount > 0 }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalOutflowSectorsCount: StateFlow<Int> = _rawSectors.combine(MutableStateFlow(0)) { raw, _ ->
        raw.count { it.flowAmount <= 0 }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val topInflowSector: StateFlow<Sector?> = _rawSectors.combine(MutableStateFlow(0)) { raw, _ ->
        raw.maxByOrNull { it.flowAmount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val topOutflowSector: StateFlow<Sector?> = _rawSectors.combine(MutableStateFlow(0)) { raw, _ ->
        raw.minByOrNull { it.flowAmount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSortOrder(order: SortOrder) {
        _sortOrder.value = order
    }

    fun selectSector(sector: Sector?) {
        _selectedSector.value = sector
        _analysisResult.value = null
        _isAnalyzing.value = false
    }

    fun toggleSimulation() {
        val newState = !_isSimulating.value
        _isSimulating.value = newState
        if (newState) {
            stopLivePolling()
            startSimulation()
        } else {
            stopSimulation()
            startLivePolling()
        }
    }

    private fun startSimulation() {
        simulationJob?.cancel()
        simulationJob = viewModelScope.launch {
            while (true) {
                delay(3000)
                _rawSectors.value = SectorDataProvider.simulateDataUpdates(_rawSectors.value)
                
                // If a sector is currently selected, keep its details in sync with the simulated live data
                val selected = _selectedSector.value
                if (selected != null) {
                    val updated = _rawSectors.value.find { it.id == selected.id }
                    if (updated != null) {
                        _selectedSector.value = updated
                    }
                }
            }
        }
    }

    private fun stopSimulation() {
        simulationJob?.cancel()
        simulationJob = null
    }

    fun requestAiAnalysis(sector: Sector) {
        viewModelScope.launch {
            _isAnalyzing.value = true
            _analysisResult.value = null
            try {
                val result = geminiManager.analyzeSectorFlow(sector)
                _analysisResult.value = result
            } catch (e: Exception) {
                _analysisResult.value = "研报分析生成失败：${e.localizedMessage}"
            } finally {
                _isAnalyzing.value = false
            }
        }
    }

    fun resetData() {
        stopSimulation()
        stopLivePolling()
        _isSimulating.value = false
        _rawSectors.value = SectorDataProvider.getInitialSectors()
        _selectedSector.value = null
        _analysisResult.value = null
        _isAnalyzing.value = false
        _searchQuery.value = ""
        _sortOrder.value = SortOrder.NET_FLOW_HIGH_TO_LOW
        startLivePolling()
    }

    override fun onCleared() {
        super.onCleared()
        stopSimulation()
        stopLivePolling()
    }
}
