package dev.jaronline.gradle.build

import org.gradle.api.Project
import org.gradle.api.Transformer
import org.gradle.api.artifacts.ExternalModuleDependencyBundle
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.artifacts.VersionConstraint
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.api.provider.ProviderFactory
import org.gradle.plugin.use.PluginDependency
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.quality.Strictness
import java.util.Optional
import java.util.concurrent.Callable
import kotlin.test.Test
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.WARN)
class BuildExtensionTest {
	@InjectMocks
	lateinit var sut: TestBuildExtension

	@Mock
	lateinit var project: Project

	@DisplayName("GIVEN value, propertyName WHEN BuildExtension#gradleListProperty(propertyName) THEN returns expected")
	@ParameterizedTest(name = "GIVEN value=\"{0}\", propertyName=\"list\" WHEN BuildExtension#gradleListProperty(propertyName) THEN returns {2}")
	@MethodSource("getGradleListPropertyData")
	fun testGradleListPropertyReturnsCorrectValue(value: String, expected: List<String>) {
		val providers = Mockito.mock(ProviderFactory::class.java)
		val listProvider = mockProvider(value)
		Mockito.`when`(listProvider.map(Mockito.any<Transformer<List<String>, String>>()))
			.thenAnswer { input -> mockProvider((input.arguments[0] as Transformer<*, String>).transform(listProvider.get())) }
		Mockito.`when`(providers.gradleProperty("list"))
			.thenReturn(listProvider)
		Mockito.`when`(project.providers).thenReturn(providers)

		val actual = sut.gradleListProperty("list").get()

		assertEquals(expected, actual)
	}

	@Test
	@DisplayName("GIVEN propertyName=\"test\" WHEN BuildExtension#gradleProperty(propertyName) THEN returns \"Test Value\"")
	fun testGradlePropertyReturnsCorrectValue() {
		val providers = Mockito.mock(ProviderFactory::class.java)
		val testProvider = mockProvider("Test Value")
		Mockito.`when`(providers.gradleProperty("test"))
			.thenReturn(testProvider)
		Mockito.`when`(project.providers).thenReturn(providers)

		val actual = sut.gradleProperty("test").get()

		assertEquals("Test Value", actual)
	}

	@Test
	@DisplayName("GIVEN variableName=\"TEST_VARIABLE\" WHEN BuildExtension#environmentVariable(variableName) THEN returns \"Test Value\"")
	fun testEnvironmentVariableReturnsCorrectValue() {
		val providers = Mockito.mock(ProviderFactory::class.java)
		val testProvider = mockProvider("Test Value")
		Mockito.`when`(providers.environmentVariable("TEST_VARIABLE"))
			.thenReturn(testProvider)
		Mockito.`when`(project.providers).thenReturn(providers)

		val actual = sut.environmentVariable("TEST_VARIABLE").get()

		assertEquals("Test Value", actual)
	}

	@Test
	@DisplayName("GIVEN catalog=libs, id=\"version\" WHEN BuildExtension#versionLookup(catalog, id) THEN returns GeoVersionConstraint")
	fun testVersionLookupReturnsCorrectGeoVersionConstraint() {
		val libs = Mockito.mock(VersionCatalog::class.java)
		val constraint = mockVersionConstraint()
		Mockito.`when`(libs.findVersion("version")).thenReturn(Optional.of(constraint))
		Mockito.`when`(project.provider(Mockito.any<Callable<VersionConstraint>>()))
			.thenAnswer { input -> mockProvider((input.arguments[0] as Callable<*>).call()) }

		val actual = sut.versionLookup(libs, "version")

		assertInstanceOf(GeoVersionConstraint::class.java, actual)
	}

	@Test
	@DisplayName("GIVEN catalog=libs, id=\"does-not-exist\" WHEN BuildExtension#versionLookup(catalog, id) THEN throws NoSuchCatalogItemException")
	fun testVersionLookupThrowsNoSuchCatalogItemException() {
		val libs = Mockito.mock(VersionCatalog::class.java)
		Mockito.`when`(libs.name).thenReturn("libs")
		Mockito.`when`(libs.findVersion(Mockito.anyString())).thenReturn(Optional.empty())
		Mockito.`when`(project.provider(Mockito.any<Callable<VersionConstraint>>()))
			.thenAnswer { input -> (input.arguments[0] as Callable<*>).call() }

		assertThrows<NoSuchCatalogItemException> {
			sut.versionLookup(libs, "does-not-exist").get()
		}
	}

	@Test
	@DisplayName("GIVEN catalog=libs, id=\"library\" WHEN BuildExtension#libraryLookup(catalog, id) THEN returns library")
	fun testLibraryLookupReturnsCorrectLibrary() {
		val libs: VersionCatalog = Mockito.mock(VersionCatalog::class.java)
		val library: MinimalExternalModuleDependency = Mockito.mock(MinimalExternalModuleDependency::class.java)
		val libraryProvider = mockProvider(library)
		Mockito.`when`(libs.findLibrary("library")).thenReturn(Optional.of(libraryProvider))

		val actual = sut.libraryLookup(libs, "library").get()

		assertEquals(library, actual)
	}

	@Test
	@DisplayName("GIVEN catalog=libs, id=\"does-not-exist\" WHEN BuildExtension#libraryLookup(catalog, id) THEN throws NoSuchCatalogItemException")
	fun testLibraryLookupThrowsNoSuchCatalogItemException() {
		val libs: VersionCatalog = Mockito.mock(VersionCatalog::class.java)
		Mockito.`when`(libs.name).thenReturn("libs")
		Mockito.`when`(libs.findLibrary(Mockito.anyString())).thenReturn(Optional.empty())

		assertThrows<NoSuchCatalogItemException> {
			sut.libraryLookup(libs, "does-not-exist")
		}
	}

