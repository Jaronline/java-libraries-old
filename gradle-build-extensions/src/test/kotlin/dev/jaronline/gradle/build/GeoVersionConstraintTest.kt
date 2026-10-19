package dev.jaronline.gradle.build

import org.gradle.api.artifacts.VersionConstraint
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import kotlin.test.assertEquals

class GeoVersionConstraintTest {
	@DisplayName("GIVEN constraint WHEN GeoVersionConstraint#version THEN returns expected")
	@ParameterizedTest(name = "GIVEN {0} WHEN GeoVersionConstraint#version THEN returns \"{1}\"")
	@MethodSource("getVersionGetterReturnsData")
	fun testVersionGetterReturnsCorrectVersion(constraint: VersionConstraint, expected: String) {
		val provider = mockProvider(constraint)
		val sut = GeoVersionConstraint(provider)
		assertEquals(expected, sut.version)
	}

	@DisplayName("GIVEN constraint WHEN GeoVersionConstraint#range THEN returns expected")
	@ParameterizedTest(name = "GIVEN {0} WHEN GeoVersionConstraint#range THEN returns \"{1}\"")
	@MethodSource("getRangeGetterCorrectData")
	fun testRangeGetterReturnsCorrectRange(constraint: VersionConstraint, expected: String) {
		val provider = mockProvider(constraint)
		val sut = GeoVersionConstraint(provider)
		assertDoesNotThrow {
			assertEquals(expected, sut.range)
		}
	}

	@DisplayName("GIVEN constraint WHEN GeoVersionConstraint#range THEN throws IllegalArgumentException")
	@ParameterizedTest(name = "GIVEN {0} WHEN GeoVersionConstraint#range THEN throws IllegalArgumentException")
	@MethodSource("getRangeGetterThrowsData")
	fun testRangeGetterThrowsIllegalArgumentException(constraint: VersionConstraint) {
		val provider = mockProvider(constraint)
		val sut = GeoVersionConstraint(provider)
		assertThrows<IllegalArgumentException> {
			sut.range
		}
	}

	@DisplayName("GIVEN constraint WHEN GeoVersionConstraint#toString() THEN returns expected")
	@ParameterizedTest(name = "GIVEN {0} WHEN GeoVersionConstraint#toString() THEN returns \"{1}\"")
	@MethodSource("getVersionGetterReturnsData")
	fun testToStringReturnsVersion(constraint: VersionConstraint, expected: String) {
		val provider = mockProvider(constraint)
		val sut = GeoVersionConstraint(provider)
		assertEquals(expected, sut.toString())
	}

	companion object {
		@JvmStatic
		fun getVersionGetterReturnsData(): List<Arguments> {
			return listOf(
				Arguments.of(
					mockVersionConstraint(preferredVersion = "0.10.0", requiredVersion = "0.11.0", strictVersion = "[0.10.0]"),
					"0.10.0"
				),
				Arguments.of(
					mockVersionConstraint(requiredVersion = "0.10.0", strictVersion = "[0.10.0]"),
					"0.10.0"
				),
				Arguments.of(
					mockVersionConstraint(strictVersion = "[0.10.0]"),
					""
				)
			)
		}

		@JvmStatic
		fun getVersionGetterCallsPreferredVersionData(): List<Arguments> {
			return listOf(
				Arguments.of(mockVersionConstraint(preferredVersion = "0.10.0")),
				Arguments.of(mockVersionConstraint(preferredVersion = "0.10.0", strictVersion = "[0.10.0]")),
				Arguments.of(mockVersionConstraint(preferredVersion = "0.10.0", requiredVersion = "0.10.0")),
				Arguments.of(mockVersionConstraint(preferredVersion = "0.10.0", strictVersion = "[0.10.0]", requiredVersion = "0.10.0"))
			)
		}

		@JvmStatic
		fun getRangeGetterCorrectData(): List<Arguments> {
			return listOf(
				Arguments.of(
					mockVersionConstraint(preferredVersion = "0.10.0", strictVersion = "[0.10.0]", requiredVersion = "0.11.0"),
					"0.11.0"
				),
				Arguments.of(
					mockVersionConstraint(preferredVersion = "0.10.0", strictVersion = "[0.10.0]"),
					"[0.10.0]"
				)
			)
		}

		@JvmStatic
		fun getRangeGetterThrowsData(): List<Arguments> {
			return listOf(
				Arguments.of(mockVersionConstraint(preferredVersion = "0.10.0")),
				Arguments.of(mockVersionConstraint())
			)
		}
	}
}
