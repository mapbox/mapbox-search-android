package com.mapbox.search

import com.mapbox.geojson.Point
import com.mapbox.search.base.core.CoreChildMetadata
import com.mapbox.search.base.core.CoreResultMetadata
import com.mapbox.search.base.mapToCore
import com.mapbox.search.base.mapToPlatform
import com.mapbox.search.common.metadata.ImageInfo
import com.mapbox.search.common.metadata.OpenHours
import com.mapbox.search.common.metadata.OpenPeriod
import com.mapbox.search.common.metadata.ParkingData
import com.mapbox.search.common.metadata.WeekDay
import com.mapbox.search.common.metadata.WeekTimestamp
import com.mapbox.test.dsl.TestCase
import io.mockk.spyk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
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

            val children: List<CoreChildMetadata> = listOf(
                CoreChildMetadata(
                    mapboxId = "mapboxId",
                    name = "name",
                    category = "category",
                    coordinates = Point.fromLngLat(0.0, 1.1),
                )
            )

            val originalCoreMeta = CoreResultMetadata(
                reviewCount = 243,
                phone = "+7 939 32 12",
                website = "www.test.com",
                avRating = 3.4,
                description = "Description of test dummy",
                openHours = testOpenHours.mapToCore(),
                primaryPhoto = testPrimaryPhotos.map { it.mapToCore() },
                otherPhoto = testOtherPhotos.map { it.mapToCore() },
                cpsJson = testCpsJson,
                parking = testParking.mapToCore(),
                children = children,
                data = spyMetaMap,
                wheelchairAccessible = true,
                delivery = true,
                driveThrough = true,
                reservable = true,
                parkingAvailable = true,
                valetParking = true,
                streetParking = true,
                servesBreakfast = true,
                servesBrunch = true,
                servesDinner = null,
                servesLunch = true,
                servesWine = true,
                servesBeer = true,
                takeout = true,
                facebookId = "the-facebook",
                fax = "+7 939 32 12",
                email = "the-email@example.com",
                instagram = "the-instagram",
                twitter = "the-twitter",
                priceLevel = "$",
                servesVegan = true,
                servesVegetarian = true,
                rating = 5.0f,
                popularity = 0.5f
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

                Verify("CoreResultMetadata.getWheelchairAccessible() called") {
                    spyCoreMeta.wheelchairAccessible
                }

                Verify("CoreResultMetadata.getDelivery() called") {
                    spyCoreMeta.delivery
                }

                Verify("CoreResultMetadata.getDriveThrough() called") {
                    spyCoreMeta.driveThrough
                }

                Verify("CoreResultMetadata.getReservable() called") {
                    spyCoreMeta.reservable
                }

                Verify("CoreResultMetadata.getParkingAvailable() called") {
                    spyCoreMeta.parkingAvailable
                }

                Verify("CoreResultMetadata.getValetParking() called") {
                    spyCoreMeta.valetParking
                }

                Verify("CoreResultMetadata.getStreetParking() called") {
                    spyCoreMeta.streetParking
                }

                Verify("CoreResultMetadata.getServesBreakfast() called") {
                    spyCoreMeta.servesBreakfast
                }

                Verify("CoreResultMetadata.getServesBrunch() called") {
                    spyCoreMeta.servesBrunch
                }

                Verify("CoreResultMetadata.getServesDinner() called") {
                    spyCoreMeta.servesDinner
                }

                Verify("CoreResultMetadata.getServesLunch() called") {
                    spyCoreMeta.servesLunch
                }

                Verify("CoreResultMetadata.getServesWine() called") {
                    spyCoreMeta.servesWine
                }

                Verify("CoreResultMetadata.getServesBeer() called") {
                    spyCoreMeta.servesBeer
                }

                Verify("CoreResultMetadata.getTakeout() called") {
                    spyCoreMeta.takeout
                }

                Verify("CoreResultMetadata.getFacebookId() called") {
                    spyCoreMeta.facebookId
                }

                Verify("CoreResultMetadata.getFax() called") {
                    spyCoreMeta.fax
                }

                Verify("CoreResultMetadata.getEmail() called") {
                    spyCoreMeta.email
                }

                Verify("CoreResultMetadata.getInstagram() called") {
                    spyCoreMeta.instagram
                }

                Verify("CoreResultMetadata.getTwitter() called") {
                    spyCoreMeta.twitter
                }

                Verify("CoreResultMetadata.getPriceLevel() called") {
                    spyCoreMeta.priceLevel
                }

                Verify("CoreResultMetadata.getServiceVegan() called") {
                    spyCoreMeta.servesVegan
                }

                Verify("CoreResultMetadata.getServesVegetarian() called") {
                    spyCoreMeta.servesVegetarian
                }

                Verify("CoreResultMetadata.getRating() called") {
                    spyCoreMeta.rating
                }

                Verify("CoreResultMetadata.getPopularity() called") {
                    spyCoreMeta.popularity
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
                    assertSame(metadata.extraData, spyMetaMap)
                }
            }

            When("openHours accessed") {
                Then("Returned data should be equal to initially provided") {
                    assertEquals(testOpenHours, metadata.openHours)
                }
            }

            When("parking accessed") {
                Then("Returned data should be equal to initially provided") {
                    assertEquals(testParking, metadata.parking)
                }
            }

            When("cpsJson accessed") {
                Then("Returned data should be as original") {
                    assertSame(originalCoreMeta.cpsJson, metadata.cpsJson)
                }
            }

            When("countryIso1 accessed") {
                Then("Returned data should be equal to initially provided", testIso1Value, metadata.countryIso1)
            }

            When("countryIso2 accessed") {
                Then("Returned data should be equal to initially provided", testIso2Value, metadata.countryIso2)
            }

            When("wheelchairAccessible accessed") {
                Then("Returned data should be as original") {
                    assertEquals(originalCoreMeta.wheelchairAccessible, metadata.wheelchairAccessible)
                }
            }

             When("delivery accessed") {
                 Then("Returned data should be as original") {
                     assertEquals(originalCoreMeta.delivery, metadata.delivery)
                 }
            }

             When("driveThrough accessed") {
                 Then("Returned data should be as original") {
                     assertEquals(originalCoreMeta.driveThrough, metadata.driveThrough)
                 }
            }

             When("reservable accessed") {
                 Then("Returned data should be as original") {
                     assertEquals(originalCoreMeta.reservable, metadata.reservable)
                 }
            }

             When("parkingAvailable accessed") {
                 Then("Returned data should be as original") {
                     assertEquals(originalCoreMeta.parkingAvailable, metadata.parkingAvailable)
                 }
            }

             When("valetParking accessed") {
                 Then("Returned data should be as original") {
                     assertEquals(originalCoreMeta.valetParking, metadata.valetParking)
                 }
            }

             When("streetParking accessed") {
                 Then("Returned data should be as original") {
                     assertEquals(originalCoreMeta.streetParking, metadata.streetParking)
                 }
            }

             When("servesBreakfast accessed") {
                 Then("Returned data should be as original") {
                     assertEquals(originalCoreMeta.servesBreakfast, metadata.servesBreakfast)
                 }
            }

             When("servesBrunch accessed") {
                 Then("Returned data should be as original") {
                     assertEquals(originalCoreMeta.servesBrunch, metadata.servesBrunch)
                 }
            }

             When("servesDinner accessed") {
                 Then("Returned data should be as original") {
                     assertEquals(originalCoreMeta.servesDinner, metadata.servesDinner)
                 }
            }

             When("servesLunch accessed") {
                 Then("Returned data should be as original") {
                     assertEquals(originalCoreMeta.servesLunch, metadata.servesLunch)
                 }
            }

             When("servesWine accessed") {
                 Then("Returned data should be as original") {
                     assertEquals(originalCoreMeta.servesWine, metadata.servesWine)
                 }
            }

             When("servesBeer accessed") {
                 Then("Returned data should be as original") {
                     assertEquals(originalCoreMeta.servesBeer, metadata.servesBeer)
                 }
            }

             When("takeout accessed") {
                 Then("Returned data should be as original") {
                     assertEquals(originalCoreMeta.takeout, metadata.takeout)
                 }
            }

             When("facebookId accessed") {
                 Then("Returned data should be as original") {
                     assertEquals(originalCoreMeta.facebookId, metadata.facebookId)
                 }
            }

             When("fax accessed") {
                 Then("Returned data should be as original") {
                     assertEquals(originalCoreMeta.fax, metadata.fax)
                 }
            }

             When("email accessed") {
                 Then("Returned data should be as original") {
                     assertEquals(originalCoreMeta.email, metadata.email)
                 }
            }

             When("instagram accessed") {
                 Then("Returned data should be as original") {
                     assertEquals(originalCoreMeta.instagram, metadata.instagram)
                 }
            }

             When("twitter accessed") {
                 Then("Returned data should be as original") {
                     assertEquals(originalCoreMeta.twitter, metadata.twitter)
                 }
            }

             When("priceLevel accessed") {
                 Then("Returned data should be as original") {
                     assertEquals(originalCoreMeta.priceLevel, metadata.priceLevel)
                 }
            }

             When("serviceVegan accessed") {
                 Then("Returned data should be as original") {
                     assertEquals(originalCoreMeta.servesVegan, metadata.servesVegan)
                 }
            }

             When("servesVegetarian accessed") {
                 Then("Returned data should be as original") {
                     assertEquals(originalCoreMeta.servesVegetarian, metadata.servesVegetarian)
                 }
            }

             When("rating accessed") {
                 Then("Returned data should be as original") {
                     assertEquals(originalCoreMeta.rating, metadata.rating)
                 }
            }

             When("popularity accessed") {
                 Then("Returned data should be as original") {
                     assertEquals(originalCoreMeta.popularity, metadata.popularity)
                 }
            }

            When("toString() called") {
                val value = metadata.toString()
                Then(
                    "Value should be as expected",
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
                            "countryIso2=$testIso2Value, " +
                            "children=${children.map { it.mapToPlatform() }}, " +
                            "wheelchairAccessible=${originalCoreMeta.wheelchairAccessible}, " +
                            "delivery=${originalCoreMeta.delivery}, " +
                            "driveThrough=${originalCoreMeta.driveThrough}, " +
                            "reservable=${originalCoreMeta.reservable}, " +
                            "parkingAvailable=${originalCoreMeta.parkingAvailable}, " +
                            "valetParking=${originalCoreMeta.valetParking}, " +
                            "streetParking=${originalCoreMeta.streetParking}, " +
                            "servesBreakfast=${originalCoreMeta.servesBreakfast}, " +
                            "servesBrunch=${originalCoreMeta.servesBrunch}, " +
                            "servesDinner=${originalCoreMeta.servesDinner}, " +
                            "servesLunch=${originalCoreMeta.servesLunch}, " +
                            "servesWine=${originalCoreMeta.servesWine}, " +
                            "servesBeer=${originalCoreMeta.servesBeer}, " +
                            "takeout=${originalCoreMeta.takeout}, " +
                            "facebookId=${originalCoreMeta.facebookId}, " +
                            "fax=${originalCoreMeta.fax}, " +
                            "email=${originalCoreMeta.email}, " +
                            "instagram=${originalCoreMeta.instagram}, " +
                            "twitter=${originalCoreMeta.twitter}, " +
                            "priceLevel=${originalCoreMeta.priceLevel}, " +
                            "servesVegan=${originalCoreMeta.servesVegan}, " +
                            "servesVegetarian=${originalCoreMeta.servesVegetarian}, " +
                            "rating=${originalCoreMeta.rating}, " +
                            "popularity=${originalCoreMeta.popularity}" +
                            ")",
                    value
                )
            }

            When("Get original core metadata") {
                Then("Value should be the same") {
                    assertSame(spyCoreMeta, metadata.coreMetadata)
                }
            }
        }
    }
}
