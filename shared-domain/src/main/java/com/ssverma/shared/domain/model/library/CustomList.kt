package com.ssverma.shared.domain.model.library

data class CustomList(
    val listId: String,
    val title: String,
    val description: String? = null,
    val coverImageUrl: String? = null,
    val isPublic: Boolean = false,
    val isCloned: Boolean = false,
    val sourceAuthorName: String? = null,
    val items: List<CustomListItem> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val itemCount: Int get() = items.size
    val previewPosters: List<String>
        get() = items.map { it.posterImageUrl }.filter { it.isNotBlank() }.take(4)
}
