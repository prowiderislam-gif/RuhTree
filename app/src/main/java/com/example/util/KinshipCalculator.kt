package com.example.util

import com.example.data.model.FamilyMember
import com.example.data.model.Gender
import com.example.data.model.SpouseRelation

data class KinshipRelationship(
    val titleForA: String, // What A is to B
    val titleForB: String, // What B is to A
    val detailedExplanation: String,
    val kinshipCategory: KinshipCategory,
    val isDualRelationship: Boolean = false
)

enum class KinshipCategory {
    SELF,
    PARENT_CHILD,
    SPOUSE,
    CONSANGUINEOUS_SPOUSE, // Cousin marriage / blood relative spouse
    SIBLING,
    HALF_SIBLING,
    GRANDPARENT_GRANDCHILD,
    UNCLE_AUNT_NEPHEW_NIECE,
    COUSIN,
    IN_LAW,
    STEP_RELATION,
    ANCESTOR_DESCENDANT,
    DISTANT_RELATIVE,
    UNRELATED
}

object KinshipCalculator {

    fun determineRelationship(
        personA: FamilyMember,
        personB: FamilyMember,
        allMembers: List<FamilyMember>,
        spouses: List<SpouseRelation>
    ): KinshipRelationship {
        if (personA.id == personB.id) {
            return KinshipRelationship(
                titleForA = "Self",
                titleForB = "Self",
                detailedExplanation = "Same person",
                kinshipCategory = KinshipCategory.SELF
            )
        }

        val memberMap = allMembers.associateBy { it.id }

        // Check if direct spouses
        val directSpouse = spouses.find {
            (it.memberId1 == personA.id && it.memberId2 == personB.id) ||
            (it.memberId1 == personB.id && it.memberId2 == personA.id)
        }

        // Calculate genetic/genealogical blood kinship first
        val bloodRelation = determineBloodRelationship(personA, personB, memberMap)

        // If they are spouses AND have a biological relationship (e.g. cousin marriage)
        if (directSpouse != null) {
            val prefix = if (directSpouse.isDivorced || personA.isDivorced || personB.isDivorced) "Ex-" else ""
            val spouseTitleA = when (personA.gender) {
                Gender.FEMALE.name -> "${prefix}Wife"
                Gender.MALE.name -> "${prefix}Husband"
                else -> "${prefix}Spouse"
            }
            val spouseTitleB = when (personB.gender) {
                Gender.FEMALE.name -> "${prefix}Wife"
                Gender.MALE.name -> "${prefix}Husband"
                else -> "${prefix}Spouse"
            }

            if (bloodRelation != null && bloodRelation.kinshipCategory != KinshipCategory.UNRELATED) {
                // Dual Relationship: e.g. "Wife and First Cousin" or "Husband and Paternal Cousin"
                val dualTitleA = "$spouseTitleA and ${bloodRelation.titleForA}"
                val dualTitleB = "$spouseTitleB and ${bloodRelation.titleForB}"
                val divorceNote = if (directSpouse.isDivorced) " (Previously Married)" else ""
                val explanation = "${personA.name} and ${personB.name} are spouses$divorceNote, and are also related by blood: ${bloodRelation.detailedExplanation}"

                return KinshipRelationship(
                    titleForA = dualTitleA,
                    titleForB = dualTitleB,
                    detailedExplanation = explanation,
                    kinshipCategory = KinshipCategory.CONSANGUINEOUS_SPOUSE,
                    isDualRelationship = true
                )
            } else {
                return KinshipRelationship(
                    titleForA = spouseTitleA,
                    titleForB = spouseTitleB,
                    detailedExplanation = if (directSpouse.isDivorced) "${personA.name} and ${personB.name} were previously married." else "${personA.name} and ${personB.name} are spouses.",
                    kinshipCategory = KinshipCategory.SPOUSE
                )
            }
        }

        // If not spouses, but have blood relation, return it
        if (bloodRelation != null) {
            return bloodRelation
        }

        // Check In-Law and Step relationships
        val inLawRelation = determineInLawRelationship(personA, personB, allMembers, spouses, memberMap)
        if (inLawRelation != null) {
            return inLawRelation
        }

        return KinshipRelationship(
            titleForA = "Relative / Branch Member",
            titleForB = "Relative / Branch Member",
            detailedExplanation = "Connected within the family tree network.",
            kinshipCategory = KinshipCategory.UNRELATED
        )
    }

