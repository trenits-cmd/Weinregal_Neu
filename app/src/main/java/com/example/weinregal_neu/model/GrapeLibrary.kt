package com.example.weinregal_neu.model

/**
 * Kompaktes Weinwissen zu den wichtigsten Rebsorten.
 * Die Daten sind lokal hinterlegt und funktionieren damit auch offline.
 */
data class GrapeProfile(
    val name: String,
    val color: String,
    val character: String,
    val aromas: String,
    val acidity: String,
    val tannin: String,
    val regions: String,
    val servingTemperature: String,
    val foodPairing: String,
    val aliases: List<String> = emptyList()
) {
    val summary: String get() = "$color · $character"
}

object GrapeLibrary {

    private val profiles = listOf(
        GrapeProfile(
            name = "Riesling",
            color = "Weißwein",
            character = "frisch, mineralisch, langlebig",
            aromas = "Zitrus, grüner Apfel, Pfirsich, Pfirsichblüte, Petrol bei Reife",
            acidity = "hoch",
            tannin = "keine",
            regions = "Mosel, Rheingau, Pfalz, Elsass, Österreich (Wachau)",
            servingTemperature = "8-12 °C",
            foodPairing = "Fisch, Meeresfrüchte, asiatische Küche, Ziegenkäse",
            aliases = listOf("Rheinriesling", "Weißer Riesling")
        ),
        GrapeProfile(
            name = "Chardonnay",
            color = "Weißwein",
            character = "vielseitig, cremig bis straff",
            aromas = "Apfel, Birne, Zitrone, Butter, Vanille, Haselnuss",
            acidity = "mittel bis hoch",
            tannin = "keine",
            regions = "Burgund, Champagne, Kalifornien, Australien, Südafrika",
            servingTemperature = "10-13 °C",
            foodPairing = "Geflügel, Fisch, cremige Saucen, Weichkäse",
            aliases = listOf("Morillon")
        ),
        GrapeProfile(
            name = "Sauvignon Blanc",
            color = "Weißwein",
            character = "aromatisch, knackig, grasig",
            aromas = "Stachelbeere, Grapefruit, Holunder, grüne Paprika, Feuerstein",
            acidity = "hoch",
            tannin = "keine",
            regions = "Loire, Neuseeland (Marlborough), Steiermark, Südafrika",
            servingTemperature = "8-10 °C",
            foodPairing = "Ziegenkäse, Salate, Spargel, Meeresfrüchte",
            aliases = listOf("Sauvignon", "Fumé Blanc")
        ),
        GrapeProfile(
            name = "Weißburgunder",
            color = "Weißwein",
            character = "dezent, elegant, nussig",
            aromas = "Birne, Apfel, Mandel, Hefe, zarter Rauch",
            acidity = "mittel",
            tannin = "keine",
            regions = "Baden, Pfalz, Burgund, Südtirol",
            servingTemperature = "9-11 °C",
            foodPairing = "Kalbfleisch, helle Saucen, Spargel, Fisch",
            aliases = listOf("Pinot Blanc", "Weissburgunder", "Klevner")
        ),
        GrapeProfile(
            name = "Grauburgunder",
            color = "Weißwein",
            character = "vollmundig, würzig, saftig",
            aromas = "Birne, Quitte, Honigmelone, Haselnuss, Würze",
            acidity = "mittel bis niedrig",
            tannin = "keine",
            regions = "Baden, Pfalz, Elsass, Friaul",
            servingTemperature = "10-12 °C",
            foodPairing = "Geflügel, Pilze, Pasta, kräftiger Fisch",
            aliases = listOf("Pinot Gris", "Ruländer", "Grauer Burgunder")
        ),
        GrapeProfile(
            name = "Silvaner",
            color = "Weißwein",
            character = "bodenständig, erdig, mild",
            aromas = "Apfel, Birne, Wiesenkräuter, feuchte Erde",
            acidity = "mittel",
            tannin = "keine",
            regions = "Franken, Rheinhessen, Elsass",
            servingTemperature = "9-11 °C",
            foodPairing = "Spargel, Gemüse, Wurst, fränkischer Braten",
            aliases = listOf("Grüner Silvaner", "Sylvaner")
        ),
        GrapeProfile(
            name = "Gewürztraminer",
            color = "Weißwein",
            character = "exotisch, intensiv, opulent",
            aromas = "Litschi, Rose, Ingwer, Zimt, Aprikose",
            acidity = "niedrig",
            tannin = "keine",
            regions = "Elsass, Pfalz, Baden, Südtirol",
            servingTemperature = "8-10 °C",
            foodPairing = "scharfe Küche, asiatisch, Münsterkäse, Desserts",
            aliases = listOf("Traminer")
        ),
        GrapeProfile(
            name = "Grüner Veltliner",
            color = "Weißwein",
            character = "pfeffrig, frisch, vielseitig",
            aromas = "grüner Apfel, Zitrus, weißer Pfeffer, Kräuter",
            acidity = "hoch",
            tannin = "keine",
            regions = "Wachau, Kamptal, Kremstal, Wien",
            servingTemperature = "8-11 °C",
            foodPairing = "Wiener Schnitzel, Gemüse, Fisch, Schinken",
            aliases = listOf("Veltliner")
        ),
        GrapeProfile(
            name = "Müller-Thurgau",
            color = "Weißwein",
            character = "mild, blumig, unkompliziert",
            aromas = "Muskat, Holunder, Apfel, Limette",
            acidity = "niedrig bis mittel",
            tannin = "keine",
            regions = "Rheinhessen, Pfalz, Baden, Südtirol",
            servingTemperature = "8-10 °C",
            foodPairing = "leichte Vorspeisen, Salate, Geflügel",
            aliases = listOf("Rivaner", "Müller Thurgau")
        ),
        GrapeProfile(
            name = "Cabernet Sauvignon",
            color = "Rotwein",
            character = "kraftvoll, strukturiert, langlebig",
            aromas = "schwarze Johannisbeere, Cassis, Paprika, Zedernholz, Tabak",
            acidity = "mittel",
            tannin = "hoch",
            regions = "Bordeaux, Napa Valley, Chile, Südafrika, Toskana",
            servingTemperature = "16-18 °C",
            foodPairing = "Rind, Lamm, Wild, kräftiger Käse",
            aliases = listOf("Cabernet")
        ),
        GrapeProfile(
            name = "Merlot",
            color = "Rotwein",
            character = "samtig, rund, zugänglich",
            aromas = "Pflaume, Kirsche, Schokolade, Zedernholz",
            acidity = "mittel",
            tannin = "mittel",
            regions = "Bordeaux (Pomerol, Saint-Émilion), Toskana, Chile",
            servingTemperature = "16-18 °C",
            foodPairing = "Rind, Pasta, Lamm, geschmorter Geflügelbraten",
            aliases = listOf("Merlot Noir")
        ),
        GrapeProfile(
            name = "Spätburgunder",
            color = "Rotwein",
            character = "elegant, seidig, fruchtig",
            aromas = "Kirsche, Erdbeere, Himbeere, Unterholz, Gewürze",
            acidity = "mittel bis hoch",
            tannin = "niedrig bis mittel",
            regions = "Burgund, Baden, Ahr, Pfalz, Oregon",
            servingTemperature = "14-16 °C",
            foodPairing = "Ente, Lamm, Pilze, milder Käse",
            aliases = listOf("Pinot Noir", "Spätburgunder", "Blauburgunder")
        ),
        GrapeProfile(
            name = "Syrah",
            color = "Rotwein",
            character = "dunkel, würzig, druckvoll",
            aromas = "Brombeere, schwarzer Pfeffer, Rauch, Olive, Veilchen",
            acidity = "mittel",
            tannin = "hoch",
            regions = "Rhône (Côte-Rôtie, Hermitage), Australien (Shiraz), Südafrika",
            servingTemperature = "16-18 °C",
            foodPairing = "Grillfleisch, Wild, Lamm, würzige Küche",
            aliases = listOf("Shiraz")
        ),
        GrapeProfile(
            name = "Tempranillo",
            color = "Rotwein",
            character = "würzig, rund, reifefähig",
            aromas = "Kirsche, Pflaume, Leder, Vanille, Tabak",
            acidity = "mittel",
            tannin = "mittel",
            regions = "Rioja, Ribera del Duero, Portugal (Tinta Roriz)",
            servingTemperature = "16-18 °C",
            foodPairing = "Lamm, Schinken, Tapas, gegrilltes Gemüse",
            aliases = listOf("Tinta Roriz", "Tinto Fino", "Aragonez")
        ),
        GrapeProfile(
            name = "Garnacha",
            color = "Rotwein",
            character = "fruchtig, warm, samtig",
            aromas = "Erdbeere, Himbeere, Süßholz, Kräuter, Pfeffer",
            acidity = "niedrig bis mittel",
            tannin = "mittel",
            regions = "Priorat, Aragón, Rhône (Grenache), Sardinien",
            servingTemperature = "15-17 °C",
            foodPairing = "Paella, Grillgemüse, Lamm, mediterrane Küche",
            aliases = listOf("Grenache", "Garnacha Tinta", "Cannonau")
        ),
        GrapeProfile(
            name = "Sangiovese",
            color = "Rotwein",
            character = "saftig, würzig, säurebetont",
            aromas = "Sauerkirsche, Tomate, Leder, Oregano, Tabak",
            acidity = "hoch",
            tannin = "mittel bis hoch",
            regions = "Chianti, Brunello di Montalcino, Romagna",
            servingTemperature = "16-18 °C",
            foodPairing = "Pasta mit Tomatensauce, Pizza, Rind, Pecorino",
            aliases = listOf("Sangioveto", "Prugnolo Gentile")
        ),
        GrapeProfile(
            name = "Nebbiolo",
            color = "Rotwein",
            character = "elegant, tanninreich, langlebig",
            aromas = "Rose, Teer, Kirsche, Trüffel, Lakritze",
            acidity = "hoch",
            tannin = "hoch",
            regions = "Barolo, Barbaresco, Langhe, Valtellina",
            servingTemperature = "17-19 °C",
            foodPairing = "Trüffelgerichte, Rind, Wild, gereifter Käse",
            aliases = listOf("Spanna", "Chiavennasca")
        ),
        GrapeProfile(
            name = "Primitivo",
            color = "Rotwein",
            character = "üppig, süßlich, alkoholreich",
            aromas = "Brombeere, Pflaume, Rosinen, Vanille, Zimt",
            acidity = "niedrig",
            tannin = "mittel",
            regions = "Apulien (Manduria), Kalifornien (Zinfandel)",
            servingTemperature = "16-18 °C",
            foodPairing = "Grillfleisch, BBQ, scharfe Salami, Hartkäse",
            aliases = listOf("Zinfandel", "Primitivo di Manduria")
        ),
        GrapeProfile(
            name = "Malbec",
            color = "Rotwein",
            character = "dunkel, fruchtig, weich",
            aromas = "Pflaume, Blaubeere, Veilchen, Kakao, Kaffee",
            acidity = "mittel",
            tannin = "mittel bis hoch",
            regions = "Mendoza, Cahors, Chile",
            servingTemperature = "16-18 °C",
            foodPairing = "Rind, Empanadas, Grillfleisch, Hartkäse",
            aliases = listOf("Côt", "Auxerrois")
        ),
        GrapeProfile(
            name = "Dornfelder",
            color = "Rotwein",
            character = "dunkel, fruchtig, mild",
            aromas = "Kirsche, Brombeere, Pflaume, leichte Würze",
            acidity = "mittel",
            tannin = "niedrig bis mittel",
            regions = "Rheinhessen, Pfalz, Württemberg",
            servingTemperature = "15-17 °C",
            foodPairing = "Wurst, deftige Küche, Pizza, Grillabend",
            aliases = emptyList()
        ),
        GrapeProfile(
            name = "Lemberger",
            color = "Rotwein",
            character = "würzig, kräftig, samtig",
            aromas = "Kirsche, Brombeere, Pfeffer, Rauch",
            acidity = "mittel",
            tannin = "mittel bis hoch",
            regions = "Württemberg, Österreich (Blaufränkisch), Ungarn (Kékfrankos)",
            servingTemperature = "16-18 °C",
            foodPairing = "Lamm, Wild, Braten, Paprika-Gerichte",
            aliases = listOf("Blaufränkisch", "Blauer Lemberger", "Kékfrankos")
        ),
        GrapeProfile(
            name = "Trollinger",
            color = "Rotwein",
            character = "leicht, frisch, unkompliziert",
            aromas = "Kirsche, Erdbeere, Mandel",
            acidity = "hoch",
            tannin = "niedrig",
            regions = "Württemberg, Südtirol (Vernatsch)",
            servingTemperature = "14-16 °C",
            foodPairing = "Vesper, Wurst, Pizza, leichte Gerichte",
            aliases = listOf("Vernatsch", "Schiava")
        )
    )

