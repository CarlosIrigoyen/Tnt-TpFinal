package com.example.trabajofinal2024

object FoodCatalog {
    // Leche y yogur
    /*val LECHE_POLVO_ENTERA = FoodTemplate(
        "Leche en polvo entera",
        "Leche y yogur",
        100f,
        494.04f,
        38.05f,
        26.15f,
        26.36f,
        0f,
        80.48f,
        0f
    )
*/
    val LECHE_FLUIDA_ENTERA = FoodTemplate(
        "Leche",
        "Leche y yogur",
        100f,
        57.92f,
        4.63f,
        3.1f,
        3.0f,
        0f,
        10.11f,
        0f
    )

    val YOGUR = FoodTemplate(
        "Yogur",
        "Leche y yogur",
        100f,
        57.92f,
        4.63f,
        3.1f,
        3.0f,
        0f,
        10.11f,
        0f
    )

    // Grasas animales
    val QUESO_PASTA_DURA = FoodTemplate(
        "Queso de pasta dura",
        "Grasas animales",
        100f,
        373.83f,
        0.34f,
        32.39f,
        26.99f,
        0f,
        82.99f,
        0f
    )

    val QUESO_PASTA_SEMIDURA_AZUL = FoodTemplate(
        "Queso azul",
        "Grasas animales",
        100f,
        328.16f,
        0.1f,
        25.33f,
        25.16f,
        0f,
        72.14f,
        0f
    )

    val MANTECA = FoodTemplate(
        "Manteca",
        "Grasas animales",
        100f,
        745.35f,
        0f,
        0.33f,
        82.67f,
        0f,
        223f,
        0f
    )

    // Carnes y huevos
    val HUEVO_CRUDO_HERVIDO = FoodTemplate(
        "Huevo duro",
        "Carnes y huevos",
        100f,
        153.75f,
        0.2f,
        12.7f,
        11.35f,
        0f,
        449f,
        0f
    )

    val HUEVO_FRITO = FoodTemplate(
        "Huevo frito",
        "Carnes y huevos",
        100f,
        191.32f,
        0.83f,
        13.61f,
        14.84f,
        0f,
        401f,
        0f
    )

    val VACUNA_MAGRA = FoodTemplate(
        "Carne vacuna magra",
        "Carnes y huevos",
        100f,
        130.6f,
        0f,
        21.4f,
        5.0f,
        0f,
        62.31f,
        0f
    )
/*
    val VACUNA_CORTES_GRASOS = FoodTemplate(
        "Carne vacuna  grasa",
        "Carnes y huevos",
        100f,
        199.55f,
        0f,
        18.95f,
        13.75f,
        0f,
        68.34f,
        0f
    )
*/
    val CARNE_PICADA = FoodTemplate(
        "Carne picada",
        "Carnes y huevos",
        100f,
        165.08f,
        0f,
        20.18f,
        9.38f,
        0f,
        65.33f,
        0f
    )

    val POLLO_DESHUESADO_SIN_PIEL = FoodTemplate(
        "Pollo",
        "Carnes y huevos",
        100f,
        165f,
        0f,
        31f,
        3.6f,
        0f,
        85f,
        0f
    )

    // Carnes procesadas
    val SALCHICHAS = FoodTemplate(
        "Salchichas",
        "Carnes procesadas",
        100f,
        187.92f,
        5.75f,
        12.25f,
        12.88f,
        0f,
        30f,
        0f
    )

    /*val SALAME_SALAMIN_CHORIZO_SECO_LONGANIZA = FoodTemplate(
        "Salame",
        "Carnes procesadas",
        100f,
        377.45f,
        0.29f,
        19.53f,
        33.13f,
        0f,
        78f,
        0f
    )
*/
    val MORTADELA = FoodTemplate(
        "Mortadela",
        "Carnes procesadas",
        100f,
        265.24f,
        1.56f,
        13f,
        23f,
        0f,
        71f,
        0f
    )

    // Platos populares
    val PAPAS_FRITAS_CASERAS = FoodTemplate(
        "Papas fritas caseras",
        "Platos populares",
        100f,
        162.04f,
        17.49f,
        2.05f,
        9.32f,
        0f,
        0f,
        2.4f
    )

    val PIZZA = FoodTemplate(
        "Pizza",
        "Platos populares",
        100f,
        251.86f,
        26.49f,
        13.57f,
        10.18f,
        0f,
        17f,
        2.3f
    )

    val EMPANADAS_CARNE_FRITAS = FoodTemplate(
        "Empanadas de carne fritas",
        "Platos populares",
        100f,
        297.48f,
        14.82f,
        10.14f,
        21.96f,
        0f,
        68.94f,
        0.62f
    )

    val EMPANADAS_CARNE = FoodTemplate(
        "Empanadas de carne caseras",
        "Platos populares",
        100f,
        214.41f,
        14.82f,
        10.14f,
        12.73f,
        0f,
        68.94f,
        0.62f
    )

    val PASTEL_DE_PAPAS = FoodTemplate(
        "Pastel de papas",
        "Platos populares",
        100f,
        143.32f,
        7.69f,
        8.34f,
        8.8f,
        0f,
        68.52f,
        0.98f
    )

