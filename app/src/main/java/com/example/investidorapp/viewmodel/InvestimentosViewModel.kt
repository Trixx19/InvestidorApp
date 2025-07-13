package com.example.investidorapp.viewmodel

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.investidorapp.MainActivity
import com.example.investidorapp.R
import com.example.investidorapp.model.Investimento
import com.google.firebase.database.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class InvestimentosViewModel(application: Application) : AndroidViewModel(application) {
    private val database = FirebaseDatabase.getInstance().reference.child("investimentos")
    private val _investimentos = MutableStateFlow<List<Investimento>>(emptyList())
    val investimentos: StateFlow<List<Investimento>> = _investimentos

    private val _inAppNotification = MutableStateFlow<String?>(null)
    val inAppNotification: StateFlow<String?> = _inAppNotification.asStateFlow()
    private var notificationJob: Job? = null

    init {
        monitorarAlteracoes()
    }

    private fun monitorarAlteracoes() {
        database.addChildEventListener(object : ChildEventListener {
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val nome = snapshot.child("nome").getValue(String::class.java) ?: "Desconhecido"
                val valor = snapshot.child("valor").getValue(Double::class.java) ?: 0.0
                Log.d("FirebaseData", "Investimento atualizado: $nome - R$ $valor")

                val notificationMessage = "$nome agora vale R$ $valor"

                showInAppNotification(notificationMessage)

                // Envia a notificação do sistema
                enviarNotificacao("Investimento Atualizado", notificationMessage)

                carregarInvestimentos()
            }
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) { carregarInvestimentos() }
            override fun onChildRemoved(snapshot: DataSnapshot) { carregarInvestimentos() }
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) { Log.e("FirebaseError", "Erro: ${error.message}") }
        })
    }

    private fun carregarInvestimentos() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lista = mutableListOf<Investimento>()
                for (item in snapshot.children) {
                    val nome = item.child("nome").getValue(String::class.java) ?: "Desconhecido"
                    val valor = item.child("valor").getValue(Double::class.java) ?: 0.0
                    lista.add(Investimento(nome, valor))
                }
                _investimentos.value = lista
            }
            override fun onCancelled(error: DatabaseError) { Log.e("FirebaseError", "Erro: ${error.message}") }
        })
    }

    private fun showInAppNotification(message: String) {
        notificationJob?.cancel() // Cancela a notificação anterior, se houver
        notificationJob = viewModelScope.launch {
            _inAppNotification.value = message
            delay(4000) //
            _inAppNotification.value = null
        }
    }

    private fun enviarNotificacao (titulo : String , mensagem : String ) {
        val channelId = "investimentos_notifications"
        val notificationId = (System .currentTimeMillis() % 10000 ).toInt()
        if (Build .VERSION .SDK_INT >= Build .VERSION_CODES .O) {
            val channel = NotificationChannel(
                channelId ,
                "Notificações de Investimentos" ,
                NotificationManager .IMPORTANCE_HIGH
            )
            val notificationManager =
                getApplication< Application >().getSystemService( Context .NOTIFICATION_SERVICE) as NotificationManager
            notificationManager .createNotificationChannel( channel )
        }
        val intent = Intent(getApplication(), MainActivity ::class .java).apply {
            flags = Intent .FLAG_ACTIVITY_NEW_TASK or Intent .FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent .getActivity(
            getApplication(),
            0,
            intent ,
            PendingIntent .FLAG_ONE_SHOT or PendingIntent .FLAG_IMMUTABLE
        )
        val notification = NotificationCompat .Builder(getApplication(), channelId )
            .setSmallIcon( R.drawable .icon_investapp)
            .setContentTitle( titulo )
            .setContentText( mensagem )
            .setPriority( NotificationCompat .PRIORITY_HIGH)
            .setContentIntent( pendingIntent )
            .setAutoCancel( true)
            .build()
        NotificationManagerCompat .from(getApplication()).notify( notificationId , notification )
    }
}