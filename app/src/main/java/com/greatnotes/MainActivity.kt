package com.greatnotes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.greatnotes.presentation.library.LibraryScreen
import com.greatnotes.presentation.theme.GreatNotesTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GreatNotesTheme {
                LibraryScreen(
                    onNotebookClick = { _ ->
                        // TODO(phase-3): navigate to the notebook editor / ink canvas for this id.
                    },
                )
            }
        }
    }
}
