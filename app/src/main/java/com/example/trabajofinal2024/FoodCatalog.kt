package com.example.trabajofinal2024

object FoodCatalog {
    // Frutas
    val BANANA = FoodTemplate("Banana", "Frutas", 100f, 98.69f, 22.84f, 1.09f, 0.33f, 0f, 0f, 2.6f)
    val DURAZNO = FoodTemplate("Durazno", "Frutas", 100f, 46.47f, 10.1f, 0.91f, 0.27f, 0f, 0f, 1.5f)
    val MANZANA = FoodTemplate("Manzana", "Frutas", 100f, 58.2f, 13.8f, 0.3f, 0.2f, 0f, 0f, 2.4f)
    val NARANJA = FoodTemplate("Naranja", "Frutas", 100f, 43f, 8.3f, 0.9f, 0.2f, 0f, 0f, 2.2f)

    // Cereales
    val ARROZ_BLANCO_COCIDO = FoodTemplate("Arroz blanco cocido", "Cereales", 100f, 130f, 28f, 2.69f, 0.28f, 0f, 0f, 0.4f)
    val PAN_FRANCES = FoodTemplate("Pan francés", "Cereales", 100f, 265f, 49f, 9f, 3.2f, 0f, 0f, 2.7f)

    // Carnes magras individuales
    val BOLA_DE_LOMO = FoodTemplate("Bola de lomo", "Carnes y huevos", 100f, 130.6f, 0f, 21.4f, 5f, 0f, 62.31f, 0f)
    val COLITA_DE_CUADRIL = FoodTemplate("Colita de cuadril", "Carnes y huevos", 100f, 130.6f, 0f, 21.4f, 5f, 0f, 62.31f, 0f)
    val CUADRIL = FoodTemplate("Cuadril", "Carnes y huevos", 100f, 130.6f, 0f, 21.4f, 5f, 0f, 62.31f, 0f)
    val NALGA = FoodTemplate("Nalga", "Carnes y huevos", 100f, 130.6f, 0f, 21.4f, 5f, 0f, 62.31f, 0f)
    val TAPA_DE_NALGA = FoodTemplate("Tapa de nalga", "Carnes y huevos", 100f, 130.6f, 0f, 21.4f, 5f, 0f, 62.31f, 0f)
    val PALETA = FoodTemplate("Paleta", "Carnes y huevos", 100f, 130.6f, 0f, 21.4f, 5f, 0f, 62.31f, 0f)
    val CUADRADA = FoodTemplate("Cuadrada", "Carnes y huevos", 100f, 130.6f, 0f, 21.4f, 5f, 0f, 62.31f, 0f)
    val PECETO = FoodTemplate("Peceto", "Carnes y huevos", 100f, 130.6f, 0f, 21.4f, 5f, 0f, 62.31f, 0f)
    val TORTUGUITA = FoodTemplate("Tortuguita", "Carnes y huevos", 100f, 130.6f, 0f, 21.4f, 5f, 0f, 62.31f, 0f)
    val VACIO = FoodTemplate("Vacío", "Carnes y huevos", 100f, 130.6f, 0f, 21.4f, 5f, 0f, 62.31f, 0f)

    // Carnes grasas individuales
    val AGUJA = FoodTemplate("Aguja", "Carnes y huevos", 100f, 199.6f, 0f, 19.0f, 13.8f, 0f, 68.3f, 0f)
    val BIFE_ANCHO = FoodTemplate("Bife ancho", "Carnes y huevos", 100f, 199.6f, 0f, 19.0f, 13.8f, 0f, 68.3f, 0f)
    val BIFE_ANGOSTO = FoodTemplate("Bife angosto", "Carnes y huevos", 100f, 199.6f, 0f, 19.0f, 13.8f, 0f, 68.3f, 0f)
    val COGOTE = FoodTemplate("Cogote", "Carnes y huevos", 100f, 199.6f, 0f, 19.0f, 13.8f, 0f, 68.3f, 0f)
    val ASADO = FoodTemplate("Asado", "Carnes y huevos", 100f, 199.6f, 0f, 19.0f, 13.8f, 0f, 68.3f, 0f)
    val COSTILLAR = FoodTemplate("Costillar", "Carnes y huevos", 100f, 199.6f, 0f, 19.0f, 13.8f, 0f, 68.3f, 0f)
    val ENTRAÑA = FoodTemplate("Entraña", "Carnes y huevos", 100f, 199.6f, 0f, 19.0f, 13.8f, 0f, 68.3f, 0f)
    val OSOBUCO = FoodTemplate("Osobuco", "Carnes y huevos", 100f, 199.6f, 0f, 19.0f, 13.8f, 0f, 68.3f, 0f)
    val MATAMBRE = FoodTemplate("Matambre", "Carnes y huevos", 100f, 199.6f, 0f, 19.0f, 13.8f, 0f, 68.3f, 0f)
    val PALOMITA = FoodTemplate("Palomita", "Carnes y huevos", 100f, 199.6f, 0f, 19.0f, 13.8f, 0f, 68.3f, 0f)