	@Test
	@DisplayName("GIVEN catalog=libs, id=\"plugin\" WHEN BuildExtension#pluginLookup(catalog, id) THEN returns plugin")
	fun testPluginLookupReturnsCorrectPlugin() {
		val libs: VersionCatalog = Mockito.mock(VersionCatalog::class.java)
		val plugin: PluginDependency = Mockito.mock(PluginDependency::class.java)
		val pluginProvider = mockProvider(plugin)
		Mockito.`when`(libs.findPlugin("plugin")).thenReturn(Optional.of(pluginProvider))

		val actual = sut.pluginLookup(libs, "plugin").get()

		assertEquals(plugin, actual)
	}

	@Test
	@DisplayName("GIVEN catalog=libs, id=\"does-not-exist\" WHEN BuildExtension#pluginLookup(catalog, id) THEN throws NoSuchCatalogItemException")
	fun testPluginLookupThrowsNoSuchCatalogItemException() {
		val libs: VersionCatalog = Mockito.mock(VersionCatalog::class.java)
		Mockito.`when`(libs.name).thenReturn("libs")
		Mockito.`when`(libs.findPlugin(Mockito.anyString())).thenReturn(Optional.empty())

		assertThrows<NoSuchCatalogItemException> {
			sut.pluginLookup(libs, "does-not-exist")
		}
	}

	@Test
	@DisplayName("GIVEN catalog=libs, id=\"bundle\" WHEN BuildExtension#bundleLookup(catalog, id) THEN returns bundle")
	fun testBundleLookupReturnsCorrectBundle() {
		val libs: VersionCatalog = Mockito.mock(VersionCatalog::class.java)
		val bundle: ExternalModuleDependencyBundle = Mockito.mock(ExternalModuleDependencyBundle::class.java)
		val bundleProvider = mockProvider(bundle)
		Mockito.`when`(libs.findBundle("bundle")).thenReturn(Optional.of(bundleProvider))

		val actual = sut.bundleLookup(libs, "bundle").get()

		assertEquals(bundle, actual)
	}

	@Test
	@DisplayName("GIVEN catalog=libs, id=\"does-not-exist\" WHEN BuildExtension#bundleLookup(catalog, id) THEN throws NoSuchCatalogItemException")
	fun testBundleLookupThrowsNoSuchCatalogItemException() {
		val libs: VersionCatalog = Mockito.mock(VersionCatalog::class.java)
		Mockito.`when`(libs.name).thenReturn("libs")
		Mockito.`when`(libs.findBundle(Mockito.anyString())).thenReturn(Optional.empty())

		assertThrows<NoSuchCatalogItemException> {
			sut.bundleLookup(libs, "does-not-exist")
		}
	}

	@Test
	@DisplayName("GIVEN name=\"libs\" WHEN BuildExtension#findVersionCatalog(name) THEN returns libs")
	fun testFindVersionCatalogReturnsCorrectCatalog() {
		val extensions = Mockito.mock(ExtensionContainer::class.java)
		val versionCatalogs = Mockito.mock(VersionCatalogsExtension::class.java)
		val libs: VersionCatalog = Mockito.mock(VersionCatalog::class.java)
		Mockito.`when`(versionCatalogs.find("libs")).thenReturn(Optional.of(libs))
		Mockito.`when`(extensions.getByType(VersionCatalogsExtension::class.java))
			.thenReturn(versionCatalogs)
		Mockito.`when`(project.extensions).thenReturn(extensions)

		val actual = sut.findVersionCatalog("libs")

		assertEquals(libs, actual)
	}

	@Test
	@DisplayName("GIVEN name=\"does-not-exist\" WHEN BuildExtension#findVersionCatalog(name) THEN throws NoSuchCatalogException")
	fun testFindVersionCatalogThrowsNoSuchCatalogException() {
		val extensions = Mockito.mock(ExtensionContainer::class.java)
		val versionCatalogs = Mockito.mock(VersionCatalogsExtension::class.java)
		Mockito.`when`(versionCatalogs.find(Mockito.anyString())).thenReturn(Optional.empty())
		Mockito.`when`(extensions.getByType(VersionCatalogsExtension::class.java))
			.thenReturn(versionCatalogs)
		Mockito.`when`(project.extensions).thenReturn(extensions)

		assertThrows<NoSuchCatalogException> {
			sut.findVersionCatalog("does-not-exist")
		}
	}

	companion object {
		@JvmStatic
		fun getGradleListPropertyData(): List<Arguments> {
			return listOf(
				Arguments.of("item1", listOf("item1")),
				Arguments.of("anotherItem", listOf("anotherItem")),
				Arguments.of("3", listOf("3")),
				Arguments.of("item1 item2", listOf("item1 item2")),
				Arguments.of("item1,item2", listOf("item1", "item2")),
				Arguments.of("item1, item2", listOf("item1", "item2")),
				Arguments.of("item1," + " ".repeat(100) + "item2", listOf("item1", "item2")),
				Arguments.of("item1,item2,item3", listOf("item1", "item2", "item3")),
				Arguments.of("item1, item2, item3", listOf("item1", "item2", "item3")),
				Arguments.of("item1, item2,item3", listOf("item1", "item2", "item3")),
				Arguments.of("item1,item2, item3", listOf("item1", "item2", "item3")),
				Arguments.of("item1  ", listOf("item1")),
				Arguments.of("item1, item2    ", listOf("item1", "item2"))
			)
		}
	}
}

class TestBuildExtension(project: Project) : BuildExtension(project)
