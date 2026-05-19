package co.com.disspace.app.domain.model

object DisspaceModules {
    val articulosModule = CrudModule(
        title = "Articulos",
        singular = "articulo",
        description = "Catalogo de productos y servicios.",
        path = "/articulos",
        listKey = "articulos",
        detailKey = "articulo",
        idKey = "ARTICULOID",
        titleFields = listOf("CODIGO", "DESCRIPCION"),
        previewFields = listOf("TIPO", "TIPOPRODID"),
        filters = listOf(ApiField("codigo", "Codigo"), ApiField("descripcion", "Descripcion"), ApiField("tipo", "Tipo")),
        fields = listOf(
            ApiField("codigo", "Codigo", required = true, existingKeys = listOf("CODIGO")),
            ApiField("descripcion", "Descripcion", existingKeys = listOf("DESCRIPCION")),
            ApiField("tipoprodid", "Tipo producto ID", kind = FieldKind.NUMBER, required = true, existingKeys = listOf("TIPOPRODID"))
        )
    )

    val listaPreciosModule = CrudModule(
        title = "Listas de precios",
        singular = "lista de precios",
        description = "Precios por proveedor y articulo.",
        path = "/listaprecios",
        listKey = "listaprecios",
        detailKey = "listaprecio",
        idKey = "LISTAPRECIOID",
        titleFields = listOf("DESCRIPCION", "PROVEEDOR"),
        previewFields = listOf("COSTO", "COSTO_INSTALACION"),
        fields = listOf(
            ApiField("terceroid", "Proveedor ID", kind = FieldKind.NUMBER, required = true, existingKeys = listOf("FABRICAID")),
            ApiField("articuloid", "Articulo ID", kind = FieldKind.NUMBER, required = true, existingKeys = listOf("ARTICULOID")),
            ApiField("costo", "Costo", kind = FieldKind.NUMBER, required = true, existingKeys = listOf("COSTO")),
            ApiField("costo_instalacion", "Costo instalacion", kind = FieldKind.NUMBER, existingKeys = listOf("COSTO_INSTALACION")),
            ApiField("instalacion_por_ancho", "Instalacion por ancho", kind = FieldKind.BOOLEAN, existingKeys = listOf("INSTALACION_POR_ANCHO")),
            ApiField("instalacion_por_alto", "Instalacion por alto", kind = FieldKind.BOOLEAN, existingKeys = listOf("INSTALACION_POR_ALTO")),
            ApiField("descripcion", "Descripcion", existingKeys = listOf("DESCRIPCION"))
        )
    )

    val genericModules: List<CrudModule>
        get() = listOf(
            CrudModule(
                title = "Terceros",
                singular = "tercero",
                description = "Clientes, vendedores y proveedores.",
                path = "/terceros",
                listKey = "terceros",
                detailKey = "tercero",
                idKey = "TERCEROID",
                titleFields = listOf("NOMBRE", "APELLIDO"),
                previewFields = listOf("NRODOCUMENTO", "TIPO", "CORREO", "TELEFONO"),
                filters = listOf(ApiField("documento", "Documento"), ApiField("nombre", "Nombre"), ApiField("tipo", "Tipo")),
                fields = listOf(
                    ApiField("nrodocumento", "Documento", existingKeys = listOf("NRODOCUMENTO")),
                    ApiField("nombre", "Nombre", required = true, existingKeys = listOf("NOMBRE")),
                    ApiField("apellido", "Apellido", required = true, existingKeys = listOf("APELLIDO")),
                    ApiField("correo", "Correo", kind = FieldKind.EMAIL, existingKeys = listOf("CORREO")),
                    ApiField("telefono", "Telefono", existingKeys = listOf("TELEFONO")),
                    ApiField("direccion", "Direccion", existingKeys = listOf("DIRECCION")),
                    ApiField("tipoterceroid", "Tipo tercero ID", kind = FieldKind.NUMBER, required = true, existingKeys = listOf("TIPOTERCEROID"))
                )
            ),
            articulosModule,
            CrudModule(
                title = "Sucursales",
                singular = "sucursal",
                description = "Sedes, consecutivos y ano de operacion.",
                path = "/sucursales",
                listKey = "sucursales",
                detailKey = "sucursal",
                idKey = "SUCURSALID",
                titleFields = listOf("NOMBRE", "AÑO"),
                previewFields = listOf("CELULAR", "CONSECUTIVO", "PRINCIPAL"),
                fields = listOf(
                    ApiField("nombre", "Nombre", required = true, existingKeys = listOf("NOMBRE")),
                    ApiField("año", "Ano", kind = FieldKind.NUMBER, existingKeys = listOf("AÑO")),
                    ApiField("celular", "Celular", existingKeys = listOf("CELULAR")),
                    ApiField("consecutivo", "Consecutivo", kind = FieldKind.NUMBER, existingKeys = listOf("CONSECUTIVO")),
                    ApiField("principal", "Principal", kind = FieldKind.BOOLEAN, existingKeys = listOf("PRINCIPAL"))
                )
            ),
            CrudModule(
                title = "Usuarios",
                singular = "usuario",
                description = "Accesos, roles y sucursal asignada. Requiere ADMIN.",
                path = "/users",
                listKey = "users",
                detailKey = "user",
                idKey = "USERID",
                titleFields = listOf("EMAIL", "ROLE"),
                previewFields = listOf("SUCURSAL", "SUCURSALID", "TERCEROID"),
                filters = listOf(ApiField("rol", "Rol"), ApiField("sucursal", "Sucursal")),
                fields = listOf(
                    ApiField("email", "Email", kind = FieldKind.EMAIL, required = true, existingKeys = listOf("EMAIL")),
                    ApiField("role", "Rol", required = true, existingKeys = listOf("ROLE")),
                    ApiField("terceroid", "Tercero ID", kind = FieldKind.NUMBER, existingKeys = listOf("TERCEROID")),
                    ApiField("sucursalid", "Sucursal ID", kind = FieldKind.NUMBER, existingKeys = listOf("SUCURSALID")),
                    ApiField("changePassword", "Cambiar contrasena", kind = FieldKind.BOOLEAN),
                    ApiField("password", "Contrasena", kind = FieldKind.PASSWORD),
                    ApiField("confirmPassword", "Confirmar contrasena", kind = FieldKind.PASSWORD)
                )
            )
        )
}