    // Carne picada
    val CARNE_PICADA = FoodTemplate("Carne picada", "Carnes y huevos", 100f, 165.1f, 0f, 20.2f, 9.4f, 0f, 65.3f, 0f)

    // Pollo
    val POLLO_DESHUESADO_SIN_PIEL = FoodTemplate("Pollo deshuesado sin piel", "Carnes y huevos", 100f, 165f, 0f, 31f, 3.6f, 0f, 85f, 0f)

    // Lácteos
    val LECHE_POLVO_ENTERA = FoodTemplate("Leche en polvo entera", "Leche y yogur", 100f, 494.0f, 38.05f, 26.15f, 26.36f, 0f, 80.48f, 0f)
    val LECHE_FLUIDA_ENTERA = FoodTemplate("Leche fluida entera", "Leche y yogur", 100f, 57.9f, 4.63f, 3.1f, 3.0f, 0f, 10.11f, 0f)
    val YOGUR = FoodTemplate("Yogur", "Leche y yogur", 100f, 57.9f, 4.63f, 3.1f, 3.0f, 0f, 10.11f, 0f)

    // Quesos duros individuales
    val QUESO_SARDO = FoodTemplate("Queso Sardo", "Grasas animales", 100f, 373.8f, 0.3f, 32.4f, 27.0f, 0f, 83.0f, 0f)
    val QUESO_ROMANO = FoodTemplate("Queso Romano", "Grasas animales", 100f, 373.8f, 0.3f, 32.4f, 27.0f, 0f, 83.0f, 0f)
    val QUESO_PROVOLONE = FoodTemplate("Queso Provolone", "Grasas animales", 100f, 373.8f, 0.3f, 32.4f, 27.0f, 0f, 83.0f, 0f)
    val QUESO_REGGIANITO = FoodTemplate("Queso Reggianito", "Grasas animales", 100f, 373.8f, 0.3f, 32.4f, 27.0f, 0f, 83.0f, 0f)
    val QUESO_PARMESANO = FoodTemplate("Queso Parmesano", "Grasas animales", 100f, 373.8f, 0.3f, 32.4f, 27.0f, 0f, 83.0f, 0f)

    // Quesos semiduros individuales
    val QUESO_HOLANDA = FoodTemplate("Queso Holanda", "Grasas animales", 100f, 328.2f, 0.1f, 25.3f, 25.2f, 0f, 72.1f, 0f)
    val QUESO_GOUDA = FoodTemplate("Queso Gouda", "Grasas animales", 100f, 328.2f, 0.1f, 25.3f, 25.2f, 0f, 72.1f, 0f)
    val QUESO_FONTINA = FoodTemplate("Queso Fontina", "Grasas animales", 100f, 328.2f, 0.1f, 25.3f, 25.2f, 0f, 72.1f, 0f)
    val QUESO_PATEGRAS = FoodTemplate("Queso Pategras", "Grasas animales", 100f, 328.2f, 0.1f, 25.3f, 25.2f, 0f, 72.1f, 0f)
    val QUESO_DAMBO = FoodTemplate("Queso Dambo", "Grasas animales", 100f, 328.2f, 0.1f, 25.3f, 25.2f, 0f, 72.1f, 0f)

    // Manteca
    val MANTECA = FoodTemplate("Manteca", "Grasas animales", 100f, 745.4f, 0f, 0.33f, 82.67f, 0f, 223f, 0f)

    // Huevos
    val HUEVO_CRUDO_HERVIDO = FoodTemplate("Huevo crudo/hervido", "Carnes y huevos", 100f, 153.8f, 0.2f, 12.7f, 11.35f, 0f, 449f, 0f)
    val HUEVO_FRITO = FoodTemplate("Huevo frito", "Carnes y huevos", 100f, 191.3f, 0.83f, 13.61f, 14.84f, 0f, 401f, 0f)

    // Carnes procesadas
    val SALCHICHAS = FoodTemplate("Salchichas", "Carnes procesadas", 100f, 187.9f, 5.75f, 12.25f, 12.88f, 0f, 30f, 0f)
    val SALAME = FoodTemplate("Salame", "Carnes procesadas", 100f, 377.5f, 0.3f, 19.5f, 33.1f, 0f, 78f, 0f)
    val SALAMIN = FoodTemplate("Salamín", "Carnes procesadas", 100f, 377.5f, 0.3f, 19.5f, 33.1f, 0f, 78f, 0f)
    val CHORIZO_SECO = FoodTemplate("Chorizo seco", "Carnes procesadas", 100f, 377.5f, 0.3f, 19.5f, 33.1f, 0f, 78f, 0f)
    val LONGANIZA = FoodTemplate("Longaniza", "Carnes procesadas", 100f, 377.5f, 0.3f, 19.5f, 33.1f, 0f, 78f, 0f)
    val MORTADELA = FoodTemplate("Mortadela", "Carnes procesadas", 100f, 265.2f, 1.56f, 13f, 23f, 0f, 71f, 0f)

