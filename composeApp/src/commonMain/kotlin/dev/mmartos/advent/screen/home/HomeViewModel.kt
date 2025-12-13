package dev.mmartos.advent.screen.home

import androidx.lifecycle.ViewModel
import dev.mmartos.advent.models.DayDetails

class HomeViewModel : ViewModel() {
    fun getDayDetails(day: Int): DayDetails =
        when (day) {
            1 -> DayDetails(day, "Day 1: Secret Entrance")
            2 -> DayDetails(day, "Day 2: Gift Shop")
            3 -> DayDetails(day, "Day 3: Lobby")
            4 -> DayDetails(day, "Day 4: Printing Department")
            5 -> DayDetails(day, "Day 5: Cafeteria")
            6 -> DayDetails(day, "Day 6: Trash Compactor")
            7 -> DayDetails(day, "Day 7: Laboratories")
            8 -> DayDetails(day, "Day 8: Playground")
            9 -> DayDetails(day, "Day 9: Movie Theater")
            10 -> DayDetails(day, "Day 10: Factory")
            11 -> DayDetails(day, "Day 11: Reactor")
            12 -> DayDetails(day, "Day 12: Christmas Tree Farm")
            else -> error("Not ready yet")
        }
}