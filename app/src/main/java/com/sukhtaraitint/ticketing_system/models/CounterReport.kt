package com.sukhtaraitint.ticketing_system.models

data class CounterReport(val id: Int? = null, val name: String? = null, var total_ticket_sold_count : Int, var total_ticket_sold_price: Double){}