    private fun determineBloodRelationship(
        personA: FamilyMember,
        personB: FamilyMember,
        memberMap: Map<Long, FamilyMember>
    ): KinshipRelationship? {
        // 1. Direct Parent - Child
        if (personA.id == personB.fatherId) {
            val childTitle = if (personB.gender == Gender.FEMALE.name) "Daughter" else if (personB.gender == Gender.MALE.name) "Son" else "Child"
            return KinshipRelationship("Father", childTitle, "${personA.name} is the biological father of ${personB.name}.", KinshipCategory.PARENT_CHILD)
        }
        if (personA.id == personB.motherId) {
            val childTitle = if (personB.gender == Gender.FEMALE.name) "Daughter" else if (personB.gender == Gender.MALE.name) "Son" else "Child"
            return KinshipRelationship("Mother", childTitle, "${personA.name} is the biological mother of ${personB.name}.", KinshipCategory.PARENT_CHILD)
        }
        if (personB.id == personA.fatherId) {
            val childTitle = if (personA.gender == Gender.FEMALE.name) "Daughter" else if (personA.gender == Gender.MALE.name) "Son" else "Child"
            return KinshipRelationship(childTitle, "Father", "${personB.name} is the biological father of ${personA.name}.", KinshipCategory.PARENT_CHILD)
        }
        if (personB.id == personA.motherId) {
            val childTitle = if (personA.gender == Gender.FEMALE.name) "Daughter" else if (personA.gender == Gender.MALE.name) "Son" else "Child"
            return KinshipRelationship(childTitle, "Mother", "${personB.name} is the biological mother of ${personA.name}.", KinshipCategory.PARENT_CHILD)
        }

        // 2. Siblings and Half-Siblings
        val aParents = listOfNotNull(personA.fatherId, personA.motherId)
        val bParents = listOfNotNull(personB.fatherId, personB.motherId)
        val sharedParents = aParents.intersect(bParents.toSet())

        if (aParents.isNotEmpty() && bParents.isNotEmpty() && sharedParents.isNotEmpty()) {
            val hasSameFather = personA.fatherId != null && personA.fatherId == personB.fatherId
            val hasSameMother = personA.motherId != null && personA.motherId == personB.motherId

            if (hasSameFather && hasSameMother) {
                val titleA = if (personA.gender == Gender.FEMALE.name) "Sister" else if (personA.gender == Gender.MALE.name) "Brother" else "Sibling"
                val titleB = if (personB.gender == Gender.FEMALE.name) "Sister" else if (personB.gender == Gender.MALE.name) "Brother" else "Sibling"
                return KinshipRelationship(titleA, titleB, "${personA.name} and ${personB.name} are full siblings sharing both parents.", KinshipCategory.SIBLING)
            } else if (hasSameFather) {
                val titleA = if (personA.gender == Gender.FEMALE.name) "Paternal Half-Sister" else "Paternal Half-Brother"
                val titleB = if (personB.gender == Gender.FEMALE.name) "Paternal Half-Sister" else "Paternal Half-Brother"
                return KinshipRelationship(titleA, titleB, "${personA.name} and ${personB.name} share the same father.", KinshipCategory.HALF_SIBLING)
            } else if (hasSameMother) {
                val titleA = if (personA.gender == Gender.FEMALE.name) "Maternal Half-Sister" else "Maternal Half-Brother"
                val titleB = if (personB.gender == Gender.FEMALE.name) "Maternal Half-Sister" else "Maternal Half-Brother"
                return KinshipRelationship(titleA, titleB, "${personA.name} and ${personB.name} share the same mother.", KinshipCategory.HALF_SIBLING)
            }
        }

        val bFather = personB.fatherId?.let { memberMap[it] }
        val bMother = personB.motherId?.let { memberMap[it] }
        val aFather = personA.fatherId?.let { memberMap[it] }
        val aMother = personA.motherId?.let { memberMap[it] }

        // 3. Grandparent / Grandchild
        if (bFather?.fatherId == personA.id) {
            val titleB = if (personB.gender == Gender.FEMALE.name) "Paternal Granddaughter" else "Paternal Grandson"
            return KinshipRelationship("Paternal Grandfather", titleB, "${personA.name} is the paternal grandfather of ${personB.name}.", KinshipCategory.GRANDPARENT_GRANDCHILD)
        }
        if (bFather?.motherId == personA.id) {
            val titleB = if (personB.gender == Gender.FEMALE.name) "Paternal Granddaughter" else "Paternal Grandson"
            return KinshipRelationship("Paternal Grandmother", titleB, "${personA.name} is the paternal grandmother of ${personB.name}.", KinshipCategory.GRANDPARENT_GRANDCHILD)
        }
        if (bMother?.fatherId == personA.id) {
            val titleB = if (personB.gender == Gender.FEMALE.name) "Maternal Granddaughter" else "Maternal Grandson"
            return KinshipRelationship("Maternal Grandfather", titleB, "${personA.name} is the maternal grandfather of ${personB.name}.", KinshipCategory.GRANDPARENT_GRANDCHILD)
        }
        if (bMother?.motherId == personA.id) {
            val titleB = if (personB.gender == Gender.FEMALE.name) "Maternal Granddaughter" else "Maternal Grandson"
            return KinshipRelationship("Maternal Grandmother", titleB, "${personA.name} is the maternal grandmother of ${personB.name}.", KinshipCategory.GRANDPARENT_GRANDCHILD)
        }

        if (aFather?.fatherId == personB.id) {
            val titleA = if (personA.gender == Gender.FEMALE.name) "Paternal Granddaughter" else "Paternal Grandson"
            return KinshipRelationship(titleA, "Paternal Grandfather", "${personB.name} is the paternal grandfather of ${personA.name}.", KinshipCategory.GRANDPARENT_GRANDCHILD)
        }
        if (aFather?.motherId == personB.id) {
            val titleA = if (personA.gender == Gender.FEMALE.name) "Paternal Granddaughter" else "Paternal Grandson"
            return KinshipRelationship(titleA, "Paternal Grandmother", "${personB.name} is the paternal grandmother of ${personA.name}.", KinshipCategory.GRANDPARENT_GRANDCHILD)
        }
        if (aMother?.fatherId == personB.id) {
            val titleA = if (personA.gender == Gender.FEMALE.name) "Maternal Granddaughter" else "Maternal Grandson"
            return KinshipRelationship(titleA, "Maternal Grandfather", "${personB.name} is the maternal grandfather of ${personA.name}.", KinshipCategory.GRANDPARENT_GRANDCHILD)
        }
        if (aMother?.motherId == personB.id) {
            val titleA = if (personA.gender == Gender.FEMALE.name) "Maternal Granddaughter" else "Maternal Grandson"
            return KinshipRelationship(titleA, "Maternal Grandmother", "${personB.name} is the maternal grandmother of ${personA.name}.", KinshipCategory.GRANDPARENT_GRANDCHILD)
        }

        // 4. Uncle / Aunt & Nephew / Niece
        if (bFather != null && areSiblings(personA, bFather)) {
            val titleA = if (personA.gender == Gender.FEMALE.name) "Paternal Aunt" else "Paternal Uncle"
            val titleB = if (personB.gender == Gender.FEMALE.name) "Paternal Niece" else "Paternal Nephew"
            return KinshipRelationship(titleA, titleB, "${personA.name} is the sibling of ${personB.name}'s father (${bFather.name}).", KinshipCategory.UNCLE_AUNT_NEPHEW_NIECE)
        }
        if (bMother != null && areSiblings(personA, bMother)) {
            val titleA = if (personA.gender == Gender.FEMALE.name) "Maternal Aunt" else "Maternal Uncle"
            val titleB = if (personB.gender == Gender.FEMALE.name) "Maternal Niece" else "Maternal Nephew"
            return KinshipRelationship(titleA, titleB, "${personA.name} is the sibling of ${personB.name}'s mother (${bMother.name}).", KinshipCategory.UNCLE_AUNT_NEPHEW_NIECE)
        }
        if (aFather != null && areSiblings(personB, aFather)) {
            val titleB = if (personB.gender == Gender.FEMALE.name) "Paternal Aunt" else "Paternal Uncle"
            val titleA = if (personA.gender == Gender.FEMALE.name) "Paternal Niece" else "Paternal Nephew"
            return KinshipRelationship(titleA, titleB, "${personB.name} is the sibling of ${personA.name}'s father (${aFather.name}).", KinshipCategory.UNCLE_AUNT_NEPHEW_NIECE)
        }
        if (aMother != null && areSiblings(personB, aMother)) {
            val titleB = if (personB.gender == Gender.FEMALE.name) "Maternal Aunt" else "Maternal Uncle"
            val titleA = if (personA.gender == Gender.FEMALE.name) "Maternal Niece" else "Maternal Nephew"
            return KinshipRelationship(titleA, titleB, "${personB.name} is the sibling of ${personA.name}'s mother (${aMother.name}).", KinshipCategory.UNCLE_AUNT_NEPHEW_NIECE)
        }

        // 5. First Cousins (Paternal / Maternal / First Cousin)
        val aParentsList = listOfNotNull(aFather, aMother)
        val bParentsList = listOfNotNull(bFather, bMother)
        for (ap in aParentsList) {
            for (bp in bParentsList) {
                if (areSiblings(ap, bp)) {
                    val isPaternal = (ap.id == personA.fatherId && bp.id == personB.fatherId)
                    val isMaternal = (ap.id == personA.motherId && bp.id == personB.motherId)
                    val prefix = if (isPaternal) "Paternal " else if (isMaternal) "Maternal " else ""
                    val titleA = "${prefix}First Cousin"
                    val titleB = "${prefix}First Cousin"
                    return KinshipRelationship(
                        titleForA = titleA,
                        titleForB = titleB,
                        detailedExplanation = "${personA.name} and ${personB.name} share grandparents (their parents ${ap.name} and ${bp.name} are siblings).",
                        kinshipCategory = KinshipCategory.COUSIN
                    )
                }
            }
        }

        // 6. Ancestor / Descendant & Distant Common Ancestors via BFS
        val aAncestorsDistance = getAncestorDistanceMap(personA, memberMap)
        val bAncestorsDistance = getAncestorDistanceMap(personB, memberMap)

        if (aAncestorsDistance.containsKey(personB.id)) {
            val dist = aAncestorsDistance[personB.id] ?: 1
            val titleB = getAncestorTitle(dist, personB.gender)
            val titleA = getDescendantTitle(dist, personA.gender)
            return KinshipRelationship(titleA, titleB, "${personB.name} is a ${dist}-generation direct ancestor of ${personA.name}.", KinshipCategory.ANCESTOR_DESCENDANT)
        }

        if (bAncestorsDistance.containsKey(personA.id)) {
            val dist = bAncestorsDistance[personA.id] ?: 1
            val titleA = getAncestorTitle(dist, personA.gender)
            val titleB = getDescendantTitle(dist, personB.gender)
            return KinshipRelationship(titleA, titleB, "${personA.name} is a ${dist}-generation direct ancestor of ${personB.name}.", KinshipCategory.ANCESTOR_DESCENDANT)
        }

        // Common Ancestors for 2nd Cousins or Distant Blood Relatives
        val commonAncestors = aAncestorsDistance.keys.intersect(bAncestorsDistance.keys)
        if (commonAncestors.isNotEmpty()) {
            val lowestCommon = commonAncestors.minByOrNull { (aAncestorsDistance[it] ?: 99) + (bAncestorsDistance[it] ?: 99) }
            if (lowestCommon != null) {
                val distA = aAncestorsDistance[lowestCommon] ?: 0
                val distB = bAncestorsDistance[lowestCommon] ?: 0
                val lca = memberMap[lowestCommon]

                if (distA == 2 && distB == 2) {
                    return KinshipRelationship(
                        titleForA = "Second Cousin",
                        titleForB = "Second Cousin",
                        detailedExplanation = "${personA.name} and ${personB.name} share great-grandparents (${lca?.name ?: "Lineage Ancestor"}).",
                        kinshipCategory = KinshipCategory.COUSIN
                    )
                } else if (distA == 2 && distB == 1) {
                    val titleA = if (personA.gender == Gender.FEMALE.name) "First Cousin Once Removed (Niece)" else "First Cousin Once Removed (Nephew)"
                    val titleB = if (personB.gender == Gender.FEMALE.name) "First Cousin Once Removed (Aunt)" else "First Cousin Once Removed (Uncle)"
                    return KinshipRelationship(titleA, titleB, "${personA.name} and ${personB.name} are first cousins once removed.", KinshipCategory.COUSIN)
                } else if (distA == 1 && distB == 2) {
                    val titleA = if (personA.gender == Gender.FEMALE.name) "First Cousin Once Removed (Aunt)" else "First Cousin Once Removed (Uncle)"
                    val titleB = if (personB.gender == Gender.FEMALE.name) "First Cousin Once Removed (Niece)" else "First Cousin Once Removed (Nephew)"
                    return KinshipRelationship(titleA, titleB, "${personA.name} and ${personB.name} are first cousins once removed.", KinshipCategory.COUSIN)
                } else {
                    return KinshipRelationship(
                        titleForA = "Blood Relative (${distA} gen)",
                        titleForB = "Blood Relative (${distB} gen)",
                        detailedExplanation = "Connected through common ancestor ${lca?.name ?: "Family Lineage"}.",
                        kinshipCategory = KinshipCategory.DISTANT_RELATIVE
                    )
                }
            }
        }

        return null
    }

