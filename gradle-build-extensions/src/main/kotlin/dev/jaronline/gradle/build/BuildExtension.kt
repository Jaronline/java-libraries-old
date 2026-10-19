package dev.jaronline.gradle.build

import org.gradle.api.Project
import org.gradle.api.artifacts.ExternalModuleDependencyBundle
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.provider.Provider
import org.gradle.plugin.use.PluginDependency

abstract class BuildExtension(val project: Project) {
	fun gradleListProperty(propertyName: String): Provider<List<String>> {
		return gradleProperty(propertyName).map { it.split(',').map { str -> str.trim() } }
	}

	fun gradleProperty(propertyName: String): Provider<String> {
		return project.providers.gradleProperty(propertyName)
	}

	fun environmentVariable(variableName: String): Provider<String> {
		return project.providers.environmentVariable(variableName)
	}

	fun versionLookup(catalog: VersionCatalog, id: String): GeoVersionConstraint {
		return GeoVersionConstraint(project.provider {
			catalog.findVersion(id)
				.orElseThrow {
					NoSuchCatalogItemException.version(id, catalog.name)
				}
		})
	}

	fun libraryLookup(catalog: VersionCatalog, id: String): Provider<MinimalExternalModuleDependency> {
		return catalog.findLibrary(id)
			.orElseThrow {
				NoSuchCatalogItemException.library(id, catalog.name)
			}
	}

	fun pluginLookup(catalog: VersionCatalog, id: String): Provider<PluginDependency> {
		return catalog.findPlugin(id)
			.orElseThrow {
				NoSuchCatalogItemException.plugin(id, catalog.name)
			}
	}

	fun bundleLookup(catalog: VersionCatalog, id: String): Provider<ExternalModuleDependencyBundle> {
		return catalog.findBundle(id)
			.orElseThrow {
				NoSuchCatalogItemException.bundle(id, catalog.name)
			}
	}

	fun findVersionCatalog(name: String): VersionCatalog {
		return project.extensions.getByType(VersionCatalogsExtension::class.java).find(name).orElseThrow {
			NoSuchCatalogException(name)
		}
	}
}
