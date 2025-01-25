package com.github.pvkvetkin.android.android_contact_list

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.ContactsContract
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted: Boolean ->
                if (granted) {
                    val allContacts = fetchAllContacts()
                    setContent {
                        ContactList(contacts = allContacts)
                    }
                } else {
                    setContent {
                        NotExistedContactList()
                    }
                }
            }

        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED -> {
                val allContacts = fetchAllContacts()
                setContent {
                    ContactList(contacts = allContacts)
                }
            }

            else -> {
                permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
            }
        }
    }
}

fun Context.fetchAllContacts(): List<Contact> {
    contentResolver.query(
        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
        null,
        null,
        null,
        null
    )
        .use { cursor ->
            if (cursor == null) return emptyList()
            val builder = ArrayList<Contact>()
            while (cursor.moveToNext()) {
                val columnIndexDisplayName =
                    cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val name =
                    cursor.getString(columnIndexDisplayName) ?: "N/A"

                val columnIndexNumber =
                    cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                val phoneNumber =
                    cursor.getString(columnIndexNumber) ?: "N/A"

                builder.add(Contact(name, phoneNumber))
            }
            return builder
        }
}

@Composable
fun ContactList(contacts: List<Contact>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(contacts) { contact ->
            ContactItem(contact = contact)
        }
    }
}

@Composable
fun ContactItem(contact: Contact) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Contact Icon",
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Column {
                Text(
                    text = contact.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = contact.phoneNumber,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun NotExistedContactList() {
    Column {
        Text(
            text = "No contacts found",
            modifier = Modifier.padding(16.dp)
        )
    }
}

data class Contact(
    val name: String,
    val phoneNumber: String
)