    private fun determineInLawRelationship(
        personA: FamilyMember,
        personB: FamilyMember,
        allMembers: List<FamilyMember>,
        spouses: List<SpouseRelation>,
        memberMap: Map<Long, FamilyMember>
    ): KinshipRelationship? {
        val bChildren = allMembers.filter { it.fatherId == personB.id || it.motherId == personB.id }
        for (child in bChildren) {
            if (areSpouses(personA.id, child.id, spouses)) {
                val titleA = if (personA.gender == Gender.FEMALE.name) "Daughter-in-law" else "Son-in-law"
                val titleB = if (personB.gender == Gender.FEMALE.name) "Mother-in-law" else "Father-in-law"
                return KinshipRelationship(titleA, titleB, "${personA.name} is married to ${personB.name}'s child (${child.name}).", KinshipCategory.IN_LAW)
            }
        }

        val aChildren = allMembers.filter { it.fatherId == personA.id || it.motherId == personA.id }
        for (child in aChildren) {
            if (areSpouses(personB.id, child.id, spouses)) {
                val titleB = if (personB.gender == Gender.FEMALE.name) "Daughter-in-law" else "Son-in-law"
                val titleA = if (personA.gender == Gender.FEMALE.name) "Mother-in-law" else "Father-in-law"
                return KinshipRelationship(titleA, titleB, "${personB.name} is married to ${personA.name}'s child (${child.name}).", KinshipCategory.IN_LAW)
            }
        }

        val bSiblings = allMembers.filter { it.id != personB.id && areSiblings(it, personB) }
        for (sib in bSiblings) {
            if (areSpouses(personA.id, sib.id, spouses)) {
                val titleA = if (personA.gender == Gender.FEMALE.name) "Sister-in-law" else "Brother-in-law"
                val titleB = if (personB.gender == Gender.FEMALE.name) "Sister-in-law" else "Brother-in-law"
                return KinshipRelationship(titleA, titleB, "${personA.name} is married to ${personB.name}'s sibling (${sib.name}).", KinshipCategory.IN_LAW)
            }
        }

        return null
    }

