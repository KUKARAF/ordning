package com.rafael.ordnung.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafael.ordnung.domain.model.Ticket
import com.rafael.ordnung.domain.usecase.ProcessTicketUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TicketListUiState(
    val isLoading: Boolean = false,
    val tickets: List<Ticket> = emptyList(),
    val stats: TicketStats? = null,
    val message: String? = null
)

data class TicketStats(
    val total: Int,
    val processed: Int,
    val failed: Int
)

@HiltViewModel
class TicketListViewModel @Inject constructor(
    private val processTicketUseCase: ProcessTicketUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(TicketListUiState())
    val uiState: StateFlow<TicketListUiState> = _uiState.asStateFlow()
    
    init {
        loadTickets()
    }
    
    fun loadTickets() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                processTicketUseCase.getAllTickets().collect { tickets ->
                    val stats = TicketStats(
                        total = tickets.size,
                        processed = tickets.count { it.isProcessed },
                        failed = tickets.count { !it.isProcessed }
                    )
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        tickets = tickets,
                        stats = stats
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    message = "Failed to load tickets: ${e.message}"
                )
            }
        }
    }
    
    fun processTicket(uri: android.net.Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val result = processTicketUseCase.processTicket(uri)
                if (result.isSuccess) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        message = "Ticket processed successfully"
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        message = "Failed to process ticket: ${result.exceptionOrNull()?.message}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    message = "Failed to process ticket: ${e.message}"
                )
            }
        }
    }
    
    fun deleteTicket(ticketId: Long) {
        viewModelScope.launch {
            try {
                processTicketUseCase.deleteTicket(ticketId)
                _uiState.value = _uiState.value.copy(
                    message = "Ticket deleted successfully"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    message = "Failed to delete ticket: ${e.message}"
                )
            }
        }
    }
    
    fun reprocessTicket(ticketId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val result = processTicketUseCase.reprocessTicket(ticketId)
                if (result.isSuccess) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        message = "Ticket reprocessed successfully"
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        message = "Failed to reprocess ticket: ${result.exceptionOrNull()?.message}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    message = "Failed to reprocess ticket: ${e.message}"
                )
            }
        }
    }
    
    fun refreshTickets() {
        loadTickets()
    }
    
    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }
}