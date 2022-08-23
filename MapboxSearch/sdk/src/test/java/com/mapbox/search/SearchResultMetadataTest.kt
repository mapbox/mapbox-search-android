package com.mapbox.search

import com.mapbox.search.base.core.CoreResultMetadata
import com.mapbox.search.metadata.OpenHours
import com.mapbox.search.metadata.OpenPeriod
import com.mapbox.search.metadata.ParkingData
import com.mapbox.search.metadata.WeekDay
import com.mapbox.search.metadata.WeekTimestamp
import com.mapbox.search.metadata.mapToCore
import com.mapbox.test.dsl.TestCase
import io.mockk.spyk
import org.junit.Assert
import org.junit.jupiter.api.TestFactory

internal class SearchResultMetadataTest {

    @TestFactory
    fun `Check SearchResultMetadata data access`() = TestCase {
        Given("SearchResultMetadata with mocked core metadata") {

            val testPrimaryPhotos = listOf(ImageInfo(url = "test-url1", width = 150, height = 100))
            val testOtherPhotos = listOf(ImageInfo(url = "test-url2", width = 500, height = 350))

            val testIso1Key = "iso_3166_1"
            val testIso1Value = "FR"

            val testIso2Key = "iso_3166_2"
            val testIso2Value = "FR-J"

            val testMetaKey = "key"
            val testValue = "test value"
            val spyMetaMap = spyk(
                hashMapOf(
                    testMetaKey to testValue,
                    testIso1Key to testIso1Value,
                    testIso2Key to testIso2Value,
                )
            )

            val testOpenHours = OpenHours.Scheduled(
                periods = listOf(
                    OpenPeriod(
                        open = WeekTimestamp(WeekDay.FRIDAY, 9, 0),
                        closed = WeekTimestamp(WeekDay.FRIDAY, 18, 0),
                    ),
                    OpenPeriod(
                        open = WeekTimestamp(WeekDay.SATURDAY, 12, 15),
                        closed = WeekTimestamp(WeekDay.SATURDAY, 13, 30),
                    )
                )
            )
            val testParking = ParkingData(
                totalCapacity = 10,
                reservedForDisabilities = 5
            )
            val testCpsJson = "{\"raw\":{\"distance\":89,\"id\":\"Parkopedia_336981\",\"lan\":51.50715,\"locationReference\":{},\"lon\":-0.129003,\"phone\":\"020 7823 4567\"}}"

            val originalCoreMeta = CoreResultMetadata(
                243,
                "+7 939 32 12",
                "www.test.com",
                3.4,
                "Description of test dummy",
                testOpenHours.mapToCore(),
                testPrimaryPhotos.map { it.mapToCore() },
                testOtherPhotos.map { it.mapToCore() },
                testCpsJson,
                testParking.mapToCore(),
                spyMetaMap
            )
            val spyCoreMeta = spyk(originalCoreMeta)

            val metadata = SearchResultMetadata(spyCoreMeta)

            When("SearchResultMetadata instantiated") {
                Verify("CoreResultMetadata.getReviewCount() called") {
                    spyCoreMeta.reviewCount
                }

                Verify("CoreResultMetadata.getPhone() called") {
                    spyCoreMeta.phone
                }

                Verify("CoreResultMetadata.getWebsite() called") {
                    spyCoreMeta.website
                }

                Verify("CoreResultMetadata.getAvRating() called") {
                    spyCoreMeta.avRating
                }

                Verify("CoreResultMetadata.getDescription() called") {
                    spyCoreMeta.description
                }

                Verify("CoreResultMetadata.getPrimaryPhotos() called") {
                    spyCoreMeta.primaryPhoto
                }

                Verify("CoreResultMetadata.getOtherPhotos() called") {
                    spyCoreMeta.otherPhoto
                }

                Verify("CoreResultMetadata.getOpenHours() called") {
                    spyCoreMeta.openHours
                }

                Verify("CoreResultMetadata.getParking() called") {
                    spyCoreMeta.parking
                }

                Verify("CoreResultMetadata.getCpsJson() called") {
                    spyCoreMeta.cpsJson
                }

                Verify("CoreResultMetadata.data.get(\"iso_3166_1\") called") {
                    spyMetaMap["iso_3166_1"]
                }

                Verify("CoreResultMetadata.data.get(\"iso_3166_2\") called") {
                    spyMetaMap["iso_3166_2"]
                }
            }

            When("reviewCount accessed") {
                Then("Returned review count should be as original", originalCoreMeta.reviewCount, metadata.reviewCount)
            }

            When("phone accessed") {
                Then("Returned phone should be as original", originalCoreMeta.phone, metadata.phone)
            }

            When("website accessed") {
                Then("Returned website should be as original", originalCoreMeta.website, metadata.website)
            }

            When("averageRating accessed") {
                Then("Returned average rating should be as original", originalCoreMeta.avRating, metadata.averageRating)
            }

            When("description accessed") {
                Then("Returned description should be as original", originalCoreMeta.description, metadata.description)
            }

            When("primaryPhotos accessed") {
                Then("Returned primary photos should be as original", testPrimaryPhotos, metadata.primaryPhotos)
            }

            When("otherPhotos accessed") {
                Then("Returned other photos should be as original", testOtherPhotos, metadata.otherPhotos)
            }

            When("extraData accessed") {
                Then("Returned data should be as original") {
                    Assert.assertSame(metadata.extraData, spyMetaMap)
                }
            }

            When("openHours accessed") {
                Then("Returned data should be equal to initially provided") {
                    Assert.assertEquals(testOpenHours, metadata.openHours)
                }
            }

            When("parking accessed") {
                Then("Returned data should be equal to initially provided") {
                    Assert.assertEquals(testParking, metadata.parking)
                }
            }

            When("cpsJson accessed") {
                Then("Returned data should be as original") {
                    Assert.assertSame(originalCoreMeta.cpsJson, metadata.cpsJson)
                }
            }

            When("countryIso1 accessed") {
                Then("Returned data should be equal to initially provided", testIso1Value, metadata.countryIso1)
            }

            When("countryIso2 accessed") {
                Then("Returned data should be equal to initially provided", testIso2Value, metadata.countryIso2)
            }

            When("toString() called") {
                val value = metadata.toString()
                Then("Value should be as expected",
                    "SearchResultMetadata(" +
                            "extraData=${originalCoreMeta.data}, " +
                            "reviewCount=${originalCoreMeta.reviewCount}, " +
                            "phone=${originalCoreMeta.phone}, " +
                            "website=${originalCoreMeta.website}, " +
                            "averageRating=${originalCoreMeta.avRating}, " +
                            "description=${originalCoreMeta.description}, " +
                            "primaryPhotos=$testPrimaryPhotos, " +
                            "otherPhotos=$testOtherPhotos, " +
                            "openHours=$testOpenHours, " +
                            "parking=$testParking, " +
                            "cpsJson=$testCpsJson, " +
                            "countryIso1=$testIso1Value, " +
                            "countryIso2=$testIso2Value" +
                            ")",
                    value
                )
            }

            When("Get original core metadata") {
                Then("Value should be the same") {
                    Assert.assertSame(spyCoreMeta, metadata.coreMetadata)
                }
            }
        }
    }
}