    private fun areSiblings(m1: FamilyMember, m2: FamilyMember): Boolean {
        if (m1.id == m2.id) return false
        val sameFather = m1.fatherId != null && m1.fatherId == m2.fatherId
        val sameMother = m1.motherId != null && m1.motherId == m2.motherId
        return sameFather || sameMother
    }

    private fun areSpouses(id1: Long, id2: Long, spouses: List<SpouseRelation>): Boolean {
        return spouses.any {
            (it.memberId1 == id1 && it.memberId2 == id2) ||
            (it.memberId1 == id2 && it.memberId2 == id1)
        }
    }

    private fun getAncestorDistanceMap(member: FamilyMember, memberMap: Map<Long, FamilyMember>): Map<Long, Int> {
        val result = mutableMapOf<Long, Int>()
        val queue = ArrayDeque<Pair<Long, Int>>()

        member.fatherId?.let { queue.add(it to 1) }
        member.motherId?.let { queue.add(it to 1) }

        while (queue.isNotEmpty()) {
            val (currentId, dist) = queue.removeFirst()
            if (!result.containsKey(currentId) || result[currentId]!! > dist) {
                result[currentId] = dist
                val parent = memberMap[currentId]
                parent?.fatherId?.let { queue.add(it to dist + 1) }
                parent?.motherId?.let { queue.add(it to dist + 1) }
            }
        }
        return result
    }

    private fun getAncestorTitle(distance: Int, gender: String): String {
        return when (distance) {
            1 -> if (gender == Gender.FEMALE.name) "Mother" else "Father"
            2 -> if (gender == Gender.FEMALE.name) "Grandmother" else "Grandfather"
            3 -> if (gender == Gender.FEMALE.name) "Great-Grandmother" else "Great-Grandfather"
            4 -> if (gender == Gender.FEMALE.name) "Great-Great-Grandmother" else "Great-Great-Grandfather"
            else -> "${distance}th Gen Ancestor"
        }
    }

    private fun getDescendantTitle(distance: Int, gender: String): String {
        return when (distance) {
            1 -> if (gender == Gender.FEMALE.name) "Daughter" else "Son"
            2 -> if (gender == Gender.FEMALE.name) "Granddaughter" else "Grandson"
            3 -> if (gender == Gender.FEMALE.name) "Great-Granddaughter" else "Great-Grandson"
            4 -> if (gender == Gender.FEMALE.name) "Great-Great-Granddaughter" else "Great-Great-Grandson"
            else -> "${distance}th Gen Descendant"
        }
    }
}
