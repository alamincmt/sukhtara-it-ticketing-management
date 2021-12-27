package com.sukhtaraitint.ticketing_system.utils

import com.sukhtaraitint.ticketing_system.models.Counters
import com.sukhtaraitint.ticketing_system.models.TotalTicketSoldReport

object ConstantValues {

    const val DB_URL = "https://sukhtara-it-default-rtdb.asia-southeast1.firebasedatabase.app/"
    var counterList: List<Counters>? = null
    var ticketSoldReportList: List<TotalTicketSoldReport>? = null
}