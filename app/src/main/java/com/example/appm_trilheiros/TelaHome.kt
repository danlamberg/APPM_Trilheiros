package com.example.appm_trilheiros

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.appm_trilheiros.model.Produto

@Preview
@Composable
fun TelaHome(onLogout: () -> Unit) {
    // Exemplo de lista de produtos
    val produtos = remember { mutableStateListOf<Produto>() }
    var selectedProduto by remember { mutableStateOf<Produto?>(null) }
    var modelo by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Bem-vindo à Tela Inicial!")

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(produtos) { produto ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedProduto = produto }
                        .padding(8.dp)
                ) {
                    Text(produto.modelo)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = modelo,
            onValueChange = { modelo = it },
            label = { Text("Modelo") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = {
            if (modelo.isNotEmpty()) {
                produtos.add(Produto(produtos.size + 1, modelo))
                modelo = ""
            }
        }) {
            Text("Inserir")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = {
            selectedProduto?.let { produto ->
                produtos.remove(produto)
                selectedProduto = null
            }
        }) {
            Text("Excluir")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = {
            onLogout()
        }) {
            Text("Sair")
        }
    }
}
