package co.com.disspace.app.domain.model

enum class FieldKind { TEXT, NUMBER, EMAIL, PASSWORD, DATE, BOOLEAN }

data class ApiField(
    val key: String,
    val label: String,
    val hint: String = "",
    val kind: FieldKind = FieldKind.TEXT,
    val required: Boolean = false,
    val existingKeys: List<String> = emptyList()
)

data class CrudModule(
    val title: String,
    val singular: String,
    val description: String,
    val path: String,
    val listKey: String,
    val detailKey: String,
    val idKey: String,
    val titleFields: List<String>,
    val previewFields: List<String>,
    val filters: List<ApiField> = emptyList(),
    val fields: List<ApiField>
)