    val PUCHERO = FoodTemplate(
        "Puchero",
        "Platos populares",
        100f,
        100.08f,
        12.44f,
        5.47f,
        3.16f,
        0f,
        14.39f,
        4.02f
    )

    // Frutas
    val BANANA = FoodTemplate(
        "Banana",
        "Frutas",
        100f,
        98.69f,
        22.84f,
        1.09f,
        0.33f,
        0f,
        0f,
        2.6f
    )
/*
    val DURAZNO = FoodTemplate(
        "Durazno",
        "Frutas",
        100f,
        46.47f,
        10.1f,
        0.91f,
        0.27f,
        0f,
        0f,
        1.5f
    )
*/
    val MANZANA = FoodTemplate(
        "Manzana",
        "Frutas",
        100f,
        58.2f,
        13.8f,
        0.3f,
        0.2f,
        0f,
        0f,
        2.4f
    )

    val NARANJA = FoodTemplate(
        "Naranja",
        "Frutas",
        100f,
        43f,
        8.3f,
        0.9f,
        0.2f,
        0f,
        0f,
        2.2f
    )

    // Cereales
    val ARROZ_BLANCO_COCIDO = FoodTemplate(
        "Arroz blanco",
        "Cereales",
        100f,
        130f,
        28f,
        2.69f,
        0.28f,
        0f,
        0f,
        0.4f
    )

    val PAN_FRANCES = FoodTemplate(
        "Pan francés",
        "Cereales",
        100f,
        265f,
        49f,
        9f,
        3.2f,
        0f,
        0f,
        2.7f
    )

    // Bebidas e infusiones azucaradas
    val AGUAS_SABORIZADAS_CLASICAS = FoodTemplate(
        "Aguas saborizadas clásicas",
        "Bebidas e infusiones azucaradas",
        100f,
        32f,
        8f,
        0f,
        0f,
        0f,
        0f,
        0f
    )

    val BEBIDAS_DEPORTIVAS_Y_ENERGIZANTES = FoodTemplate(
        "Bebidas deportivas y energizantes",
        "Bebidas e infusiones azucaradas",
        100f,
        24f,
        6f,
        0f,
        0f,
        0f,
        0f,
        0f
    )

    val GASEOSAS_CLASICAS = FoodTemplate(
        "Gaseosas clásicas",
        "Bebidas e infusiones azucaradas",
        100f,
        42.96f,
        10.74f,
        0f,
        0f,
        0f,
        0f,
        0f
    )

    // Bebidas alcohólicas
    val VINO = FoodTemplate(
        "Vino",
        "Bebidas alcohólicas",
        100f,
        86.59f,
        3.17f,
        0.19f,
        0f,
        10.45f,
        0f,
        0f
    )

    val CERVEZA_O_APERITIVOS = FoodTemplate(
        "Cerveza o aperitivos",
        "Bebidas alcohólicas",
        100f,
        35.48f,
        1.61f,
        0.3f,
        0.06f,
        3.9f,
        0f,
        0f
    )
/*
    val LICOR = FoodTemplate(
        "Licor",
        "Bebidas alcohólicas",
        100f,
        370.35f,
        46.75f,
        0.1f,
        0.3f,
        25.75f,
        0f,
        0f
    )

    val BEBIDAS_BLANCAS = FoodTemplate(
        "Bebidas blancas",
        "Bebidas alcohólicas",
        100f,
        233.8f,
        0f,
        0f,
        0f,
        33.4f,
        0f,
        0f
    )
*/
    val ALL = listOf(
        // Leche y yogur
        //LECHE_POLVO_ENTERA,
        LECHE_FLUIDA_ENTERA, YOGUR,

        // Grasas animales
        QUESO_PASTA_DURA, QUESO_PASTA_SEMIDURA_AZUL, MANTECA,

        // Carnes y huevos
        HUEVO_CRUDO_HERVIDO, HUEVO_FRITO, VACUNA_MAGRA,
        CARNE_PICADA, POLLO_DESHUESADO_SIN_PIEL,
        //VACUNA_CORTES_GRASOS

        // Carnes procesadas
        SALCHICHAS,  MORTADELA,
        //SALAME_SALAMIN_CHORIZO_SECO_LONGANIZA

        // Platos populares
        PAPAS_FRITAS_CASERAS, PIZZA, EMPANADAS_CARNE_FRITAS, EMPANADAS_CARNE,
        PASTEL_DE_PAPAS, PUCHERO,

        // Frutas
        BANANA, MANZANA, NARANJA,
        // DURAZNO,

        // Cereales
        ARROZ_BLANCO_COCIDO, PAN_FRANCES,

        // Bebidas e infusiones azucaradas
        AGUAS_SABORIZADAS_CLASICAS, BEBIDAS_DEPORTIVAS_Y_ENERGIZANTES, GASEOSAS_CLASICAS,

        // Bebidas alcohólicas
        VINO, CERVEZA_O_APERITIVOS,
        //LICOR, BEBIDAS_BLANCAS
    )
    fun findByName(name: String) = ALL.find { it.nombre.equals(name, ignoreCase = true) }
}
