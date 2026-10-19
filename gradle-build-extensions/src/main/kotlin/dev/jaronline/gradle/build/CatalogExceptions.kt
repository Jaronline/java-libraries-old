package dev.jaronline.gradle.build

class NoSuchCatalogException(catalogName: String): RuntimeException("No version catalogue '$catalogName' exists!")

class NoSuchCatalogItemException private constructor(type: String, id: String, catalogName: String):
	RuntimeException("$type '${id.replace('.', '-')}' does not exist in version catalogue '${catalogName}'!") {
	companion object {
		fun version(id: String, catalogName: String): NoSuchCatalogItemException {
			return NoSuchCatalogItemException("Version", id, catalogName)
		}

		fun library(id: String, catalogName: String): NoSuchCatalogItemException {
			return NoSuchCatalogItemException("Library", id, catalogName)
		}

		fun plugin(id: String, catalogName: String): NoSuchCatalogItemException {
			return NoSuchCatalogItemException("Plugin", id, catalogName)
		}

		fun bundle(id: String, catalogName: String): NoSuchCatalogItemException {
			return NoSuchCatalogItemException("Bundle", id, catalogName)
		}
	}
}
