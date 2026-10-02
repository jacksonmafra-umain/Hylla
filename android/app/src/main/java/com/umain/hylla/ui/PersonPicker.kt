package com.umain.hylla.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.umain.hylla.fleet.Person
import com.umain.hylla.fleet.PersonId

/** One of the fleet's people, as a radio group with 48 dp rows. */
@Composable
fun PersonPicker(people: List<Person>, selected: PersonId?, onSelect: (PersonId) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.selectableGroup()) {
        people.forEach { person ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .selectable(selected = person.id == selected, role = Role.RadioButton) { onSelect(person.id) },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButton(selected = person.id == selected, onClick = null)
                Text(person.name, Modifier.padding(start = 16.dp))
            }
        }
    }
}
