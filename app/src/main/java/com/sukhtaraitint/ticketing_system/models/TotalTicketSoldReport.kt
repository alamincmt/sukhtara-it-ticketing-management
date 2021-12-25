package com.sukhtaraitint.ticketing_system.models

data class TotalTicketSoldReport(val ticketCounterReport: HashMap<String, Int>? = null, val total_tickets: Int? = null, val date_time: Long? = null, val report_taken_by: String? = null){
}
