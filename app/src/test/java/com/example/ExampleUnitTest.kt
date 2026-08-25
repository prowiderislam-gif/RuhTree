package com.example

import com.example.data.model.FamilyMember
import com.example.data.model.Gender
import com.example.data.model.SpouseRelation
import com.example.util.DateUtils
import com.example.util.KinshipCalculator
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testPaternalUncleAndNephewKinship() {
        // Grandfather
        val grandfather = FamilyMember(id = 1, name = "Arthur", gender = Gender.MALE.name)
        // Father (son of Arthur)
        val father = FamilyMember(id = 2, name = "Jonathan", gender = Gender.MALE.name, fatherId = 1)
        // Paternal Uncle (brother of father, son of Arthur)
        val uncle = FamilyMember(id = 3, name = "Marcus", gender = Gender.MALE.name, fatherId = 1)
        // Boy (son of Jonathan -> nephew to Marcus)
        val boy = FamilyMember(id = 4, name = "Leo", gender = Gender.MALE.name, fatherId = 2)

        val members = listOf(grandfather, father, uncle, boy)
        val spouses = emptyList<SpouseRelation>()

        // Compare uncle & boy
        val result = KinshipCalculator.determineRelationship(uncle, boy, members, spouses)

        assertEquals("Paternal Uncle", result.titleForA)
        assertEquals("Paternal Nephew", result.titleForB)
    }

    @Test
    fun testAgeCalculationAndDifference() {
        val ageDiff = DateUtils.calculateAgeDifference("1976-12-04", "2008-04-12")
        assertTrue(ageDiff.hasExactDates)
        assertTrue(ageDiff.firstIsOlder)
        assertTrue(ageDiff.differenceText.contains("31 year"))
    }
}
