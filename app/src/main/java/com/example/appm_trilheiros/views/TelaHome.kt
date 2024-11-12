package com.example.appm_trilheiros.views

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.appm_trilheiros.models.Item
import com.example.appm_trilheiros.models.ItemDao
import com.example.appm_trilheiros.viewmodels.ItensViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import android.util.Log

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaHome(
    onLogout: () -> Unit,
    itemDao: ItemDao,
    itensViewModel: ItensViewModel, // Adicione a referência ao ItensViewModel
    navController: NavController // NavController para navegação
) {
    var items by remember { mutableStateOf(listOf<Item>()) }
    var selectedItem by remember { mutableStateOf<Item?>(null) }
    var descricao by remember { mutableStateOf("") }
    var expandedActions by remember { mutableStateOf(false) } // Estado para controlar a visibilidade dos botões de ação
    var expandedProfileOptions by remember { mutableStateOf(false) } // Estado para controlar a visibilidade das opções de perfil

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // Coleta de itens do banco de dados para o usuário atual
    LaunchedEffect(Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            itemDao.listarFlowPorUsuario(userId).collect { itemList ->
                items = itemList
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Text("Adicione ou edite um item:", style = MaterialTheme.typography.titleLarge)

            Spacer(modifier = Modifier.height(16.dp))

            // Campo de texto para descrição do item
            TextField(
                value = descricao,
                onValueChange = { descricao = it },
                label = { Text("Descrição do Item") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Botão para alternar a exibição dos botões de ação
            Button(
                onClick = { expandedActions = !expandedActions },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (expandedActions) "Ocultar Ações" else "Mostrar Ações")
            }

            // Agrupando os botões de ações do item (Inserir, Excluir, Editar)
            if (expandedActions) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = {
                            if (descricao.isNotEmpty() && selectedItem == null) {
                                val userId = FirebaseAuth.getInstance().currentUser?.uid
                                if (userId != null) {
                                    val newItem = Item(descricao = descricao, userId = userId)
                                    itensViewModel.gravarItem(newItem) // Chama o método para gravar no Firebase
                                    descricao = ""
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Text("Inserir")
                    }

                    Button(
                        onClick = {
                            selectedItem?.let { item ->
                                // Chama a função da ViewModel para excluir o item
                                itensViewModel.excluirItem(item)
                                // Limpa a seleção e a descrição após a exclusão
                                selectedItem = null
                                descricao = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Text("Excluir")
                    }

                    Button(
                        onClick = {
                            selectedItem?.let { item ->
                                if (descricao.isNotEmpty()) {
                                    val updatedItem =
                                        item.copy(descricao = descricao) // Atualiza a descrição do item

                                    // Chama a função atualizarItem do ViewModel
                                    itensViewModel.atualizarItem(updatedItem)

                                    descricao = "" // Limpa o campo de descrição
                                    selectedItem = null // Desmarca o item selecionado
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Text("Editar")
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Botão para alternar a exibição das opções de perfil
            Button(
                onClick = { expandedProfileOptions = !expandedProfileOptions },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (expandedProfileOptions) "Ocultar Opções de Perfil" else "Mostrar Opções de Perfil")
            }

            // Opções de perfil (Editar Perfil, Compartilhar Lista, Sair)
            if (expandedProfileOptions) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = {
                            navController.navigate("editar_perfil") // Navegação para tela de edição de perfil
                        },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Text("Editar Perfil")
                    }

                    Button(
                        onClick = {
                            val itemListString = items.joinToString(separator = "\n") { it.descricao }
                            val shareIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, "*Lista de Itens de Montanha:*\n$itemListString")
                                type = "text/plain"
                            }
                            val chooser = Intent.createChooser(shareIntent, "Compartilhar via")
                            context.startActivity(chooser)
                        },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Text("Compartilhar Lista")
                    }

                    Button(
                        onClick = { onLogout() },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Text("Sair")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Mostra o item selecionado
            selectedItem?.let {
                Text("Item selecionado: ${it.descricao}", style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Lista de itens
            LazyColumn(state = listState) {
                items(items) { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedItem = item
                                descricao = item.descricao
                            }
                            .padding(8.dp)
                    ) {
                        Text(item.descricao, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
