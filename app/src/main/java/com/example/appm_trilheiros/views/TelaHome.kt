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
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaHome(
    onLogout: () -> Unit,
    itemDao: ItemDao
) {
    var items by remember { mutableStateOf(listOf<Item>()) }
    var selectedItem by remember { mutableStateOf<Item?>(null) }
    var descricao by remember { mutableStateOf("") }

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Adicione ou edite um item:", style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(16.dp))

        // Botão para inserir um novo item
        Button(
            onClick = {
                if (descricao.isNotEmpty() && selectedItem == null) {
                    val userId = FirebaseAuth.getInstance().currentUser?.uid // Obtém o ID do usuário atual
                    if (userId != null) { // Verifica se o userId não é nulo
                        val newItem = Item(descricao = descricao, userId = userId) // Adiciona o userId
                        coroutineScope.launch {
                            itemDao.gravar(newItem)
                        }
                        descricao = "" // Limpa o campo de descrição
                    } else {
                        // Aqui você pode adicionar uma mensagem de erro informando que o usuário não está autenticado
                    }
                }
            }
        ) {
            Text("Inserir")
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Botão para excluir o item selecionado
        Button(
            onClick = {
                selectedItem?.let { item ->
                    coroutineScope.launch {
                        itemDao.excluir(item)
                    }
                    selectedItem = null
                    descricao = "" // Limpa o campo de descrição
                }
            }
        ) {
            Text("Excluir")
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Botão para editar o item selecionado
        Button(
            onClick = {
                selectedItem?.let { item ->
                    if (descricao.isNotEmpty()) {
                        val updatedItem = item.copy(descricao = descricao) // Atualiza a descrição do item
                        coroutineScope.launch {
                            itemDao.gravar(updatedItem) // Gravar o item atualizado
                        }
                        descricao = "" // Limpa o campo de descrição
                        selectedItem = null
                    }
                }
            }
        ) {
            Text("Editar")
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Botão para sair da tela
        Button(onClick = { onLogout() }) {
            Text("Sair")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Campo de texto para a descrição do item
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

        // Exibe o item selecionado
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
                            selectedItem = item // Seleciona o item
                            descricao = item.descricao // Atualiza o campo de descrição
                        }
                        .padding(8.dp)
                ) {
                    Text(item.descricao, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}
