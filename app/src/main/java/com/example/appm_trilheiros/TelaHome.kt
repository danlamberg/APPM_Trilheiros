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
import com.example.appm_trilheiros.dados.Item
import com.example.appm_trilheiros.dados.ItemDao
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaHome(
    onLogout: () -> Unit,
    itemDao: ItemDao
) {
    // Estado para armazenar a lista de itens do banco de dados
    var items by remember { mutableStateOf(listOf<Item>()) }
    var selectedItem by remember { mutableStateOf<Item?>(null) }
    var descricao by remember { mutableStateOf("") }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Obtenha o contexto
    val context = LocalContext.current

    // Coletar itens do banco de dados usando Flow
    LaunchedEffect(Unit) {
        // Coleta o fluxo de itens e atualiza o estado da lista
        itemDao.listarFlow().collect { itemList ->
            items = itemList
        }
    }

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
                if (descricao.isNotEmpty() && selectedItem == null) {
                    val newItem = Item(descricao = descricao)
                    coroutineScope.launch {
                        itemDao.gravar(newItem) // Insere o novo item no banco de dados
                    }
                    descricao = "" // Limpa o campo de entrada
                }
            }
        ) {
            Text("Inserir")
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Botão de Excluir
        Button(
            onClick = {
                selectedItem?.let { item ->
                    coroutineScope.launch {
                        itemDao.excluir(item) // Remove o item do banco de dados
                    }
                    selectedItem = null
                    descricao = ""
                }
            }
        ) {
            Text("Excluir")
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Botão de Editar
        Button(
            onClick = {
                selectedItem?.let { item ->
                    if (descricao.isNotEmpty()) {
                        val updatedItem = item.copy(descricao = descricao)
                        coroutineScope.launch {
                            itemDao.gravar(updatedItem) // Atualiza o item no banco de dados (Upsert)
                        }
                        descricao = ""
                        selectedItem = null
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

        // Campo de texto para entrada da descrição do item
        TextField(
            value = descricao,
            onValueChange = { descricao = it },
            label = { Text("Descrição do Item") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Botão para compartilhar a lista de itens
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
            }
        ) {
            Text("Compartilhar Lista")
        }

        Spacer(modifier = Modifier.height(16.dp))

        selectedItem?.let {
            Text("Item selecionado: ${it.descricao}", style = MaterialTheme.typography.bodyMedium)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Lista de itens com gerenciamento de rolagem
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