    fun all(): List<GrapeProfile> = profiles

    fun find(query: String): GrapeProfile? {
        val normalized = query.normalize()
        if (normalized.isBlank()) return null

        profiles.firstOrNull { it.name.normalize() == normalized }?.let { return it }
        profiles.firstOrNull { profile -> profile.aliases.any { it.normalize() == normalized } }?.let { return it }

        // Teilstring-Treffer, z. B. "Cabernet Sauvignon & Merlot" oder "Riesling trocken".
        // Bei mehreren Treffern gewinnt der laengste Begriff, damit "Cabernet Sauvignon"
        // nicht vom kuerzeren Alias "Sauvignon" (Sauvignon Blanc) ueberstimmt wird.
        return profiles
            .flatMap { profile ->
                (listOf(profile.name) + profile.aliases).mapNotNull { term ->
                    val normalizedTerm = term.normalize()
                    if (normalizedTerm.length > 3 && normalized.contains(normalizedTerm)) {
                        profile to normalizedTerm.length
                    } else {
                        null
                    }
                }
            }
            .maxByOrNull { it.second }
            ?.first
    }

    private fun String.normalize(): String = lowercase()
        .replace("ä", "a")
        .replace("ö", "o")
        .replace("ü", "u")
        .replace("ß", "ss")
        .replace(Regex("[^a-z0-9 ]"), " ")
        .replace(Regex("\\s+"), " ")
        .trim()
}
