package com.apesource.account.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.apesource.account.data.entity.Book
import com.apesource.account.ui.theme.*
import com.apesource.account.ui.viewmodel.AccountViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookEditScreen(
    onBack: () -> Unit,
    onAddBook: () -> Unit,
    viewModel: AccountViewModel = viewModel()
) {
    val books by viewModel.books.collectAsState(initial = emptyList())
    val currentBook by viewModel.currentBook.collectAsState()
    var selectedBook by remember { mutableStateOf<Book?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var bookToDelete by remember { mutableStateOf<Book?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("账本管理", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "当前账本",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = currentBook.name,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "周期起始日: 每月${currentBook.startDay}日",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }
                            IconButton(
                                onClick = {
                                    selectedBook = currentBook
                                    showEditDialog = true
                                }
                            ) {
                                Icon(
                                    Icons.Default.MoreVert,
                                    contentDescription = "更多",
                                    tint = TextSecondary
                                )
                            }
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "其他账本",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
            }

            if (books.filter { it.id != currentBook.id }.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(48.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.Book,
                                contentDescription = null,
                                tint = TextSecondary.copy(alpha = 0.5f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "暂无其他账本",
                                fontSize = 14.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
            } else {
                items(books.filter { it.id != currentBook.id }) { book ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = book.name,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "周期起始日: 每月${book.startDay}日",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }
                            Row {
                                IconButton(onClick = {
                                    selectedBook = book
                                    showEditDialog = true
                                }) {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = "编辑",
                                        tint = TextSecondary
                                    )
                                }
                                IconButton(onClick = {
                                    viewModel.selectBook(book)
                                }) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = "切换到此账本",
                                        tint = Primary
                                    )
                                }
                                IconButton(onClick = {
                                    bookToDelete = book
                                    showDeleteConfirmDialog = true
                                }) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "删除",
                                        tint = ExpenseColor
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onAddBook,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("添加账本")
                }
            }
        }
    }

    if (showEditDialog && selectedBook != null) {
        BookEditDialog(
            book = selectedBook!!,
            isNewBook = false,
            onDismiss = {
                showEditDialog = false
                selectedBook = null
            },
            onConfirm = { updatedBook ->
                viewModel.updateBook(updatedBook)
                showEditDialog = false
                selectedBook = null
            },
            onSwitchTo = {
                viewModel.selectBook(selectedBook!!)
                showEditDialog = false
                selectedBook = null
            },
            onDelete = {
                bookToDelete = selectedBook
                showEditDialog = false
                selectedBook = null
                showDeleteConfirmDialog = true
            }
        )
    }

    if (showDeleteConfirmDialog && bookToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteConfirmDialog = false
                bookToDelete = null
            },
            title = { Text("确认删除") },
            text = { Text("确定要删除账本\"${bookToDelete!!.name}\"吗？删除后无法恢复。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteBook(bookToDelete!!)
                        showDeleteConfirmDialog = false
                        bookToDelete = null
                    }
                ) {
                    Text("删除", color = ExpenseColor)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteConfirmDialog = false
                    bookToDelete = null
                }) {
                    Text("取消")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookEditDialog(
    book: Book,
    isNewBook: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (Book) -> Unit,
    onSwitchTo: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    var bookName by remember { mutableStateOf(book.name) }
    var startDay by remember { mutableStateOf(book.startDay) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isNewBook) "添加账本" else "编辑账本") },
        text = {
            Column {
                OutlinedTextField(
                    value = bookName,
                    onValueChange = { bookName = it },
                    label = { Text("账本名称") },
                    placeholder = { Text("请输入账本名称") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = "每月${startDay}日",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("周期起始日") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        (1..31).forEach { day ->
                            DropdownMenuItem(
                                text = { Text("每月${day}日") },
                                onClick = {
                                    startDay = day
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                if (!isNewBook) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedButton(
                        onClick = onSwitchTo,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("切换到此账本", fontSize = 12.sp)
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    TextButton(
                        onClick = onDelete,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.textButtonColors(contentColor = ExpenseColor)
                    ) {
                        Text("删除账本", fontSize = 12.sp)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (bookName.isNotBlank()) {
                        onConfirm(book.copy(
                            name = bookName,
                            startDay = startDay
                        ))
                    }
                },
                enabled = bookName.isNotBlank()
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookAddScreen(
    onBack: () -> Unit,
    viewModel: AccountViewModel = viewModel()
) {
    var bookName by remember { mutableStateOf("") }
    var startDay by remember { mutableStateOf(1) }
    var expanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("添加账本", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    OutlinedTextField(
                        value = bookName,
                        onValueChange = { bookName = it },
                        label = { Text("账本名称") },
                        placeholder = { Text("请输入账本名称") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = "每月${startDay}日",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("周期起始日") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            (1..31).forEach { day ->
                                DropdownMenuItem(
                                    text = { Text("每月${day}日") },
                                    onClick = {
                                        startDay = day
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (bookName.isNotBlank()) {
                        val newBook = Book(
                            id = System.currentTimeMillis(),
                            name = bookName,
                            cover = "",
                            startDay = startDay,
                            isDefault = false
                        )
                        viewModel.addBook(newBook)
                        viewModel.selectBook(newBook)
                        onBack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                enabled = bookName.isNotBlank()
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("保存账本")
            }
        }
    }
}
