@file:OptIn(MapboxExperimental::class)

package com.mapbox.search

import com.mapbox.annotation.MapboxExperimental
import com.mapbox.geojson.Point
import com.mapbox.search.base.core.CoreChildMetadata
import com.mapbox.search.base.core.CoreParkingAvailabilityLevel
import com.mapbox.search.base.core.CoreParkingInfo
import com.mapbox.search.base.core.CoreParkingPaymentMethod
import com.mapbox.search.base.core.CoreParkingPaymentType
import com.mapbox.search.base.core.CoreParkingRestriction
import com.mapbox.search.base.core.CoreParkingTrend
import com.mapbox.search.base.core.createCoreResultMetadata
import com.mapbox.search.base.factory.mapToCore
import com.mapbox.search.base.factory.parking.mapToCore
import com.mapbox.search.base.factory.parking.mapToPlatform
import com.mapbox.search.base.mapToCore
import com.mapbox.search.base.mapToPlatform
import com.mapbox.search.common.metadata.ChildMetadata
import com.mapbox.search.common.metadata.ImageInfo
import com.mapbox.search.common.metadata.OpenHours
import com.mapbox.search.common.metadata.OpenPeriod
import com.mapbox.search.common.metadata.ParkingData
import com.mapbox.search.common.metadata.WeekDay
import com.mapbox.search.common.metadata.WeekTimestamp
import com.mapbox.search.common.tests.ReflectionObjectsFactory
import com.mapbox.search.common.tests.ToStringVerifier
import com.mapbox.search.common.tests.withPrefabTestPoint
import com.mapbox.search.tests_support.SdkCustomTypeObjectCreators
import com.mapbox.test.dsl.TestCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import nl.jqno.equalsverifier.EqualsVerifier
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
            val metaMap = mockk<HashMap<String, String>>(relaxed = true).apply {
                every { this@apply[eq(testMetaKey)] } returns testValue
                every { this@apply[eq(testIso1Key)] } returns testIso1Value
                every { this@apply[eq(testIso2Key)] } returns testIso2Value
            }

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

            val parkingInfo = CoreParkingInfo(
                capacity = 100,
                rateInfo = null,
                availability = 75,
                availabilityLevel = CoreParkingAvailabilityLevel.HIGH,
                availabilityAt = "test",
                trend = CoreParkingTrend.INCREASING,
                paymentMethods = listOf(CoreParkingPaymentMethod.PARKING_METER),
                paymentTypes = listOf(CoreParkingPaymentType.CARDS),
                restrictions = listOf(CoreParkingRestriction.NO_LPG),
            )

            val originalCoreMeta = createCoreResultMetadata(
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
                data = metaMap,
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
                popularity = 0.5f,
                cuisines = listOf("french", "spanish"),
                parkingInfo = parkingInfo,
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
                    metaMap["iso_3166_1"]
                }

                Verify("CoreResultMetadata.data.get(\"iso_3166_2\") called") {
                    metaMap["iso_3166_2"]
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

                Verify("CoreResultMetadata.getCuisines() called") {
                    spyCoreMeta.cuisines
                }

                Verify("CoreResultMetadata.parkingInfo() called") {
                    spyCoreMeta.parkingInfo
                }
            }

            When("reviewCount accessed") {
                Then(
                    "Returned review count should be as original",
                    originalCoreMeta.reviewCount,
                    metadata.reviewCount
                )
            }

            When("phone accessed") {
                Then("Returned phone should be as original", originalCoreMeta.phone, metadata.phone)
            }

            When("website accessed") {
                Then(
                    "Returned website should be as original",
                    originalCoreMeta.website,
                    metadata.website
                )
            }

            When("averageRating accessed") {
                Then(
                    "Returned average rating should be as original",
                    originalCoreMeta.avRating,
                    metadata.averageRating
                )
            }

            When("description accessed") {
                Then(
                    "Returned description should be as original",
                    originalCoreMeta.description,
                    metadata.description
                )
            }

            When("primaryPhotos accessed") {
                Then(
                    "Returned primary photos should be as original",
                    testPrimaryPhotos,
                    metadata.primaryPhotos
                )
            }

            When("otherPhotos accessed") {
                Then(
                    "Returned other photos should be as original",
                    testOtherPhotos,
                    metadata.otherPhotos
                )
            }

            When("extraData accessed") {
                Then("Returned data should be as original") {
                    assertSame(metadata.extraData, metaMap)
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
                Then(
                    "Returned data should be equal to initially provided",
                    testIso1Value,
                    metadata.countryIso1
                )
            }

            When("countryIso2 accessed") {
                Then(
                    "Returned data should be equal to initially provided",
                    testIso2Value,
                    metadata.countryIso2
                )
            }

            When("wheelchairAccessible accessed") {
                Then("Returned data should be as original") {
                    assertEquals(
                        originalCoreMeta.wheelchairAccessible,
                        metadata.wheelchairAccessible
                    )
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

            When("cuisines accessed") {
                Then("Returned data should be as original") {
                    assertEquals(originalCoreMeta.cuisines, metadata.cuisines)
                }
            }

            When("parkingInfo accessed") {
                Then("Returned data should be as original") {
                    assertEquals(originalCoreMeta.parkingInfo, metadata.parkingInfo?.mapToCore())
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
                            "popularity=${originalCoreMeta.popularity}, " +
                            "cuisines=${originalCoreMeta.cuisines}, " +
                            "parkingInfo=${parkingInfo.mapToPlatform()}" +
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

    @TestFactory
    fun `Check SearchResultMetadata Builder`() = TestCase {
        Given("the following search result metadata properties") {
            val reviewCount = 243
            val phone = "+7 939 32 12"
            val website = "www.test.com"
            val avRating = 3.4
            val description = "Description of test dummy"
            val openHours = OpenHours.AlwaysOpen
            val primaryPhoto = listOf<ImageInfo>()
            val otherPhoto = listOf<ImageInfo>()
            val cpsJson = "testCpsJson"
            val parking = ParkingData(1, 1)
            val children = listOf<ChildMetadata>()
            val data = mapOf<String, String>()
            val wheelchairAccessible = true
            val delivery = true
            val driveThrough = true
            val reservable = true
            val parkingAvailable = true
            val valetParking = true
            val streetParking = true
            val servesBreakfast = true
            val servesBrunch = true
            val servesDinner = true
            val servesLunch = true
            val servesWine = true
            val servesBeer = true
            val takeout = true
            val facebookId = "the-facebook"
            val fax = "+7 939 32 12"
            val email = "the-email@example.com"
            val instagram = "the-instagram"
            val twitter = "the-twitter"
            val priceLevel = "$"
            val servesVegan = true
            val servesVegetarian = true
            val rating = 5.0f
            val popularity = 0.5f
            val cuisines = listOf("greek")

            When("a SearchResultMeta object is created") {
                val searchResultMetadata =
                    SearchResultMetadata.Builder().reviewCount(reviewCount).phone(phone)
                        .website(website).averageRating(avRating).description(description)
                        .openHours(openHours).primaryPhotos(primaryPhoto).otherPhotos(otherPhoto)
                        .cpsJson(cpsJson).parking(parking).children(children).metadata(data)
                        .wheelchairAccessible(wheelchairAccessible).delivery(delivery)
                        .driveThrough(driveThrough).reservable(reservable)
                        .parkingAvailable(parkingAvailable)
                        .valetParking(valetParking).streetParking(streetParking)
                        .servesBreakfast(servesBreakfast).servesBrunch(servesBrunch)
                        .servesDinner(servesDinner).servesLunch(servesLunch).servesWine(servesWine)
                        .servesBeer(servesBeer)
                        .takeout(takeout).facebookId(facebookId).fax(fax).email(email)
                        .instagram(instagram).twitter(twitter).priceLevel(priceLevel)
                        .servesVegan(servesVegan).servesVegetarian(servesVegetarian).rating(rating)
                        .popularity(popularity)
                        .cuisines(cuisines)
                        .build()

                Then("all properties should be set") {
                    assertEquals(searchResultMetadata.reviewCount, reviewCount)
                    assertEquals(searchResultMetadata.phone, phone)
                    assertEquals(searchResultMetadata.website, website)
                    assertEquals(searchResultMetadata.averageRating, avRating)
                    assertEquals(searchResultMetadata.description, description)
                    assertEquals(searchResultMetadata.openHours, openHours)
                    assertEquals(searchResultMetadata.primaryPhotos, primaryPhoto)
                    assertEquals(searchResultMetadata.otherPhotos, otherPhoto)
                    assertEquals(searchResultMetadata.cpsJson, cpsJson)
                    assertEquals(searchResultMetadata.parking, parking)
                    assertEquals(searchResultMetadata.children, children)
                    assertEquals(searchResultMetadata.extraData, data)
                    assertEquals(searchResultMetadata.wheelchairAccessible, wheelchairAccessible)
                    assertEquals(searchResultMetadata.delivery, delivery)
                    assertEquals(searchResultMetadata.driveThrough, driveThrough)
                    assertEquals(searchResultMetadata.reservable, reservable)
                    assertEquals(searchResultMetadata.parkingAvailable, parkingAvailable)
                    assertEquals(searchResultMetadata.valetParking, valetParking)
                    assertEquals(searchResultMetadata.streetParking, streetParking)
                    assertEquals(searchResultMetadata.servesBreakfast, servesBreakfast)
                    assertEquals(searchResultMetadata.servesBrunch, servesBrunch)
                    assertEquals(searchResultMetadata.servesDinner, servesDinner)
                    assertEquals(searchResultMetadata.servesLunch, servesLunch)
                    assertEquals(searchResultMetadata.servesWine, servesWine)
                    assertEquals(searchResultMetadata.servesBeer, servesBeer)
                    assertEquals(searchResultMetadata.takeout, takeout)
                    assertEquals(searchResultMetadata.facebookId, facebookId)
                    assertEquals(searchResultMetadata.fax, fax)
                    assertEquals(searchResultMetadata.email, email)
                    assertEquals(searchResultMetadata.instagram, instagram)
                    assertEquals(searchResultMetadata.twitter, twitter)
                    assertEquals(searchResultMetadata.priceLevel, priceLevel)
                    assertEquals(searchResultMetadata.servesVegan, servesVegan)
                    assertEquals(searchResultMetadata.servesVegetarian, servesVegetarian)
                    assertEquals(searchResultMetadata.rating, rating)
                    assertEquals(searchResultMetadata.popularity, popularity)
                    assertEquals(searchResultMetadata.cuisines, cuisines)
                }
            }
        }
    }

    @TestFactory
    fun `Test generated equals(), hashCode() and toString() methods`() = TestCase {
        Given("SearchResultMetadata class") {

            val reflectionObjectFactory = ReflectionObjectsFactory(
                extraCreators = SdkCustomTypeObjectCreators.ALL_CREATORS,
            )

            When("SearchResultMetadata class") {
                Then("equals() and hashCode() functions should use every declared property") {
                    EqualsVerifier
                        .forClass(SearchResultMetadata::class.java)
                        .withPrefabValues(
                            CoreChildMetadata::class.java,
                            CoreChildMetadata(
                                mapboxId = "mapboxId_1",
                                name = "name_1",
                                category = "category_1",
                                coordinates = Point.fromLngLat(1.0, 0.0),
                            ),
                            CoreChildMetadata(
                                mapboxId = "mapboxId_2",
                                name = "name_2",
                                category = "category_2",
                                coordinates = Point.fromLngLat(2.0, 0.0),
                            ),
                        )
                        .withPrefabValues(
                            ChildMetadata::class.java,
                            ChildMetadata(
                                mapboxId = "mapboxId_1",
                                name = "name_1",
                                category = "category_1",
                                coordinates = Point.fromLngLat(1.0, 0.0),
                            ),
                            ChildMetadata(
                                mapboxId = "mapboxId_2",
                                name = "name_2",
                                category = "category_2",
                                coordinates = Point.fromLngLat(2.0, 0.0),
                            ),
                        )
                        .withPrefabTestPoint()
                        .withOnlyTheseFields("coreMetadata")
                        .verify()
                }

                Then("toString() function should use every declared property") {
                    ToStringVerifier(
                        clazz = SearchResultMetadata::class,
                        objectsFactory = reflectionObjectFactory,
                        ignoredProperties = listOf("coreMetadata"),
                    ).verify()
                }
            }
        }
    }
}
