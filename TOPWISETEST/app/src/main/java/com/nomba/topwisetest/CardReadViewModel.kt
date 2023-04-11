package com.nomba.topwisetest

import androidx.compose.runtime.MutableState
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow

enum class CardReadStatus{
    INSERT_CARD,
    INPUT_PIN,
    RESULT
}

class CardReadViewModel: ViewModel() {
    val cardReadState = MutableStateFlow(CardReadStatus.INSERT_CARD)

    fun onEventTriggered(event: CardReadEvents){
        when(event){
            CardReadEvents.InputPin -> {
                cardReadState.value = CardReadStatus.INPUT_PIN
            }
            CardReadEvents.InsertCard -> {
                cardReadState.value = CardReadStatus.INSERT_CARD
            }
            CardReadEvents.Result -> {
                cardReadState.value = CardReadStatus.RESULT
            }
        }
    }

    sealed class CardReadEvents(){
        object InsertCard: CardReadEvents()
        object InputPin: CardReadEvents()
        object Result: CardReadEvents()
    }
}