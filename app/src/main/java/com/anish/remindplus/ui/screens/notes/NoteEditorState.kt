package com.anish.remindplus.ui.screens.notes

// ── Editor Mode ───────────────────────────────────────────────────────────────

enum class EditorMode {
    TEXT,       // Normal typing / checklist / formatting
    HIGHLIGHT,  // Quick highlighter overlay — keyboard hidden, toolbar stays TEXT-like
    DRAW        // Full draw mode with pen / eraser / color / stroke pickers
}
