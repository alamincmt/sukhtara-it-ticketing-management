package com.sukhtaraitint.ticketing_system.models

data class TicketSold(val id: Int = -1, val group_counter_id: Int? = -1, val from_counter_id: String? = null, val to_counter_id: String? = null, val price_total: String? = "0", val total_tickets: Int? = null, val date_time: Long? = null, val sold_by_counter_id: String? = null, ){
}
