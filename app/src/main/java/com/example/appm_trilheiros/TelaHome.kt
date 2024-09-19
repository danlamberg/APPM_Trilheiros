package com.example.appm_trilheiros

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.appm_trilheiros.model.Produto
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaHome(onLogout: () -> Unit) {

    val produtos = remember { mutableStateListOf<Produto>() }
    var selectedProduto by remember { mutableStateOf<Produto?>(null) }
    var modelo by remember { mutableStateOf("") }

    // Estado da lista para controlar a rolagem
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Adicione ou edite um item:", style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(16.dp))

        // Botão de Inserir
        Button(
            onClick = {
                if (modelo.isNotEmpty() && selectedProduto == null) {
                    produtos.add(Produto(produtos.size + 1, modelo))
                    modelo = ""

                    // Rolar até o último item adicionado
                    coroutineScope.launch {
                        listState.animateScrollToItem(produtos.size - 1)
                    }
                }
            }
        ) {
            Text("Inserir")
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Botão de Excluir
        Button(
            onClick = {
                selectedProduto?.let { produto ->
                    produtos.remove(produto)
                    selectedProduto = null
                    modelo = ""
                }
            }
        ) {
            Text("Excluir")
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Botão de Editar
        Button(
            onClick = {
                selectedProduto?.let { produto ->
                    val index = produtos.indexOf(produto)
                    if (index != -1 && modelo.isNotEmpty()) {
                        produtos[index] = produto.copy(modelo = modelo)
                        modelo = ""
                        selectedProduto = null
                    }
                }
            }
        ) {
            Text("Editar")
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Botão de Logout
        Button(onClick = { onLogout() }) {
            Text("Sair")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Campo de texto para entrada de produto
        TextField(
            value = modelo,
            onValueChange = { modelo = it },
            label = { Text("Item") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Mostrar o produto selecionado
        selectedProduto?.let {
            Text("Produto selecionado: ${it.modelo}", style = MaterialTheme.typography.bodyMedium)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Lista de produtos com gerenciamento de rolagem
        LazyColumn(state = listState) {
            items(produtos) { produto ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectedProduto = produto
                            modelo = produto.modelo
                        }
                        .padding(8.dp)
                ) {
                    Text(produto.modelo, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}