    // Platos populares
    val PAPAS_FRITAS_CASERAS = FoodTemplate("Papas fritas caseras", "Platos populares", 100f, 162.0f, 17.49f, 2.05f, 9.32f, 0f, 0f, 2.4f)
    val PIZZA = FoodTemplate("Pizza", "Platos populares", 100f, 251.9f, 26.49f, 13.57f, 10.18f, 0f, 17f, 2.3f)
    val EMPANADAS_CARNE_FRITAS = FoodTemplate("Empanadas de carne fritas", "Platos populares", 100f, 297.5f, 14.82f, 10.14f, 21.96f, 0f, 68.94f, 0.62f)
    val EMPANADAS_CARNE_HORNO = FoodTemplate("Empanadas de carne al horno", "Platos populares", 100f, 214.4f, 14.82f, 10.14f, 12.73f, 0f, 68.94f, 0.62f)
    val PASTEL_PAPAS = FoodTemplate("Pastel de papas", "Platos populares", 100f, 143.3f, 7.69f, 8.34f, 8.8f, 0f, 68.52f, 0.98f)
    val PUCHERO = FoodTemplate("Puchero", "Platos populares", 100f, 100.1f, 12.44f, 5.47f, 3.16f, 0f, 14.39f, 4.02f)

    // Bebidas
    val AGUA_SABORIZADA = FoodTemplate("Agua saborizada", "Bebidas e infusiones azucaradas", 100f, 32f, 8f, 0f, 0f, 0f, 0f, 0f)
    val BEBIDA_DEPORTIVA = FoodTemplate("Bebida deportiva", "Bebidas e infusiones azucaradas", 100f, 24f, 6f, 0f, 0f, 0f, 0f, 0f)
    val GASEOSAS = FoodTemplate("Gaseosas", "Bebidas e infusiones azucaradas", 100f, 43.0f, 10.74f, 0f, 0f, 0f, 0f, 0f)

    // Bebidas alcohólicas
    val VINO = FoodTemplate("Vino", "Bebidas alcohólicas", 100f, 86.6f, 3.17f, 0.19f, 0f, 10.45f, 0f, 0f)
    val CERVEZA = FoodTemplate("Cerveza", "Bebidas alcohólicas", 100f, 35.5f, 1.61f, 0.3f, 0.06f, 3.9f, 0f, 0f)
    val LICOR = FoodTemplate("Licor", "Bebidas alcohólicas", 100f, 370.4f, 46.75f, 0.1f, 0.3f, 25.75f, 0f, 0f)
    val BEBIDA_BLANCA = FoodTemplate("Bebida blanca", "Bebidas alcohólicas", 100f, 233.8f, 0f, 0f, 0f, 33.4f, 0f, 0f)

    val ALL = listOf(
        // Frutas
        BANANA, DURAZNO, MANZANA, NARANJA,

        // Cereales
        ARROZ_BLANCO_COCIDO, PAN_FRANCES,

        // Carnes magras individuales
        BOLA_DE_LOMO, COLITA_DE_CUADRIL, CUADRIL, NALGA, TAPA_DE_NALGA,
        PALETA, CUADRADA, PECETO, TORTUGUITA, VACIO,

        // Carnes grasas individuales
        AGUJA, BIFE_ANCHO, BIFE_ANGOSTO, COGOTE, ASADO,
        COSTILLAR, ENTRAÑA, OSOBUCO, MATAMBRE, PALOMITA,

        // Otras carnes
        CARNE_PICADA, POLLO_DESHUESADO_SIN_PIEL,

        // Lácteos
        LECHE_POLVO_ENTERA, LECHE_FLUIDA_ENTERA, YOGUR,

        // Quesos duros individuales
        QUESO_SARDO, QUESO_ROMANO, QUESO_PROVOLONE, QUESO_REGGIANITO, QUESO_PARMESANO,

        // Quesos semiduros individuales
        QUESO_HOLANDA, QUESO_GOUDA, QUESO_FONTINA, QUESO_PATEGRAS, QUESO_DAMBO,

        // Grasas
        MANTECA,

        // Huevos
        HUEVO_CRUDO_HERVIDO, HUEVO_FRITO,

        // Carnes procesadas
        SALCHICHAS, SALAME, SALAMIN, CHORIZO_SECO, LONGANIZA, MORTADELA,

        // Platos populares
        PAPAS_FRITAS_CASERAS, PIZZA, EMPANADAS_CARNE_FRITAS, EMPANADAS_CARNE_HORNO,
        PASTEL_PAPAS, PUCHERO,

        // Bebidas
        AGUA_SABORIZADA, BEBIDA_DEPORTIVA, GASEOSAS,

        // Bebidas alcohólicas
        VINO, CERVEZA, LICOR, BEBIDA_BLANCA
    )

    fun findByName(name: String) = ALL.find { it.nombre.equals(name, true) }
}