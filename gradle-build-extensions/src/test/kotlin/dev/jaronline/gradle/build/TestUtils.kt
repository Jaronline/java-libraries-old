package dev.jaronline.gradle.build

import org.gradle.api.artifacts.VersionConstraint
import org.gradle.api.provider.Provider
import org.mockito.Mockito

fun <T> mockProvider(value: T): Provider<T> {
	@Suppress("UNCHECKED_CAST")
	val provider = Mockito.mock(Provider::class.java) as Provider<T>
	Mockito.`when`(provider.get()).thenReturn(value)
	return provider
}

fun mockVersionConstraint(
	requiredVersion: String = "",
	preferredVersion: String = "",
	strictVersion: String = ""
): VersionConstraint {
	val constraint = Mockito.mock(VersionConstraint::class.java)
	Mockito.`when`(constraint.requiredVersion).thenReturn(requiredVersion)
	Mockito.`when`(constraint.preferredVersion).thenReturn(preferredVersion)
	Mockito.`when`(constraint.strictVersion).thenReturn(strictVersion)

	return constraint
}
